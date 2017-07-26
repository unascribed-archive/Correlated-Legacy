package com.elytradev.correlated.item;

import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.network.AddCaltropMessage;
import com.elytradev.correlated.network.AddGlobeMessage;
import com.elytradev.correlated.network.AddLineMessage;
import com.elytradev.correlated.tile.TileEntityController;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;
import com.elytradev.correlated.tile.TileEntityOpticalReceiver;
import com.elytradev.correlated.wifi.Beam;
import com.elytradev.correlated.wifi.Optical;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

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
	public FontRenderer getFontRenderer(ItemStack stack) {
		return Minecraft.getMinecraft().standardGalacticFontRenderer;
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		tooltip.add(I18n.translateToLocal("item.correlated.debugginator.hint"));
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		if (player.isSneaking()) {
			if (!player.world.isRemote) {
				RayTraceResult rtr = rayTrace(player.world, player, false);
				IBlockState bs = player.world.getBlockState(pos);
				ItemStack is = bs.getBlock().getPickBlock(bs, rtr, player.world, pos, player);
				EntityItem ei = new EntityItem(player.world, rtr.hitVec.xCoord, rtr.hitVec.yCoord, rtr.hitVec.zCoord);
				ei.setEntityItemStack(is);
				player.world.spawnEntity(ei);
			}
			return true;
		}
		return super.onBlockStartBreak(itemstack, pos, player);
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return false;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (isSelected && entityIn instanceof EntityPlayer) {
			((EntityPlayer)entityIn).getFoodStats().setFoodLevel(20);
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (playerIn.isSneaking()) {
			ItemStack is = playerIn.getHeldItem(handIn);
			Correlated.proxy.clearShapes();
			if (is.getItemDamage() == 0) {
				playerIn.playSound(SoundEvents.BLOCK_PISTON_CONTRACT, 1f, 2f);
				is.setItemDamage(1);
				EnchantmentHelper.setEnchantments(ImmutableMap.of(), is);
			} else if (is.getItemDamage() == 1) {
				playerIn.playSound(SoundEvents.BLOCK_PISTON_EXTEND, 1f, 2f);
				is.setItemDamage(0);
				EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SILK_TOUCH, 1), is);
			}
			return ActionResult.newResult(EnumActionResult.SUCCESS, is);
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
			double d3 = 2560;
			Vec3d vec3d1 = vec3d.addVector(f6 * d3, f5 * d3, f7 * d3);
			RayTraceResult rtr = worldIn.rayTraceBlocks(vec3d, vec3d1, true, !true, false);
			if (rtr != null) {
				worldIn.playSound(playerIn, rtr.hitVec.xCoord, rtr.hitVec.yCoord, rtr.hitVec.zCoord, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
				if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP) {
					((EntityPlayerMP)playerIn).connection.setPlayerLocation(rtr.hitVec.xCoord, rtr.hitVec.yCoord, rtr.hitVec.zCoord, playerIn.rotationYawHead, playerIn.rotationPitch);
				}
			}
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		boolean closed = player.getHeldItem(hand).getItemDamage() == 1;
		if (world.isRemote) return EnumActionResult.SUCCESS;
		if (closed) {
			if (te instanceof TileEntityController) {
				if (((TileEntityController) te).isPowered()) {
					((TileEntityController) te).scanNetwork();
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
			if (te instanceof TileEntityOpticalReceiver) {
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
	
}
