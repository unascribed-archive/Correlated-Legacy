package com.unascribed.correlatedpotentialistics.block.item;

import java.util.List;

import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessTransmitter;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemBlockWirelessEndpoint extends ItemBlock {

	public ItemBlockWirelessEndpoint(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.format("tooltip.correlatedpotentialistics.rf_usage", 24));
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return stack.getItemDamage() == 0 ? 
				"tile.correlatedpotentialistics.wireless_receiver" :
				"tile.correlatedpotentialistics.wireless_transmitter";
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (stack.getMetadata() == 0) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityWirelessTransmitter) {
				TileEntityWirelessTransmitter tewt = (TileEntityWirelessTransmitter)te;
				if (!stack.hasTagCompound()) {
					stack.setTagCompound(new NBTTagCompound());
				}
				stack.getTagCompound().setLong("TransmitterUUIDMost", tewt.getId().getMostSignificantBits());
				stack.getTagCompound().setLong("TransmitterUUIDLeast", tewt.getId().getLeastSignificantBits());
				if (world.isRemote) {
					player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.receiver_linked"));
				}
				return true;
			}
			if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("TransmitterUUIDMost")) {
				if (world.isRemote) {
					player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.receiver_unlinked"));
				}
				return false;
			}
		}
		return super.onItemUse(stack, player, world, pos, side, hitX, hitY, hitZ);
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
