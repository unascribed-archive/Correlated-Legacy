package com.elytradev.correlated.item;

import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.network.fx.AddCaltropMessage;
import com.elytradev.correlated.network.fx.AddGlobeMessage;
import com.elytradev.correlated.network.fx.AddLineMessage;
import com.elytradev.correlated.tile.TileEntityController;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;
import com.elytradev.correlated.tile.TileEntityOpticalTransceiver;
import com.elytradev.correlated.wifi.Beam;
import com.elytradev.correlated.wifi.Optical;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import com.elytradev.correlated.C28n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDebugginator extends Item {

	public ItemDebugginator() {
		setMaxStackSize(1);
		setUnlocalizedName("correlated.debugginator");
	}
	
	@Override
	public boolean canHarvestBlock(IBlockState blockIn) {
		return true;
	}
	
	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		return 400f;
	}
	
	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		super.addInformation(stack, world, tooltip, flag);
		tooltip.add("\u00A75"+C28n.format("item.correlated.debugginator.0"));
		tooltip.add("\u00A75"+C28n.format("item.correlated.debugginator.1"));
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		if (player.isSneaking()) {
			if (!player.world.isRemote) {
				RayTraceResult rtr = rayTrace(player.world, player, false);
				IBlockState bs = player.world.getBlockState(pos);
				ItemStack is = bs.getBlock().getPickBlock(bs, rtr, player.world, pos, player);
				EntityItem ei = new EntityItem(player.world, rtr.hitVec.x, rtr.hitVec.y, rtr.hitVec.z);
				ei.setItem(is);
				player.world.spawnEntity(ei);
			}
			return true;
		}
		return super.onBlockStartBreak(itemstack, pos, player);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return false;
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) < 1;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return stack.hasTagCompound() ? Math.min((System.currentTimeMillis()-stack.getTagCompound().getLong("LastTeleport"))/5000D, 1) : 1;
	}
	
	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return 0xFFAA00FF;
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> mm = super.getAttributeModifiers(slot, stack);
		if (slot == EntityEquipmentSlot.MAINHAND) {
			mm.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", 6, 2));
		}
		return mm;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (isSelected && entityIn instanceof EntityPlayer) {
			((EntityPlayer)entityIn).getFoodStats().setFoodLevel(20);
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack is = playerIn.getHeldItem(handIn);
		if (playerIn.isSneaking()) {
			Correlated.proxy.clearShapes();
			if (is.hasTagCompound()) {
				is.getTagCompound().removeTag("LastTeleport");
				is.getTagCompound().removeTag("LastTeleportPos");
			}
			if (is.getItemDamage() == 0) {
				playerIn.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1f, 1.5f);
				is.setItemDamage(1);
				EnchantmentHelper.setEnchantments(ImmutableMap.of(), is);
			} else if (is.getItemDamage() == 1) {
				playerIn.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1f, 2f);
				is.setItemDamage(0);
				EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SILK_TOUCH, 1), is);
			}
			return ActionResult.newResult(EnumActionResult.SUCCESS, is);
		} else {
			if (!is.hasTagCompound()) {
				is.setTagCompound(new NBTTagCompound());
			}
			double targetX;
			double targetY;
			double targetZ;
			if (is.getTagCompound().hasKey("LastTeleportPos")
					&& System.currentTimeMillis()-is.getTagCompound().getLong("LastTeleport") < 5000) {
				NBTTagList li = is.getTagCompound().getTagList("LastTeleportPos", NBT.TAG_DOUBLE);
				targetX = li.getDoubleAt(0);
				targetY = li.getDoubleAt(1);
				targetZ = li.getDoubleAt(2);
			} else {
				float f = playerIn.rotationPitch;
				float f1 = playerIn.rotationYaw;
				double d0 = playerIn.posX;
				double d1 = playerIn.posY + playerIn.getEyeHeight();
				double d2 = playerIn.posZ;
				Vec3d vec3d = new Vec3d(d0, d1, d2);
				float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
				float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
				float f4 = -MathHelper.cos(-f * 0.017453292F);
				float f5 = MathHelper.sin(-f * 0.017453292F);
				float f6 = f3 * f4;
				float f7 = f2 * f4;
				// max distance by rayTraceBlocksFar with 45deg pitch/yaw
				double d3 = 19196;
				Vec3d vec3d1 = vec3d.addVector(f6 * d3, f5 * d3, f7 * d3);
				RayTraceResult rtr = rayTraceBlocksFar(worldIn, vec3d, vec3d1, true, false, false);
				if (rtr != null) {
					targetX = rtr.hitVec.x;
					targetY = rtr.hitVec.y;
					targetZ = rtr.hitVec.z;
				} else {
					return ActionResult.newResult(EnumActionResult.SUCCESS, is);
				}
			}
			
			is.getTagCompound().setLong("LastTeleport", System.currentTimeMillis());
			NBTTagList list = new NBTTagList();
			list.appendTag(new NBTTagDouble(playerIn.posX));
			list.appendTag(new NBTTagDouble(playerIn.posY));
			list.appendTag(new NBTTagDouble(playerIn.posZ));
			is.getTagCompound().setTag("LastTeleportPos", list);
			
			worldIn.playSound(playerIn, targetX, targetY, targetZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
			playerIn.fallDistance = 0;
			
			if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP) {
				((EntityPlayerMP)playerIn).connection.setPlayerLocation(targetX, targetY, targetZ, playerIn.rotationYawHead, playerIn.rotationPitch);
			}
			return ActionResult.newResult(EnumActionResult.SUCCESS, is);
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		boolean closed = player.getHeldItem(hand).getItemDamage() == 1;
		if (world.isRemote) return EnumActionResult.SUCCESS;
		if (closed) {
			if (te instanceof TileEntityController) {
				if (((TileEntityController) te).isPowered()) {
					((TileEntityController) te).booting = true;
					((TileEntityController) te).bootTicks = 95;
					playSuccessEffect(player, world, pos);
				} else {
					playFailureEffect(player, world, pos);
				}
			} else if (te instanceof TileEntityMicrowaveBeam) {
				Beam b = CorrelatedWorldData.getFor(world).getWirelessManager().getBeam(pos);
				if (b != null) {
					for (BlockPos bp : b.beamMutable()) {
						new AddCaltropMessage(bp.getX()+0.5, bp.getY()+0.5, bp.getZ()+0.5, 1f).sendTo(player);
					}
					playSuccessEffect(player, world, pos);
				} else {
					playFailureEffect(player, world, pos);
				}
			}
		} else {
			if (te instanceof TileEntityOpticalTransceiver) {
				Optical o = CorrelatedWorldData.getFor(world).getWirelessManager().getOptical(pos);
				if (o != null) {
					new AddGlobeMessage(o.getX(), o.getY(), o.getZ(), (float)o.getRadius()).sendTo(player);
					playSuccessEffect(player, world, pos);
				} else {
					playFailureEffect(player, world, pos);
				}
			} else if (te instanceof TileEntityMicrowaveBeam) {
				Beam b = CorrelatedWorldData.getFor(world).getWirelessManager().getBeam(pos);
				if (b != null) {
					new AddLineMessage(b.getStart().getX()+0.5, b.getStart().getY()+0.5, b.getStart().getZ()+0.5, b.getEnd().getX()+0.5, b.getEnd().getY()+0.5, b.getEnd().getZ()+0.5).sendTo(player);
					for (BlockPos bp : b.getObstructions()) {
						new AddCaltropMessage(bp.getX()+0.5, bp.getY()+0.5, bp.getZ()+0.5, 1f).sendTo(player);
					}
					playSuccessEffect(player, world, pos);
				} else {
					playFailureEffect(player, world, pos);
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (stack.getItemDamage() == 0) {
			if (entity instanceof EntityPlayer) {
				if (!player.world.isRemote) {
					player.world.newExplosion(player, player.posX, player.posY, player.posZ, 1, false, false);
					player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
				}
			} else {
				entity.playSound(CSoundEvents.CONVERT, 1f, 0.5f);
				if (player.world instanceof WorldServer) {
					((WorldServer)player.world).spawnParticle(EnumParticleTypes.REDSTONE, entity.posX, entity.posY+(entity.height/2), entity.posZ, 512, entity.width/2, entity.height/2, entity.width/2, 1000);
				}
				entity.setDead();
			}
		} else {
			entity.attackEntityFrom(DamageSource.causePlayerDamage(player), 9001);
		}
		return true;
	}

	private void playFailureEffect(EntityPlayer player, World world, BlockPos pos) {
		world.playSound(null, pos, SoundEvents.BLOCK_NOTE_PLING, SoundCategory.PLAYERS, 1f, 0.5f);
		if (world instanceof WorldServer) {
			((WorldServer)world).spawnParticle(EnumParticleTypes.SPELL_INSTANT, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 24, 0.5, 0.5, 0.5, 0);
		}
	}
	
	private void playSuccessEffect(EntityPlayer player, World world, BlockPos pos) {
		world.playSound(null, pos, SoundEvents.BLOCK_NOTE_PLING, SoundCategory.PLAYERS, 1f, 2f);
		if (world instanceof WorldServer) {
			((WorldServer)world).spawnParticle(EnumParticleTypes.SPELL_WITCH, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 24, 0.5, 0.5, 0.5, 0);
		}
	}
	
	public static RayTraceResult rayTraceBlocksFar(World world, Vec3d vec31,
			Vec3d vec32, boolean stopOnLiquid,
			boolean ignoreBlockWithoutBoundingBox,
			boolean returnLastUncollidableBlock) {
		if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y)
				&& !Double.isNaN(vec31.z)) {
			if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y)
					&& !Double.isNaN(vec32.z)) {
				int i = MathHelper.floor(vec32.x);
				int j = MathHelper.floor(vec32.y);
				int k = MathHelper.floor(vec32.z);
				int l = MathHelper.floor(vec31.x);
				int i1 = MathHelper.floor(vec31.y);
				int j1 = MathHelper.floor(vec31.z);
				BlockPos blockpos = new BlockPos(l, i1, j1);
				IBlockState iblockstate = world.getBlockState(blockpos);
				Block block = iblockstate.getBlock();

				if ((!ignoreBlockWithoutBoundingBox
						|| iblockstate.getCollisionBoundingBox(world,
								blockpos) != Block.NULL_AABB)
						&& block.canCollideCheck(iblockstate, stopOnLiquid)) {
					RayTraceResult raytraceresult = iblockstate
							.collisionRayTrace(world, blockpos, vec31, vec32);

					if (raytraceresult != null) {
						return raytraceresult;
					}
				}

				RayTraceResult raytraceresult2 = null;
				int k1 = 32768;

				while (k1-- >= 0) {
					if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y)
							|| Double.isNaN(vec31.z)) {
						return null;
					}

					if (l == i && i1 == j && j1 == k) {
						return returnLastUncollidableBlock ? raytraceresult2
								: null;
					}

					boolean flag2 = true;
					boolean flag = true;
					boolean flag1 = true;
					double d0 = 999.0D;
					double d1 = 999.0D;
					double d2 = 999.0D;

					if (i > l) {
						d0 = l + 1.0D;
					} else if (i < l) {
						d0 = l + 0.0D;
					} else {
						flag2 = false;
					}

					if (j > i1) {
						d1 = i1 + 1.0D;
					} else if (j < i1) {
						d1 = i1 + 0.0D;
					} else {
						flag = false;
					}

					if (k > j1) {
						d2 = j1 + 1.0D;
					} else if (k < j1) {
						d2 = j1 + 0.0D;
					} else {
						flag1 = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
					double d6 = vec32.x - vec31.x;
					double d7 = vec32.y - vec31.y;
					double d8 = vec32.z - vec31.z;

					if (flag2) {
						d3 = (d0 - vec31.x) / d6;
					}

					if (flag) {
						d4 = (d1 - vec31.y) / d7;
					}

					if (flag1) {
						d5 = (d2 - vec31.z) / d8;
					}

					if (d3 == -0.0D) {
						d3 = -1.0E-4D;
					}

					if (d4 == -0.0D) {
						d4 = -1.0E-4D;
					}

					if (d5 == -0.0D) {
						d5 = -1.0E-4D;
					}

					EnumFacing enumfacing;

					if (d3 < d4 && d3 < d5) {
						enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
						vec31 = new Vec3d(d0, vec31.y + d7 * d3,
								vec31.z + d8 * d3);
					} else if (d4 < d5) {
						enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
						vec31 = new Vec3d(vec31.x + d6 * d4, d1,
								vec31.z + d8 * d4);
					} else {
						enumfacing = k > j1 ? EnumFacing.NORTH
								: EnumFacing.SOUTH;
						vec31 = new Vec3d(vec31.x + d6 * d5,
								vec31.y + d7 * d5, d2);
					}

					l = MathHelper.floor(vec31.x)
							- (enumfacing == EnumFacing.EAST ? 1 : 0);
					i1 = MathHelper.floor(vec31.y)
							- (enumfacing == EnumFacing.UP ? 1 : 0);
					j1 = MathHelper.floor(vec31.z)
							- (enumfacing == EnumFacing.SOUTH ? 1 : 0);
					blockpos = new BlockPos(l, i1, j1);
					IBlockState iblockstate1 = world.getBlockState(blockpos);
					Block block1 = iblockstate1.getBlock();

					if (!ignoreBlockWithoutBoundingBox
							|| iblockstate1.getMaterial() == Material.PORTAL
							|| iblockstate1.getCollisionBoundingBox(world,
									blockpos) != Block.NULL_AABB) {
						if (block1.canCollideCheck(iblockstate1,
								stopOnLiquid)) {
							RayTraceResult raytraceresult1 = iblockstate1
									.collisionRayTrace(world, blockpos, vec31,
											vec32);

							if (raytraceresult1 != null) {
								return raytraceresult1;
							}
						} else {
							raytraceresult2 = new RayTraceResult(
									RayTraceResult.Type.MISS, vec31, enumfacing,
									blockpos);
						}
					}
				}

				return returnLastUncollidableBlock ? raytraceresult2 : null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
}
