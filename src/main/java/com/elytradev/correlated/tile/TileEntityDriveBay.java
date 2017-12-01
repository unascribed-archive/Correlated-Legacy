package com.elytradev.correlated.tile;

import java.util.Iterator;
import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockDriveBay;
import com.elytradev.correlated.helper.ItemStacks;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.item.ItemDrive;
import com.google.common.collect.Iterators;

import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.UnitDictionary;
import com.elytradev.probe.api.impl.ProbeData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import java.util.Arrays;

public class TileEntityDriveBay extends TileEntityNetworkMember implements ITickable, Iterable<ItemStack> {

	private ItemStack[] drives = new ItemStack[8];
	private double consumedPerTick = CConfig.driveBayPUsage;
	
	public TileEntityDriveBay() {
		Arrays.fill(drives, ItemStack.EMPTY);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		for (int i = 0; i < drives.length; i++) {
			NBTTagCompound drive = new NBTTagCompound();
			if (drives[i] != null) {
				drives[i].writeToNBT(drive);
			}
			compound.setTag("Drive"+i, drive);
		}
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		for (int i = 0; i < drives.length; i++) {
			if (compound.hasKey("Drive"+i)) {
				NBTTagCompound drive = compound.getCompoundTag("Drive"+i);
				if (drive.hasNoTags()) {
					drives[i] = ItemStack.EMPTY;
				} else {
					ItemStack is = new ItemStack(drive);
					if (hasWorld() && world.isRemote) {
						ItemStacks.ensureHasTag(is);
						is.setTagCompound(is.getTagCompound().copy());
						is.getTagCompound().setBoolean("Dirty", true);
					}
					drives[i] = is;
				}
			}
		}
		onDriveChange();
	}

	@Override
	public double getPotentialConsumedPerTick() {
		return consumedPerTick;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		for (int i = 0; i < drives.length; i++) {
			ItemStack drive = drives[i];
			if (!(drive.getItem() instanceof ItemDrive)) continue;
			ItemDrive id = ((ItemDrive)drive.getItem());
			ItemStack prototype = drive.copy();
			ItemStacks.ensureHasTag(prototype).getTagCompound().setInteger("UsedBits", id.getKilobitsUsed(drive));
			ItemStacks.ensureHasTag(prototype).getTagCompound().removeTag("Data");
			nbt.setTag("Drive"+i, prototype.serializeNBT());
		}
		return nbt;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound nbt) {
		if (nbt.getBoolean("UpdateUsedBits")) {
			int slot = nbt.getInteger("Slot");
			ItemStack stack = drives[slot];
			if (!stack.isEmpty()) {
				ItemStacks.ensureHasTag(stack).getTagCompound().setBoolean("Dirty", true);
				ItemStacks.ensureHasTag(stack).getTagCompound().setInteger("UsedBits", nbt.getInteger("UsedBits"));
			}
		} else {
			for (int i = 0; i < drives.length; i++) {
				if (nbt.hasKey("Drive"+i)) {
					NBTTagCompound tag = nbt.getCompoundTag("Drive"+i);
					if (tag.hasNoTags()) {
						drives[i] = ItemStack.EMPTY;
					} else {
						drives[i] = new ItemStack(tag);
					}
				}
			}
		}
	}

	@Override
	public void update() {
		if (hasWorld() && !world.isRemote) {
			for (int i = 0; i < 8; i++) {
				ItemStack is = drives[i];
				if (is.isEmpty()) continue;
				if (ItemStacks.getBoolean(is, "Dirty").or(false)) {
					is.getTagCompound().removeTag("Dirty");
					markDirty();
					blinkDriveInSlot(i);
				}
			}
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == CBlocks.DRIVE_BAY) {
				boolean lit;
				if (hasController() && getController().isPowered()) {
					lit = true;
				} else {
					lit = false;
				}
				if (lit != state.getValue(BlockDriveBay.LIT)) {
					getWorld().setBlockState(getPos(), state.withProperty(BlockDriveBay.LIT, lit));
				}
			}
		}
	}

	public void blinkDriveInSlot(int slot) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("UpdateUsedBits", true);
		ItemStack drive = drives[slot];
		if (!(drive.getItem() instanceof ItemDrive)) return;
		ItemDrive id = ((ItemDrive)drive.getItem());
		nbt.setInteger("Slot", slot);
		nbt.setInteger("UsedBits", id.getKilobitsUsed(drive));
		CNetwork.sendUpdatePacket(this, nbt);
	}
	
	public void setDriveInSlot(int slot, ItemStack drive) {
		drives[slot] = drive;
		if (hasWorld() && !world.isRemote && world instanceof WorldServer) {
			NBTTagCompound nbt = new NBTTagCompound();
			if (drive.getItem() instanceof ItemDrive) {
				ItemStack prototype = drive.copy();
				ItemStacks.ensureHasTag(prototype).getTagCompound().setInteger("UsedBits", ((ItemDrive)drive.getItem()).getKilobitsUsed(drive));
				ItemStacks.ensureHasTag(prototype).getTagCompound().removeTag("Data");
				nbt.setTag("Drive"+slot, prototype.serializeNBT());
			} else {
				nbt.setTag("Drive"+slot, new NBTTagCompound());
			}
			CNetwork.sendUpdatePacket(this, nbt); 
			onDriveChange();
		}
	}

	private void onDriveChange() {
		double old = consumedPerTick;
		consumedPerTick = CConfig.driveBayPUsage;
		for (ItemStack is : drives) {
			if (is.getItem() instanceof ItemDrive) {
				consumedPerTick += ((ItemDrive)is.getItem()).getPotentialConsumptionRate(is);
			}
		}
		if (hasWorld() && !world.isRemote && hasController()) {
			getController().updateConsumptionRate(consumedPerTick-old);
			getController().updateDrivesCache();
		}
	}

	public ItemStack getDriveInSlot(int slot) {
		return drives[slot];
	}

	public boolean hasDriveInSlot(int slot) {
		return !drives[slot].isEmpty();
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return Iterators.filter(Iterators.forArray(drives), (is) -> is != null && is.getItem() instanceof ItemDrive);
	}

	public void clear() {
		Arrays.fill(drives, ItemStack.EMPTY);
		onDriveChange();
	}
	
	private Object probeCapability;
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == null) return null;
		if (capability == Correlated.PROBE) {
			if (probeCapability == null) probeCapability = new ProbeCapability();
			return (T)probeCapability;
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == null) return false;
		if (capability == Correlated.PROBE) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	private final class ProbeCapability implements IProbeDataProvider {
		@Override
		public void provideProbeData(List<IProbeData> data) {
			double storage = 0;
			double maxStorage = 0;
			for (ItemStack drive : drives) {
				if (drive != null && drive.getItem() instanceof ItemDrive) {
					ItemDrive id = (ItemDrive)drive.getItem();
					storage += (id.getKilobitsUsed(drive)/8D)*1024;
					maxStorage += (id.getMaxKilobits(drive)/8D)*1024;
				}
			}
			data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.storage"))
					.withBar(0, storage, maxStorage, UnitDictionary.BYTES));
		}
	}

}
