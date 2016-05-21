package io.github.elytra.copo.tile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.IVT;
import io.github.elytra.copo.block.BlockVT;
import io.github.elytra.copo.item.ItemDrive;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityVT extends TileEntityNetworkMember implements ITickable, IInventory, IVT {
	private Map<UUID, UserPreferences> preferences = Maps.newHashMap();

	@Override
	public void update() {
		if (hasWorldObj() && !worldObj.isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == CoPo.vt) {
				boolean lit;
				if (hasController() && getController().isPowered()) {
					lit = true;
				} else {
					lit = false;
				}
				if (lit != state.getValue(BlockVT.lit)) {
					getWorld().setBlockState(getPos(), state.withProperty(BlockVT.lit, lit));
				}
			}
			
			if (hasController()) {
				TileEntityController controller = getController();
				if (controller.isPowered() && !controller.error && !controller.booting && dumpDrive != null) {
					if (dumpDrive.getItem() instanceof ItemDrive) {
						ItemDrive id = (ItemDrive)dumpDrive.getItem();
						List<ItemStack> prototypes = id.getPrototypes(dumpDrive);
						int moved = 0;
						while (moved < 100) {
							if (prototypes.isEmpty()) break;
							ItemStack prototype = prototypes.get(0);
							ItemStack split = id.removeItems(dumpDrive, prototype, prototype.getMaxStackSize());
							if (split.stackSize == 0) {
								prototypes.remove(0);
								continue;
							}
							controller.addItemToNetwork(split);
							moved += split.stackSize;
							if (split.stackSize > 0) {
								// no more room for this item in the network, skip it this tick
								prototypes.remove(0);
								moved -= split.stackSize;
								id.addItem(dumpDrive, split);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public int getEnergyConsumedPerTick() {
		return 4;
	}

	public UserPreferences getPreferences(UUID uuid) {
		if (!preferences.containsKey(uuid)) {
			preferences.put(uuid, new UserPreferences());
		}
		return preferences.get(uuid);
	}

	@Override
	public UserPreferences getPreferences(EntityPlayer player) {
		return getPreferences(player.getGameProfile().getId());
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagList prefs = new NBTTagList();
		for (Map.Entry<UUID, UserPreferences> en : preferences.entrySet()) {
			UserPreferences pref = en.getValue();
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("UUIDMost", en.getKey().getMostSignificantBits());
			data.setLong("UUIDLeast", en.getKey().getLeastSignificantBits());
			pref.writeToNBT(data);
			prefs.appendTag(data);
		}
		compound.setTag("Preferences", prefs);
		if (dumpDrive != null) {
			compound.setTag("DumpDrive", dumpDrive.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("DumpDrive", NBT.TAG_COMPOUND)) {
			dumpDrive = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("DumpDrive"));
		} else {
			dumpDrive = null;
		}
		NBTTagList prefs = compound.getTagList("Preferences", NBT.TAG_COMPOUND);
		for (int i = 0; i < prefs.tagCount(); i++) {
			UserPreferences pref = new UserPreferences();
			NBTTagCompound data = prefs.getCompoundTagAt(i);
			pref.readFromNBT(data);
			preferences.put(new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")), pref);
		}
	}

	private ItemStack dumpDrive;
	
	@Override
	public String getName() {
		return "container.vt";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return dumpDrive;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return dumpDrive.splitStack(count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index != 0) throw new IndexOutOfBoundsException();
		ItemStack swp = dumpDrive;
		dumpDrive = null;
		return swp;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException();
		dumpDrive = stack;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index != 0) throw new IndexOutOfBoundsException();
		return stack != null && stack.getItem() instanceof ItemDrive;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		dumpDrive = null;
	}

	@Override
	public boolean supportsDumpSlot() {
		return true;
	}

	@Override
	public IInventory getDumpSlotInventory() {
		return this;
	}

	@Override
	public boolean canContinueInteracting(EntityPlayer player) {
		return true;
	}
	
	@Override
	public void markUnderlyingStorageDirty() {
		markDirty();
	}

}
