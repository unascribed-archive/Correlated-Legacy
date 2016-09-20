package io.github.elytra.copo.tile;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.IDigitalStorage;
import io.github.elytra.copo.IVT;
import io.github.elytra.copo.inventory.ContainerVT.SortMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityImporterChest extends TileEntity implements IInventory, ISidedInventory, IDigitalStorage, ITickable, IVT {
	private List<ItemStack> storage = Lists.newArrayList();
	private int changeId;
	
	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}
	
	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}
	
	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[0];
	}
	
	@Override
	public String getName() {
		return "importer_chest";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return storage.size();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return storage.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack is = storage.get(index);
		ItemStack split = is.splitStack(count);
		if (is.stackSize <= 0) {
			storage.remove(index);
		}
		changeId++;
		return split;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack is = storage.remove(index);
		changeId++;
		return is;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index == storage.size()) {
			storage.add(stack);
		} else {
			storage.set(index, stack);
		}
		changeId++;
	}

	@Override
	public int getInventoryStackLimit() {
		return Integer.MAX_VALUE;
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
		return false;
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
		storage.clear();
	}

	@Override
	public int getChangeId() {
		return changeId;
	}

	@Override
	public List<ItemStack> getTypes() {
		return storage;
	}

	@Override
	public ItemStack addItemToNetwork(ItemStack stack) {
		if (stack == null) return null;
		for (ItemStack is : storage) {
			if (areCompatible(is, stack)) {
				is.stackSize += stack.stackSize;
				stack.stackSize = 0;
				return null;
			}
		}
		storage.add(stack.copy());
		changeId++;
		return null;
	}

	@Override
	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean checkInterfaces) {
		if (prototype == null) return null;
		ItemStack res = prototype.copy();
		res.stackSize = 0;
		Iterator<ItemStack> iter = storage.iterator();
		while (iter.hasNext()) {
			ItemStack is = iter.next();
			if (areCompatible(is, res)) {
				int toTake = Math.min(amount, is.stackSize);
				amount -= toTake;
				res.stackSize += toTake;
				is.stackSize -= toTake;
				if (is.stackSize <= 0) {
					iter.remove();
				}
				if (amount <= 0) break;
			}
		}
		changeId++;
		return res.stackSize <= 0 ? null : res;
	}

	@Override
	public boolean isPowered() {
		return true;
	}

	@Override
	public int getKilobitsStorageFree() {
		return 0;
	}
	
	private boolean areCompatible(ItemStack a, ItemStack b) {
		return     a != null
				&& a.getItem() == b.getItem()
				&& (!a.getHasSubtypes() || a.getMetadata() == b.getMetadata())
				&& ItemStack.areItemStackTagsEqual(a, b)
				&& a.areCapsCompatible(b)
				&& a.isStackable();
	}
	
	public float lidAngle;
	public float prevLidAngle;
	public int numPlayersUsing;
	private int ticksSinceSync;

	@Override
	public void update() {
		if (++this.ticksSinceSync % 20 * 4 == 0) {
			this.worldObj.addBlockEvent(this.pos, CoPo.importer_chest, 1,
					this.numPlayersUsing);
		}

		this.prevLidAngle = this.lidAngle;
		int i = this.pos.getX();
		int j = this.pos.getY();
		int k = this.pos.getZ();

		if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
			double d0 = i + 0.5D;
			double d1 = k + 0.5D;
			this.worldObj.playSound((EntityPlayer) null, d0, j + 0.5D,
					d1, SoundEvents.BLOCK_ENDERCHEST_OPEN, SoundCategory.BLOCKS,
					0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
		}

		if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F
				|| this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
			float f2 = this.lidAngle;

			if (this.numPlayersUsing > 0) {
				this.lidAngle += 0.1F;
			} else {
				this.lidAngle -= 0.1F;
			}

			if (this.lidAngle > 1.0F) {
				this.lidAngle = 1.0F;
			}

			if (this.lidAngle < 0.5F && f2 >= 0.5F) {
				double d3 = i + 0.5D;
				double d2 = k + 0.5D;
				this.worldObj.playSound((EntityPlayer) null, d3,
						j + 0.5D, d2,
						SoundEvents.BLOCK_ENDERCHEST_CLOSE,
						SoundCategory.BLOCKS, 0.5F,
						this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			}

			if (this.lidAngle < 0.0F) {
				this.lidAngle = 0.0F;
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			numPlayersUsing = type;
			return true;
		} else {
			return super.receiveClientEvent(id, type);
		}
	}

	@Override
	public void invalidate() {
		updateContainingBlockInfo();
		super.invalidate();
	}

	public void openChest() {
		numPlayersUsing++;
		worldObj.addBlockEvent(getPos(), CoPo.importer_chest, 1, numPlayersUsing);
	}

	public void closeChest() {
		numPlayersUsing--;
		worldObj.addBlockEvent(getPos(), CoPo.importer_chest, 1, numPlayersUsing);
	}

	@Override
	public UserPreferences getPreferences(EntityPlayer player) {
		UserPreferences prefs = new UserPreferences();
		prefs.sortMode = SortMode.NAME;
		prefs.sortAscending = true;
		return prefs;
	}

	@Override
	public IDigitalStorage getStorage() {
		return this;
	}

	@Override
	public boolean hasStorage() {
		return true;
	}

	@Override
	public boolean supportsDumpSlot() {
		return false;
	}

	@Override
	public IInventory getDumpSlotInventory() {
		return null;
	}

	@Override
	public boolean canContinueInteracting(EntityPlayer player) {
		return true;
	}

	@Override
	public void markUnderlyingStorageDirty() {
		markDirty();
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

}
