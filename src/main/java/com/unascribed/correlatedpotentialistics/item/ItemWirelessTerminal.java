package com.unascribed.correlatedpotentialistics.item;

import java.util.UUID;

import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.CoPoWorldData.Transmitter;
import com.unascribed.correlatedpotentialistics.tile.TileEntityController;
import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessTransmitter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemWirelessTerminal extends Item {
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote && getTransmitter(stack, world, player, true) != null) {
			player.openGui(CoPo.inst, 3, world, player.inventory.currentItem, 0, 0);
		}
		return super.onItemRightClick(stack, world, player);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityWirelessTransmitter) {
			TileEntityWirelessTransmitter tewt = (TileEntityWirelessTransmitter)te;
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setLong("TransmitterUUIDMost", tewt.getId().getMostSignificantBits());
			stack.getTagCompound().setLong("TransmitterUUIDLeast", tewt.getId().getLeastSignificantBits());
			if (world.isRemote) {
				player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.terminal_linked"));
			}
			return true;
		}
		return super.onItemUse(stack, player, world, pos, side, hitX, hitY, hitZ);
	}
	
	public Transmitter getTransmitter(ItemStack stack, World world, EntityPlayer player, boolean sendMessages) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("TransmitterUUIDMost")) {
			if (sendMessages) player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.terminal_unlinked"));
			return null;
		}
		UUID uuid = new UUID(stack.getTagCompound().getLong("TransmitterUUIDMost"), stack.getTagCompound().getLong("TransmitterUUIDLeast"));
		Transmitter t = CoPo.getDataFor(world).getTransmitterById(uuid);
		if (t == null) {
			if (sendMessages) player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.terminal_cantconnect"));
			return null;
		}
		if (t.position.distanceSq(player.posX, player.posY, player.posZ) > t.range*t.range) {
			if (sendMessages) player.addChatMessage(new ChatComponentTranslation("msg.correlatedpotentialistics.terminal_outofrange"));
			return null;
		}
		return t;
	}
	
	public TileEntityController getTransmitterController(ItemStack stack, World world, EntityPlayer player) {
		Transmitter t = getTransmitter(stack, world, player, false);
		TileEntity te = world.getTileEntity(t.position);
		if (te != null && te instanceof TileEntityWirelessTransmitter) {
			return ((TileEntityWirelessTransmitter)te).getController();
		}
		return null;
	}
}
