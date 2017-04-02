package com.elytradev.correlated.tile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.block.BlockTerminal;
import com.elytradev.correlated.compat.probe.UnitPotential;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.network.ChangeAPNMessage;
import com.elytradev.correlated.storage.CompoundDigitalStorage;
import com.elytradev.correlated.storage.IDigitalStorage;
import com.elytradev.correlated.storage.ITerminal;
import com.elytradev.correlated.storage.SimpleUserPreferences;
import com.elytradev.correlated.storage.UserPreferences;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.elytradev.correlated.wifi.Station;
import com.elytradev.correlated.wifi.WirelessManager;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.impl.ProbeData;

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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityTerminal extends TileEntityEnergyAcceptor implements ITickable, IInventory, ITerminal, ISidedInventory, IWirelessClient {
	private Map<UUID, SimpleUserPreferences> preferences = Maps.newHashMap();
	private String error;
	private String apn;
	
	@Override
	public void update() {
		if (hasWorld() && !world.isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == CBlocks.TERMINAL) {
				boolean lit;
				if (hasController()) {
					lit = getController().isPowered();
				} else {
					lit = getPotentialStored() > getPotentialConsumedPerTick();
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
			} else {
				modifyEnergyStored(-getPotentialConsumedPerTick());
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
		CNetwork.sendUpdatePacket(this);
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
	public int getPotentialConsumedPerTick() {
		return CConfig.terminalPUsage;
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
		if (apn != null) {
			compound.setString("APN", apn);
		}
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
		if (compound.hasKey("APN", NBT.TAG_STRING)) {
			apn = compound.getString("APN");
		} else {
			apn = null;
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
		if (hasController()) return true;
		return getStorage() != null;
	}

	@Override
	public IDigitalStorage getStorage() {
		if (hasController()) return getController();
		if (apn == null) return null;
		Iterable<Station> li = CorrelatedWorldData.getFor(world).getWirelessManager().allStationsInChunk(world.getChunkFromBlockCoords(getPosition()));
		for (Station s : li) {
			if (s.getAPNs().contains(apn) && s.isInRange(getX(), getY(), getZ())) {
				List<IDigitalStorage> storages = s.getStorages(apn);
				if (storages.size() == 1) {
					return storages.get(0);
				} else {
					return new CompoundDigitalStorage(storages);
				}
			}
		}
		return null;
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
		return hasController() ? getController().isPowered() : getPotentialStored() > getPotentialConsumedPerTick();
	}

	@Override
	public void markUnderlyingStorageDirty() {
		markDirty();
	}

	@Override
	public boolean isEmpty() {
		return inv.isEmpty();
	}
	
	@Override
	public int getSignalStrength() {
		if (hasController()) return -1;
		WirelessManager wm = CorrelatedWorldData.getFor(getWorld()).getWirelessManager();
		return wm.getSignalStrength(getX(), getY(), getZ(), getAPN());
	}
	
	@Override
	public void setAPN(String apn) {
		if (world.isRemote) {
			new ChangeAPNMessage(getPos(), apn == null ? Collections.emptyList() : Collections.singleton(apn)).sendToServer();
		} else {
			this.apn = apn;
		}
	}
	
	@Override
	public String getAPN() {
		return this.apn;
	}
	
	@Override
	public void setAPNs(Set<String> apn) {
		if (apn.size() > 1) throw new IllegalArgumentException("Only supports 1 APN");
		this.apn = apn.isEmpty() ? null : apn.iterator().next();
	}

	@Override
	public Set<String> getAPNs() {
		return this.apn == null ? Collections.emptySet() : Collections.singleton(this.apn);
	}
	
	@Override
	public double getX() {
		return getPos().getX()+0.5;
	}
	
	@Override
	public double getY() {
		return getPos().getY()+0.5;
	}
	
	@Override
	public double getZ() {
		return getPos().getZ()+0.5;
	}
	
	@Override
	public BlockPos getPosition() {
		return getPos();
	}
	
	@Override
	public int getMaxPotential() {
		return getPotentialConsumedPerTick()*40;
	}

	@Override
	public int getReceiveCap() {
		return getPotentialConsumedPerTick()*2;
	}

	@Override
	public boolean canReceivePotential() {
		return !hasController();
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
			if (hasController()) return;
			data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.energy_stored"))
					.withBar(0, getPotentialStored(), getMaxPotential(), UnitPotential.INSTANCE));
		}
	}

}
