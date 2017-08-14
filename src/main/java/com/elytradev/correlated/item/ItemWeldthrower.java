package com.elytradev.correlated.item;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableInt;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.entity.EntityAutomaton;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CItems;
import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.network.StartWeldthrowingMessage;
import com.google.common.collect.Maps;

import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

public class ItemWeldthrower extends Item {
	public ItemWeldthrower() {
		MinecraftForge.EVENT_BUS.register(this);
		setMaxStackSize(1);
	}
	
	public Map<EntityPlayer, MutableInt> weldthrowing = Maps.newIdentityHashMap();
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		int i = 0;
		while (I18n.canTranslate("tooltip.correlated.weldthrower." + i)) {
			tooltip.add(I18n.translateToLocal("tooltip.correlated.weldthrower." + i));
			i++;
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemStack = player.getHeldItem(hand);
		if (hand == EnumHand.MAIN_HAND) {
			if (!world.isRemote && !weldthrowing.containsKey(player) && (player.capabilities.isCreativeMode || player.inventory.clearMatchingItems(CItems.MISC, 5, 1, null) > 0)) {
				world.playSound(null, player.posX, player.posY, player.posZ, CSoundEvents.WELDTHROW, SoundCategory.PLAYERS, 0.4f, 1f);
				weldthrowing.put(player, new MutableInt());
				new StartWeldthrowingMessage(player.getEntityId()).sendToAllWatching(player);;
				return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
			}
		}
		return super.onItemRightClick(world, player, hand);
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent e) {
		if (weldthrowing.containsKey(e.player)) {
			MutableInt mi = weldthrowing.get(e.player);
			mi.increment();
			if (mi.intValue() > 65 || !e.player.isEntityAlive()) {
				weldthrowing.remove(e.player);
				return;
			}
			Correlated.proxy.weldthrowerTick(e.player);
			Vec3d look = e.player.getLookVec();
			Vec3d right = look.rotateYaw(-90);
			double dist = 0.5;
			double gap = 0.6;
			look.rotateYaw(20);
			Vec3d cursor = new Vec3d(e.player.posX+(right.x*dist)+(look.x*gap),
					e.player.posY+(e.player.getEyeHeight()-0.1)+(right.y*dist)+(look.y*gap),
					e.player.posZ+(right.z*dist)+(look.z*gap));
			for (int i = 0; i < Math.min(mi.intValue()/4, 10); i++) {
				AxisAlignedBB aabb = new AxisAlignedBB(cursor.x-0.1, cursor.y-0.1, cursor.z-0.1, cursor.x+0.1, cursor.y+0.1, cursor.z+0.1);
				if (e.player.world.collidesWithAnyBlock(aabb)) break;
				aabb = aabb.grow(0.9);
				for (Entity ent : e.player.world.getEntitiesWithinAABBExcludingEntity(e.player, aabb)) {
					if (ent instanceof EntityAutomaton) {
						EntityAutomaton a = ((EntityAutomaton) ent);
						if (!a.world.isRemote) {
							if (a.world.rand.nextInt(3) == 0 && a.getHealth() < a.getMaxHealth() && a.getFavor(e.player) >= 0) {
								a.adjustFavor(e.player, 1);
							}
							a.heal(0.05f);
							Correlated.proxy.weldthrowerHeal((EntityAutomaton)ent);
						}
					} else if (ent instanceof EntityLivingBase && CConfig.weldthrowerHurts) {
						EntityLivingBase elb = (EntityLivingBase)ent;
						elb.setFire(4);
						elb.attackEntityFrom(new EntityDamageSource("correlated.weld", e.player), 2);
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
