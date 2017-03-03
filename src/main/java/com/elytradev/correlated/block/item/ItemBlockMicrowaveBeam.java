package com.elytradev.correlated.block.item;

import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;
import com.elytradev.correlated.wifi.Beam;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemBlockMicrowaveBeam extends ItemBlock {

	public ItemBlockMicrowaveBeam(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.format("tile.correlated.microwave_beam.0"));
		tooltip.add(I18n.format("tooltip.correlated.rf_usage", Correlated.inst.beamRfUsage));
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getMetadata() == 0) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityMicrowaveBeam) {
				if (!stack.hasTagCompound()) {
					stack.setTagCompound(new NBTTagCompound());
				}
				stack.getTagCompound().setLong("OtherSide", pos.toLong());
				if (world.isRemote) {
					player.sendMessage(new TextComponentTranslation("msg.correlated.position_saved"));
				}
				return EnumActionResult.SUCCESS;
			}
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("OtherSide")) {
				BlockPos other = BlockPos.fromLong(stack.getTagCompound().getLong("OtherSide"));
				Beam b = Correlated.getDataFor(world).getWirelessManager().getBeam(other);
				if (b != null) {
					player.sendMessage(new TextComponentTranslation("msg.correlated.beam_exists"));
					return EnumActionResult.FAIL;
				}
			}
		}
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public boolean getHasSubtypes() {
		return true;
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
}
