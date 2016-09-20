package io.github.elytra.copo.tile;

import java.util.Iterator;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.block.BlockDriveBay;
import io.github.elytra.copo.helper.ItemStacks;
import io.github.elytra.copo.item.ItemDrive;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import scala.actors.threadpool.Arrays;

public class TileEntityDriveBay extends TileEntityNetworkMember implements ITickable, Iterable<ItemStack> {

	private ItemStack[] drives = new ItemStack[8];
	private int consumedPerTick = CoPo.inst.driveBayRfUsage;

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
					drives[i] = null;
				} else {
					ItemStack is = ItemStack.loadItemStackFromNBT(drive);
					if (hasWorldObj() && worldObj.isRemote) {
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
	public int getEnergyConsumedPerTick() {
		return consumedPerTick;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", getPos().getX());
		nbt.setInteger("y", getPos().getY());
		nbt.setInteger("z", getPos().getZ());
		for (int i = 0; i < drives.length; i++) {
			ItemStack drive = drives[i];
			if (drive == null || !(drive.getItem() instanceof ItemDrive)) continue;
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
		if (pkt.getNbtCompound().getBoolean("UpdateUsedBits")) {
			int slot = pkt.getNbtCompound().getInteger("Slot");
			ItemStack stack = drives[slot];
			if (stack != null) {
				ItemStacks.ensureHasTag(stack).getTagCompound().setBoolean("Dirty", true);
				ItemStacks.ensureHasTag(stack).getTagCompound().setInteger("UsedBits", pkt.getNbtCompound().getInteger("UsedBits"));
			}
		} else {
			for (int i = 0; i < drives.length; i++) {
				if (pkt.getNbtCompound().hasKey("Drive"+i)) {
					NBTTagCompound tag = pkt.getNbtCompound().getCompoundTag("Drive"+i);
					if (tag.hasNoTags()) {
						drives[i] = null;
					} else {
						drives[i] = ItemStack.loadItemStackFromNBT(tag);
					}
				}
			}
		}
	}

	@Override
	public void update() {
		if (hasWorldObj() && !worldObj.isRemote) {
			for (int i = 0; i < 8; i++) {
				ItemStack is = drives[i];
				if (is == null) continue;
				if (ItemStacks.getBoolean(is, "Dirty").or(false)) {
					is.getTagCompound().removeTag("Dirty");
					markDirty();
					blinkDriveInSlot(i);
				}
			}
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == CoPo.drive_bay) {
				boolean lit;
				if (hasStorage() && getStorage().isPowered()) {
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
		if (drive == null || !(drive.getItem() instanceof ItemDrive)) return;
		ItemDrive id = ((ItemDrive)drive.getItem());
		nbt.setInteger("Slot", slot);
		nbt.setInteger("UsedBits", id.getKilobitsUsed(drive));
		sendUpdatePacket(nbt);
	}
	
	public void setDriveInSlot(int slot, ItemStack drive) {
		drives[slot] = drive;
		if (hasWorldObj() && !worldObj.isRemote && worldObj instanceof WorldServer) {
			NBTTagCompound nbt = new NBTTagCompound();
			if (drive != null && drive.getItem() instanceof ItemDrive) {
				ItemStack prototype = drive.copy();
				ItemStacks.ensureHasTag(prototype).getTagCompound().setInteger("UsedBits", ((ItemDrive)drive.getItem()).getKilobitsUsed(drive));
				ItemStacks.ensureHasTag(prototype).getTagCompound().removeTag("Data");
				nbt.setTag("Drive"+slot, prototype.serializeNBT());
			} else {
				nbt.setTag("Drive"+slot, new NBTTagCompound());
			}
			sendUpdatePacket(nbt); 
			onDriveChange();
		}
	}

	private void sendUpdatePacket(NBTTagCompound nbt) {
		WorldServer ws = (WorldServer)worldObj;
		Chunk c = worldObj.getChunkFromBlockCoords(getPos());
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), nbt);
		for (EntityPlayerMP player : worldObj.getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue())) {
			if (ws.getPlayerChunkMap().isPlayerWatchingChunk(player, c.xPosition, c.zPosition)) {
				player.connection.sendPacket(packet);
			}
		}
	}

	private void onDriveChange() {
		int old = consumedPerTick;
		consumedPerTick = CoPo.inst.driveBayRfUsage;
		for (ItemStack is : drives) {
			if (is == null) continue;
			if (is.getItem() instanceof ItemDrive) {
				consumedPerTick += ((ItemDrive)is.getItem()).getRFConsumptionRate(is);
			}
		}
		if (hasWorldObj() && !worldObj.isRemote && hasStorage()) {
			getStorage().updateConsumptionRate(consumedPerTick-old);
			getStorage().updateDrivesCache();
		}
	}

	public ItemStack getDriveInSlot(int slot) {
		return drives[slot];
	}

	public boolean hasDriveInSlot(int slot) {
		return drives[slot] != null;
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return Iterators.filter(Iterators.forArray(drives), (is) -> is != null && is.getItem() instanceof ItemDrive);
	}

	public void clear() {
		Arrays.fill(drives, null);
		NBTTagCompound nbt = new NBTTagCompound();
		for (int i = 0; i < drives.length; i++) {
			nbt.setTag("Drive"+i, new NBTTagCompound());
		}
		sendUpdatePacket(nbt); 
		onDriveChange();
	}

}
