package io.github.elytra.correlated.item;

import java.util.UUID;

import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.CorrelatedWorldData.Transmitter;
import io.github.elytra.correlated.tile.TileEntityController;
import io.github.elytra.correlated.tile.TileEntityWirelessTransmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemWirelessTerminal extends Item {
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote && getTransmitter(stack, world, player, true) != null) {
			player.openGui(Correlated.inst, 3, world, player.inventory.currentItem, 0, 0);
			return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
		}
		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityWirelessTransmitter) {
			TileEntityWirelessTransmitter tewt = (TileEntityWirelessTransmitter)te;
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setLong("TransmitterUUIDMost", tewt.getId().getMostSignificantBits());
			stack.getTagCompound().setLong("TransmitterUUIDLeast", tewt.getId().getLeastSignificantBits());
			if (world.isRemote) {
				player.sendMessage(new TextComponentTranslation("msg.correlated.terminal_linked"));
			}
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public int getItemStackLimit(ItemStack is) {
		return 1;
	}
	
	public Transmitter getTransmitter(ItemStack stack, World world, EntityPlayer player, boolean sendMessages) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("TransmitterUUIDMost")) {
			if (sendMessages) player.sendMessage(new TextComponentTranslation("msg.correlated.terminal_unlinked"));
			return null;
		}
		UUID uuid = new UUID(stack.getTagCompound().getLong("TransmitterUUIDMost"), stack.getTagCompound().getLong("TransmitterUUIDLeast"));
		Transmitter t = Correlated.getDataFor(world).getTransmitterById(uuid);
		if (t == null) {
			if (sendMessages) player.sendMessage(new TextComponentTranslation("msg.correlated.terminal_cantconnect"));
			return null;
		}
		if (t.position.distanceSq(player.posX, player.posY, player.posZ) > t.range*t.range) {
			if (sendMessages) player.sendMessage(new TextComponentTranslation("msg.correlated.terminal_outofrange"));
			return null;
		}
		return t;
	}
	
	public TileEntityController getTransmitterController(ItemStack stack, World world, EntityPlayer player) {
		Transmitter t = getTransmitter(stack, world, player, false);
		if (t == null) return null;
		TileEntity te = world.getTileEntity(t.position);
		if (te != null && te instanceof TileEntityWirelessTransmitter) {
			return ((TileEntityWirelessTransmitter)te).getStorage();
		}
		return null;
	}
}
