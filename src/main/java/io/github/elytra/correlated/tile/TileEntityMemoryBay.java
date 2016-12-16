package io.github.elytra.correlated.tile;

import com.google.common.base.Predicates;

import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.block.BlockMemoryBay;
import io.github.elytra.correlated.helper.ItemStacks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class TileEntityMemoryBay extends TileEntityNetworkMember implements ITickable {

	private ItemStack[] memory = new ItemStack[12];

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		for (int i = 0; i < memory.length; i++) {
			NBTTagCompound drive = new NBTTagCompound();
			if (memory[i] != null) {
				memory[i].writeToNBT(drive);
			}
			compound.setTag("Memory"+i, drive);
		}
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		for (int i = 0; i < memory.length; i++) {
			if (compound.hasKey("Memory"+i)) {
				NBTTagCompound drive = compound.getCompoundTag("Memory"+i);
				if (drive.hasNoTags()) {
					memory[i] = null;
				} else {
					ItemStack is = ItemStack.loadItemStackFromNBT(drive);
					if (hasWorldObj() && worldObj.isRemote) {
						ItemStacks.ensureHasTag(is);
						is.setTagCompound((NBTTagCompound)is.getTagCompound().copy());
						is.getTagCompound().setBoolean("Dirty", true);
					}
					memory[i] = is;
				}
			}
		}
		onMemoryChange();
	}

	@Override
	public long getEnergyConsumedPerTick() {
		return Correlated.inst.memoryBayRfUsage;
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
		for (int i = 0; i < memory.length; i++) {
			ItemStack stack = memory[i];
			if (stack == null) continue;
			nbt.setTag("Memory"+i, stack.serializeNBT());
		}
		return nbt;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		for (int i = 0; i < memory.length; i++) {
			if (pkt.getNbtCompound().hasKey("Memory"+i)) {
				NBTTagCompound tag = pkt.getNbtCompound().getCompoundTag("Memory"+i);
				if (tag.hasNoTags()) {
					memory[i] = null;
				} else {
					memory[i] = ItemStack.loadItemStackFromNBT(tag);
				}
			}
		}
	}

	@Override
	public void update() {
		if (hasWorldObj() && !worldObj.isRemote) {
			for (int i = 0; i < 12; i++) {
				ItemStack is = memory[i];
				if (is == null) continue;
			}
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == Correlated.memory_bay) {
				boolean lit;
				if (hasStorage() && getStorage().isPowered()) {
					lit = true;
				} else {
					lit = false;
				}
				if (lit != state.getValue(BlockMemoryBay.lit)) {
					getWorld().setBlockState(getPos(), state.withProperty(BlockMemoryBay.lit, lit));
				}
			}
		}
	}

	public void setMemoryInSlot(int slot, ItemStack mem) {
		memory[slot] = mem;
		if (hasWorldObj() && !worldObj.isRemote && worldObj instanceof WorldServer) {
			NBTTagCompound nbt = new NBTTagCompound();
			if (mem != null) {
				nbt.setTag("Memory"+slot, mem.serializeNBT());
			} else {
				nbt.setTag("Memory"+slot, new NBTTagCompound());
			}
			sendUpdatePacket(nbt); 
			onMemoryChange();
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

	private void onMemoryChange() {
		if (hasWorldObj() && !worldObj.isRemote && hasStorage()) {
			getStorage().updateMemoryCache();
		}
	}

	public ItemStack getMemoryInSlot(int slot) {
		return memory[slot];
	}

	public boolean hasMemoryInSlot(int slot) {
		return memory[slot] != null;
	}

}
