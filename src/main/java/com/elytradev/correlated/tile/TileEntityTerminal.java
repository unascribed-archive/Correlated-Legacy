package com.elytradev.correlated.tile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockTerminal;
import com.elytradev.correlated.storage.IDigitalStorage;
import com.elytradev.correlated.storage.ITerminal;
import com.elytradev.correlated.storage.SimpleUserPreferences;
import com.elytradev.correlated.storage.UserPreferences;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityTerminal extends TileEntityNetworkMember implements ITickable, IInventory, ITerminal, ISidedInventory {
	private Map<UUID, SimpleUserPreferences> preferences = Maps.newHashMap();
	private String error;
	
	@Override
	public void update() {
		if (hasWorld() && !world.isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == Correlated.terminal) {
				boolean lit;
				if (hasController() && getController().isPowered()) {
					lit = true;
				} else {
					lit = false;
				}
				if (lit != state.getValue(BlockTerminal.LIT)) {
					getWorld().setBlockState(getPos(), state = state.withProperty(BlockTerminal.LIT, lit));
				}
				boolean floppy = !getStackInSlot(1).isEmpty();
				if (floppy != state.getValue(BlockTerminal.FLOPPY)) {
					getWorld().setBlockState(getPos(), state = state.withProperty(BlockTerminal.FLOPPY, floppy));
				}
			}
			
			if (hasController()) {
				TileEntityController controller = getController();
				if (controller.error) {
					if (!Objects.equal(error, controller.errorReason)) {
						setError(controller.errorReason);
					}
				} else {
					setError(null);
				}
			}
		}
	}
	
	public boolean isErroring() {
		return error != null;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
		Correlated.sendUpdatePacket(this);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		if (error != null) nbt.setString("Error", error);
		return nbt;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound nbt) {
		error = nbt.hasKey("Error", NBT.TAG_STRING) ? nbt.getString("Error") : null;
	}
	
	@Override
	public int getEnergyConsumedPerTick() {
		return Correlated.inst.terminalRfUsage;
	}

	public UserPreferences getPreferences(UUID uuid) {
		if (!preferences.containsKey(uuid)) {
			preferences.put(uuid, new SimpleUserPreferences());
		}
		return preferences.get(uuid);
	}

	@Override
	public UserPreferences getPreferences(EntityPlayer player) {
		return getPreferences(player.getGameProfile().getId());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagList prefs = new NBTTagList();
		for (Map.Entry<UUID, SimpleUserPreferences> en : preferences.entrySet()) {
			SimpleUserPreferences pref = en.getValue();
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("UUIDMost", en.getKey().getMostSignificantBits());
			data.setLong("UUIDLeast", en.getKey().getLeastSignificantBits());
			pref.writeToNBT(data);
			prefs.appendTag(data);
		}
		compound.setTag("Preferences", prefs);
		NBTTagList invList = new NBTTagList();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack is = getStackInSlot(i);
			if (is.isEmpty()) continue;
			NBTTagCompound tag = is.writeToNBT(new NBTTagCompound());
			tag.setInteger("Slot", i);
			invList.appendTag(tag);
		}
		compound.setTag("Inventory", invList);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList prefs = compound.getTagList("Preferences", NBT.TAG_COMPOUND);
		for (int i = 0; i < prefs.tagCount(); i++) {
			SimpleUserPreferences pref = new SimpleUserPreferences();
			NBTTagCompound data = prefs.getCompoundTagAt(i);
			pref.readFromNBT(data);
			preferences.put(new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")), pref);
		}
		inv.clear();
		NBTTagList invList = compound.getTagList("Inventory", NBT.TAG_COMPOUND);
		for (int i = 0; i < invList.tagCount(); i++) {
			NBTTagCompound tag = invList.getCompoundTagAt(i);
			ItemStack is = new ItemStack(tag);
			int slot = tag.getInteger("Slot");
			setInventorySlotContents(slot, is);
		}
	}
	
	private InventoryBasic inv = new InventoryBasic("gui.correlated.terminal", false, 2);

	public void addInventoryChangeListener(IInventoryChangedListener listener) {
		inv.addInventoryChangeListener(listener);
	}

	public void removeInventoryChangeListener(IInventoryChangedListener listener) {
		inv.removeInventoryChangeListener(listener);
	}
	
	@Override
	public boolean hasStorage() {
		return hasController();
	}

	@Override
	public IDigitalStorage getStorage() {
		return getController();
	}
	
	@Override
	public ItemStack getStackInSlot(int index) {
		return inv.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return inv.decrStackSize(index, count);
	}

	public ItemStack addItem(ItemStack stack) {
		return inv.addItem(stack);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return inv.removeStackFromSlot(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		inv.setInventorySlotContents(index, stack);
	}

	@Override
	public int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public String getName() {
		return inv.getName();
	}

	@Override
	public boolean hasCustomName() {
		return inv.hasCustomName();
	}

	public void setCustomName(String inventoryTitleIn) {
		inv.setCustomName(inventoryTitleIn);
	}

	@Override
	public ITextComponent getDisplayName() {
		return inv.getDisplayName();
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public void markDirty() {
		inv.markDirty();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return inv.isUsableByPlayer(player);
	}

	@Override
	public void openInventory(EntityPlayer player) {
		inv.openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		inv.closeInventory(player);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return inv.isItemValidForSlot(index, stack);
	}

	@Override
	public int getField(int id) {
		return inv.getField(id);
	}

	@Override
	public void setField(int id, int value) {
		inv.setField(id, value);
	}

	@Override
	public int getFieldCount() {
		return inv.getFieldCount();
	}

	@Override
	public void clear() {
		inv.clear();
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean hasMaintenanceSlot() {
		return true;
	}
	
	@Override
	public void setMaintenanceSlotContent(ItemStack stack) {
		setInventorySlotContents(0, stack);
	}
	
	@Override
	public ItemStack getMaintenanceSlotContent() {
		return getStackInSlot(0);
	}

	@Override
	public boolean canContinueInteracting(EntityPlayer player) {
		return hasStorage() && getStorage().isPowered();
	}

	@Override
	public void markUnderlyingStorageDirty() {
		markDirty();
	}

	@Override
	public boolean isEmpty() {
		return inv.isEmpty();
	}
	
	// TODO implement wireless
	
	@Override
	public boolean allowAPNSelection() {
		return false;
	}
	
	@Override
	public int getSignalStrength() {
		return -1;
	}
	
	@Override
	public void setAPN(String apn) {
		
	}
	
	@Override
	public String getAPN() {
		return null;
	}
	
	@Override
	public BlockPos getPosition() {
		return getPos();
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
			// do nothing, just suppress the default behavior
		}
	}

}
