package io.github.elytra.copo.block.item;

import java.util.List;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.tile.TileEntityWirelessTransmitter;
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

public class ItemBlockWirelessEndpoint extends ItemBlock {

	public ItemBlockWirelessEndpoint(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.format("tooltip.correlatedpotentialistics.rf_usage", stack.getItemDamage() == 0 ? CoPo.inst.receiverRfUsage : CoPo.inst.transmitterRfUsage));
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return stack.getItemDamage() == 0 ? 
				"tile.correlatedpotentialistics.wireless_receiver" :
				"tile.correlatedpotentialistics.wireless_transmitter";
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
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
					player.addChatMessage(new TextComponentTranslation("msg.correlatedpotentialistics.receiver_linked"));
				}
				return EnumActionResult.FAIL;
			}
			if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("TransmitterUUIDMost")) {
				if (world.isRemote) {
					player.addChatMessage(new TextComponentTranslation("msg.correlatedpotentialistics.receiver_unlinked"));
				}
				return EnumActionResult.FAIL;
			}
		}
		return super.onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
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
