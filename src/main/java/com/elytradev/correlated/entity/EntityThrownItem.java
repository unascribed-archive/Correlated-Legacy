package com.elytradev.correlated.entity;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.network.SetGlitchingStateMessage;
import com.elytradev.correlated.network.SetGlitchingStateMessage.GlitchState;
import com.elytradev.correlated.world.DungeonPlayer;
import com.elytradev.correlated.world.LimboProvider;
import com.google.common.primitives.Ints;

import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
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

	private static final DataParameter<ItemStack> STACK = EntityDataManager.createKey(EntityThrownItem.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private boolean noTeleport = false;
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(STACK, ItemStack.EMPTY);
		timeUntilPortal = 1000;
	}
	
	public ItemStack getStack() {
		return dataManager.get(STACK);
	}
	
	public void setStack(ItemStack stack) {
		dataManager.set(STACK, stack);
	}
	
	@Override
	protected void onImpact(RayTraceResult result) {
		if (getStack() != null) {
			if (!world.isRemote && result.entityHit != null) {
				result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 0);
			}
			if (getStack() != null && getStack().getItem() == Correlated.misc) {
				if (getStack().getMetadata() == 6 && !world.isRemote) {
					if (getThrower() instanceof EntityPlayerMP && !noTeleport) {
						super.onImpact(result);
						EntityPlayerMP p = (EntityPlayerMP)getThrower();
						p.mcServer.addScheduledTask(() -> {
							NBTTagCompound oldEntity = p.writeToNBT(new NBTTagCompound());
							DungeonPlayer player = new DungeonPlayer(p.getGameProfile(), p.inventory.getFirstEmptyStack(), oldEntity);
							long hashCode = 1;
							long prime = 59;
							Vec3i radius = new Vec3i(2, 2, 2);
							for (BlockPos bp : BlockPos.getAllInBoxMutable(getPosition().subtract(radius), getPosition().add(radius))) {
								IBlockState state = world.getBlockState(bp);
								hashCode = (hashCode * prime) + state.getBlock().getRegistryName().hashCode();
								hashCode = (hashCode * prime) + state.getBlock().getMetaFromState(state);
							}
							Correlated.log.debug("Dungeon seed is {}", hashCode);
							player.setSeed(hashCode);
							int dim = Correlated.limboDimId;
							if (net.minecraftforge.common.ForgeHooks.onTravelToDimension(p, dim)) {
								new SetGlitchingStateMessage(GlitchState.CORRUPTING).sendTo(p);
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
					playSound(Correlated.data_core_shatter, 1f, 0.875f+(rand.nextFloat()/4));
					if (!world.isRemote) {
						for (ItemStack is : Correlated.drive.getTypes(getStack())) {
							int amt = is.getCount();
							while (amt > 0) {
								ItemStack stack = is.copy();
								stack.setCount(Math.min(10000, amt));
								EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY, this.posZ, stack);
								entityitem.setDefaultPickupDelay();
								// You just KNOW someone is going to use MAX_VALUE as a lifespan
								entityitem.lifespan = Ints.saturatedCast(entityitem.lifespan*64L);
								if (captureDrops) {
									capturedDrops.add(entityitem);
								} else {
									world.spawnEntity(entityitem);
								}
								amt -= stack.getCount();
							}
						}
					}
					setDead();
					if (world instanceof WorldServer) {
						((WorldServer)world).spawnParticle(EnumParticleTypes.ITEM_CRACK,
								result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord, 80,
								0, 0, 0, 0.15, Item.getIdFromItem(Correlated.misc), 8);
					}
					return;
				}
			}
			if (!world.isRemote) {
				EntityItem item = new EntityItem(world, result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord, getStack());
				world.spawnEntity(item);
			}
		}
		setDead();
	}
	
	@Override
	public void onUpdate() {
		if (!world.isRemote && world.getBlockState(getPosition()).getBlock() == Blocks.PORTAL) {
			if (getStack() != null && getStack().getItem() == Correlated.misc) {
				if (getStack().getMetadata() == 3) {
					world.playSound(null, posX, posY, posZ, Correlated.convert, SoundCategory.PLAYERS, 1, rand.nextFloat()+0.75f);
					if (world instanceof WorldServer) {
						((WorldServer)world).spawnParticle(EnumParticleTypes.REDSTONE, posX, posY, posZ, 100, 0.2, 0.2, 0.2, 100);
					}
					setStack(new ItemStack(Correlated.misc, getStack().getCount(), 6));
					noTeleport = true;
				} else if (getStack().getMetadata() == 6) {
					world.createExplosion(this, posX, posY, posZ, 0, false);
					setDead();
				}
			}
		}
		super.onUpdate();
	}
	
}
