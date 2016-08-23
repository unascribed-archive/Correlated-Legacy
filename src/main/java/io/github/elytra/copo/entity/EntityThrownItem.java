package io.github.elytra.copo.entity;

import com.google.common.base.Optional;
import com.google.common.primitives.Ints;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.network.SetGlitchingStateMessage;
import io.github.elytra.copo.network.SetGlitchingStateMessage.GlitchState;
import io.github.elytra.copo.world.DungeonPlayer;
import io.github.elytra.copo.world.LimboProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.DamageSource;
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
		if (getStack() != null) {
			if (!worldObj.isRemote && result.entityHit != null) {
				result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 0);
			}
			if (getStack() != null && getStack().getItem() == CoPo.misc) {
				if (getStack().getMetadata() == 6 && !worldObj.isRemote) {
					if (getThrower() instanceof EntityPlayerMP && !noTeleport) {
						super.onImpact(result);
						EntityPlayerMP p = (EntityPlayerMP)getThrower();
						p.mcServer.addScheduledTask(() -> {
							NBTTagCompound oldEntity = p.writeToNBT(new NBTTagCompound());
							DungeonPlayer player = new DungeonPlayer(p.getGameProfile(), p.inventory.getFirstEmptyStack(), oldEntity);
							int dim = CoPo.limboDimId;
							if (net.minecraftforge.common.ForgeHooks.onTravelToDimension(p, dim)) {
								CoPo.inst.network.sendTo(new SetGlitchingStateMessage(GlitchState.CORRUPTING), p);
								WorldServer dest = p.mcServer.worldServerForDimension(dim);
								if (dest.provider instanceof LimboProvider) {
									((LimboProvider)dest.provider).addEnteringPlayer(player);
								}
								PlayerList r = p.mcServer.getPlayerList();
								r.updatePermissionLevel(p);
								p.setDead();
							}
						});
						return;
					}
				} else if (getStack().getMetadata() == 8) {
					playSound(CoPo.data_core_shatter, 1f, 0.875f+(rand.nextFloat()/4));
					if (!worldObj.isRemote) {
						for (ItemStack is : CoPo.drive.getTypes(getStack())) {
							int amt = is.stackSize;
							while (amt > 0) {
								ItemStack stack = is.copy();
								stack.stackSize = Math.min(10000, amt);
								EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, stack);
								entityitem.setDefaultPickupDelay();
								// You just KNOW someone is going to use MAX_VALUE as a lifespan
								entityitem.lifespan = Ints.saturatedCast(entityitem.lifespan*64L);
								if (captureDrops) {
									capturedDrops.add(entityitem);
								} else {
									worldObj.spawnEntityInWorld(entityitem);
								}
								amt -= stack.stackSize;
							}
						}
					}
					setDead();
					if (worldObj instanceof WorldServer) {
						((WorldServer)worldObj).spawnParticle(EnumParticleTypes.ITEM_CRACK,
								result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord, 80,
								0, 0, 0, 0.15, Item.getIdFromItem(CoPo.misc), 8);
					}
					return;
				}
			}
			if (!worldObj.isRemote) {
				EntityItem item = new EntityItem(worldObj, result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord, getStack());
				worldObj.spawnEntityInWorld(item);
			}
		}
		setDead();
	}
	
	@Override
	public void onUpdate() {
		if (!worldObj.isRemote && worldObj.getBlockState(getPosition()).getBlock() == Blocks.PORTAL) {
			if (getStack() != null && getStack().getItem() == CoPo.misc) {
				if (getStack().getMetadata() == 3) {
					worldObj.playSound(null, posX, posY, posZ, CoPo.convert, SoundCategory.PLAYERS, 1, rand.nextFloat()+0.75f);
					if (worldObj instanceof WorldServer) {
						((WorldServer)worldObj).spawnParticle(EnumParticleTypes.REDSTONE, posX, posY, posZ, 100, 0.2, 0.2, 0.2, 100);
					}
					setStack(new ItemStack(CoPo.misc, getStack().stackSize, 6));
					noTeleport = true;
				} else if (getStack().getMetadata() == 6) {
					worldObj.createExplosion(this, posX, posY, posZ, 0, false);
					setDead();
				}
			}
		}
		super.onUpdate();
	}
	
}
