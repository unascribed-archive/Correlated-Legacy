package io.github.elytra.correlated;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.Constants.NBT;

public class CorrelatedWorldData extends WorldSavedData {
	public static final class Transmitter implements INBTSerializable<NBTTagCompound> {
		public UUID id;
		public BlockPos position;
		public double range;
		protected boolean valid = false;
		public Transmitter() {}
		public Transmitter(UUID id, BlockPos position) {
			this.id = id;
			this.position = position;
			this.range = Correlated.inst.defaultWirelessRange;
		}
		public Transmitter(UUID id, BlockPos position, double range) {
			this.id = id;
			this.position = position;
			this.range = range;
		}
		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			id = new UUID(nbt.getLong("UUIDMost"), nbt.getLong("UUIDLeast"));
			position = BlockPos.fromLong(nbt.getLong("Position"));
			range = nbt.getDouble("Range");
		}
		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setLong("UUIDMost", id.getMostSignificantBits());
			nbt.setLong("UUIDLeast", id.getLeastSignificantBits());
			nbt.setLong("Position", position.toLong());
			nbt.setDouble("Range", range);
			return nbt;
		}
		public boolean isValid() {
			return valid;
		}
	}
	
	private Map<UUID, Transmitter> transmitters = Maps.newHashMap();
	private Map<UUID, NBTTagCompound> playerRespawnData = Maps.newHashMap();
	
	public CorrelatedWorldData(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList tr = nbt.getTagList("Transmitters", NBT.TAG_COMPOUND);
		transmitters.clear();
		for (int i = 0; i < tr.tagCount(); i++) {
			Transmitter t = new Transmitter();
			t.deserializeNBT(tr.getCompoundTagAt(i));
			transmitters.put(t.id, t);
		}
		NBTTagList re = nbt.getTagList("PlayerRespawnData", NBT.TAG_COMPOUND);
		playerRespawnData.clear();
		for (int i = 0; i < re.tagCount(); i++) {
			NBTTagCompound tag = re.getCompoundTagAt(i);
			playerRespawnData.put(tag.getUniqueId("Id"), tag.getCompoundTag("Data"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList tr = new NBTTagList();
		for (Transmitter t : transmitters.values()) {
			tr.appendTag(t.serializeNBT());
		}
		nbt.setTag("Transmitters", tr);
		NBTTagList re = new NBTTagList();
		for (Map.Entry<UUID, NBTTagCompound> en : playerRespawnData.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setUniqueId("Id", en.getKey());
			tag.setTag("Data", en.getValue());
			re.appendTag(tag);
		}
		nbt.setTag("PlayerRespawnData", re);
		return nbt;
	}
	
	public List<Transmitter> getTransmitters() {
		return ImmutableList.copyOf(transmitters.values());
	}
	
	public List<Transmitter> getTransmittersVisibleAt(Vec3d pos) {
		return getTransmittersVisibleAt(pos.xCoord, pos.yCoord, pos.zCoord);
	}
	
	public List<Transmitter> getTransmittersVisibleAt(double x, double y, double z) {
		List<Transmitter> li = Lists.newArrayList();
		for (Transmitter t : transmitters.values()) {
			if (t.position.distanceSqToCenter(x, y, z) <= t.range*t.range) {
				li.add(t);
			}
		}
		return li;
	}
	
	public void addTransmitter(Transmitter t) {
		if (t == null) return;
		Correlated.log.info("Adding transmitter at {}, id {}", t.position, t.id);
		transmitters.put(t.id, t);
		t.valid = true;
		markDirty();
	}
	
	public void removeTransmitter(Transmitter t) {
		if (t == null) return;
		removeTransmitterById(t.id);
	}
	
	public Transmitter getTransmitterById(UUID id) {
		return transmitters.get(id);
	}
	
	public void removeTransmitterById(UUID id) {
		Transmitter t = transmitters.remove(id);
		if (t != null) {
			t.valid = false;
			Correlated.log.info("Removing transmitter at {}, id {}", t.position, t.id);
			markDirty();
		}
	}
	
	public Map<UUID, NBTTagCompound> getPlayerRespawnData() {
		return playerRespawnData;
	}

}
