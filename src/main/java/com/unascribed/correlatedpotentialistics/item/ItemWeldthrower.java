package com.unascribed.correlatedpotentialistics.item;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.Maps;
import com.unascribed.correlatedpotentialistics.CoPo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class ItemWeldthrower extends Item {
	public ItemWeldthrower() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private Map<EntityPlayer, MutableInt> weldthrowing = Maps.newHashMap();
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		int i = 0;
		while (I18n.canTranslate("tooltip.correlatedpotentialistics.weldthrower." + i)) {
			tooltip.add(I18n.translateToLocal("tooltip.correlatedpotentialistics.weldthrower." + i));
			i++;
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (hand == EnumHand.MAIN_HAND) {
			if (!worldIn.isRemote && !weldthrowing.containsKey(playerIn) && (playerIn.capabilities.isCreativeMode || playerIn.inventory.clearMatchingItems(CoPo.misc, 5, 1, null) > 0)) {
				worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, CoPo.weldthrow, SoundCategory.PLAYERS, 0.4f, 1f);
				weldthrowing.put(playerIn, new MutableInt());
				return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
			}
		}
		return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent e) {
		if (weldthrowing.containsKey(e.player)) {
			MutableInt mi = weldthrowing.get(e.player);
			mi.increment();
			if (mi.intValue() > 100 || !e.player.isEntityAlive()) {
				weldthrowing.remove(e.player);
				return;
			}
			CoPo.proxy.weldthrowerTick(e.player);
			Vec3d look = e.player.getLookVec();
			Vec3d right = look.rotateYaw(-90);
			double dist = 0.5;
			double gap = 0.6;
			look.rotateYaw(20);
			Vec3d cursor = new Vec3d(e.player.posX+(right.xCoord*dist)+(look.xCoord*gap),
					e.player.posY+(e.player.getEyeHeight()-0.1)+(right.yCoord*dist)+(look.yCoord*gap),
					e.player.posZ+(right.zCoord*dist)+(look.zCoord*gap));
			for (int i = 0; i < Math.min(mi.intValue()/4, 10); i++) {
				AxisAlignedBB aabb = new AxisAlignedBB(cursor.xCoord-0.1, cursor.yCoord-0.1, cursor.zCoord-0.1, cursor.xCoord+0.1, cursor.yCoord+0.1, cursor.zCoord+0.1);
				if (e.player.worldObj.collidesWithAnyBlock(aabb)) break;
				aabb = aabb.expandXyz(1.4);
				for (Entity ent : e.player.worldObj.getEntitiesWithinAABBExcludingEntity(e.player, aabb)) {
					if (ent instanceof EntityLivingBase) {
						EntityLivingBase elb = (EntityLivingBase)ent;
						elb.setFire(4);
						elb.attackEntityFrom(new EntityDamageSource("correlatedpotentialistics.weld", e.player), 2);
					}
				}
				
				cursor = cursor.add(look);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent e) {
		weldthrowing.remove(e.player);
	}
}
