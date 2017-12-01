package com.elytradev.correlated.tile;

import java.util.Arrays;
import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockMemoryBay;
import com.elytradev.correlated.helper.ItemStacks;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.item.ItemMemory;
import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.UnitDictionary;
import com.elytradev.probe.api.impl.ProbeData;
import com.google.common.base.Predicates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityMemoryBay extends TileEntityNetworkMember implements ITickable {

	private ItemStack[] memory = new ItemStack[12];

	public TileEntityMemoryBay() {
		Arrays.fill(memory, ItemStack.EMPTY);
	}
	
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
					memory[i] = ItemStack.EMPTY;
				} else {
					ItemStack is = new ItemStack(drive);
					if (hasWorld() && world.isRemote) {
						ItemStacks.ensureHasTag(is);
						is.setTagCompound(is.getTagCompound().copy());
						is.getTagCompound().setBoolean("Dirty", true);
					}
					memory[i] = is;
				}
			}
		}
		onMemoryChange();
	}

	@Override
	public double getPotentialConsumedPerTick() {
		return CConfig.memoryBayPUsage;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		for (int i = 0; i < memory.length; i++) {
			ItemStack stack = memory[i];
			if (stack.isEmpty()) continue;
			nbt.setTag("Memory"+i, stack.serializeNBT());
		}
		return nbt;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound nbt) {
		for (int i = 0; i < memory.length; i++) {
			if (nbt.hasKey("Memory"+i)) {
				NBTTagCompound tag = nbt.getCompoundTag("Memory"+i);
				if (tag.hasNoTags()) {
					memory[i] = ItemStack.EMPTY;
				} else {
					memory[i] = new ItemStack(tag);
				}
			}
		}
	}

	@Override
	public void update() {
		if (hasWorld() && !world.isRemote) {
			IBlockState state = getWorld().getBlockState(getPos());
			if (state.getBlock() == CBlocks.MEMORY_BAY) {
				boolean lit;
				if (hasController() && getController().isPowered()) {
					lit = true;
				} else {
					lit = false;
				}
				if (lit != state.getValue(BlockMemoryBay.LIT)) {
					getWorld().setBlockState(getPos(), state.withProperty(BlockMemoryBay.LIT, lit));
				}
			}
		}
	}

	public void setMemoryInSlot(int slot, ItemStack mem) {
		memory[slot] = mem;
		if (hasWorld() && !world.isRemote && world instanceof WorldServer) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setTag("Memory"+slot, mem.serializeNBT());
			sendUpdatePacket(nbt); 
			onMemoryChange();
		}
	}

	private void sendUpdatePacket(NBTTagCompound nbt) {
		WorldServer ws = (WorldServer)world;
		Chunk c = world.getChunkFromBlockCoords(getPos());
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), nbt);
		for (EntityPlayerMP player : world.getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue())) {
			if (ws.getPlayerChunkMap().isPlayerWatchingChunk(player, c.x, c.z)) {
				player.connection.sendPacket(packet);
			}
		}
	}

	private void onMemoryChange() {
		if (hasWorld() && !world.isRemote && hasController()) {
			getController().updateMemoryCache();
		}
	}

	public ItemStack getMemoryInSlot(int slot) {
		return memory[slot];
	}

	public boolean hasMemoryInSlot(int slot) {
		return !memory[slot].isEmpty();
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
			double max = 0;
			for (ItemStack mem : memory) {
				if (mem != null && mem.getItem() instanceof ItemMemory) {
					ItemMemory im = (ItemMemory)mem.getItem();
					max += (im.getMaxBits(mem)/8D);
				}
			}
			data.add(new ProbeData(new TextComponentTranslation("tooltip.correlated.memory"))
					.withBar(0, max, max, UnitDictionary.BYTES));
		}
	}

}
