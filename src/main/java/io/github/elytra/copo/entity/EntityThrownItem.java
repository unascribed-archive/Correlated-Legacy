package io.github.elytra.copo.entity;

import com.google.common.base.Optional;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.network.SetGlitchingStateMessage;
import io.github.elytra.copo.network.SetGlitchingStateMessage.GlitchState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityThrownItem extends EntityEnderPearl {

	public EntityThrownItem(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	public EntityThrownItem(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
	}

	public EntityThrownItem(World worldIn) {
		super(worldIn);
	}

	private static final DataParameter<Optional<ItemStack>> STACK = EntityDataManager.createKey(EntityThrownItem.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private boolean noTeleport = false;
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(STACK, Optional.absent());
		timeUntilPortal = 1000;
	}
	
	public ItemStack getStack() {
		return dataManager.get(STACK).orNull();
	}
	
	public void setStack(ItemStack stack) {
		dataManager.set(STACK, Optional.of(stack));
	}
	
	@Override
	protected void onImpact(RayTraceResult result) {
		if (!worldObj.isRemote && getStack() != null) {
			if (getStack() != null && getStack().getItem() == CoPo.misc && getStack().getMetadata() == 6) {
				if (getThrower() instanceof EntityPlayerMP && !noTeleport) {
					super.onImpact(result);
					EntityPlayerMP p = (EntityPlayerMP)getThrower();
					p.mcServer.addScheduledTask(() -> {
						NBTTagCompound oldEntity = p.writeToNBT(new NBTTagCompound());
						CoPo.inst.network.sendTo(new SetGlitchingStateMessage(GlitchState.CORRUPTING), p);
						int dim = CoPo.limboDimId;
						if (net.minecraftforge.common.ForgeHooks.onTravelToDimension(p, dim)) {
							PlayerList r = p.mcServer.getPlayerList();
							r.updatePermissionLevel(p);
							p.setDropItemsWhenDead(false);
							p.setDead();
						}
					});
					return;
				}
			}
			EntityItem item = new EntityItem(worldObj, result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord, getStack());
			worldObj.spawnEntityInWorld(item);
		}
		setDead();
	}
	
	@Override
	public void onUpdate() {
		if (worldObj.getBlockState(getPosition()).getBlock() == Blocks.PORTAL) {
			if (getStack() != null && getStack().getItem() == CoPo.misc && getStack().getMetadata() == 3) {
				if (!worldObj.isRemote) {
					worldObj.playSound(null, posX, posY, posZ, CoPo.glitch, SoundCategory.PLAYERS, 1, rand.nextFloat()+0.75f);
					if (worldObj instanceof WorldServer) {
						((WorldServer)worldObj).spawnParticle(EnumParticleTypes.REDSTONE, posX, posY, posZ, 100, 0.2, 0.2, 0.2, 100);
					}
				}
				setStack(new ItemStack(CoPo.misc, getStack().stackSize, 6));
				noTeleport = true;
			}
		}
		super.onUpdate();
	}
	
}
