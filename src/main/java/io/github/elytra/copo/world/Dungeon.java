package io.github.elytra.copo.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.google.common.collect.Lists;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.math.Vec2f;
import io.github.elytra.hallways.DungeonTile;
import io.github.elytra.hallways.HallwayGenerator;
import io.github.elytra.hallways.VectorField;
import io.github.elytra.hallways.DungeonTile.TileType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.common.util.Constants.NBT;

public class Dungeon implements INBTSerializable<NBTTagCompound> {
	/**
	 * Measured in blocks
	 */
	public static final int NODE_SIZE = 8;
	/**
	 * Measured in nodes
	 */
	public static final int DUNGEON_SIZE = 64;
	
	public static final int MIN_COMPLEXITY = 30;
	public static final int IDEAL_COMPLEXITY = 40;
	public static final int MAX_COMPLEXITY = 50;
	
	protected int x;
	protected int z;
	
	private boolean conquered = false;
	private final List<DungeonPlayer> players = Lists.newArrayList();
	private final List<DungeonPlayer> playersView = Collections.unmodifiableList(players);
	
	private final Map<UUID, DungeonPlayer> playerLookup = new WeakHashMap<>();
	private VectorField<DungeonTile> plan = new VectorField<>(0, 0);

	public void generateNewPlan() {
		DungeonTile hall = new DungeonTile(TileType.HALLWAY);
		DungeonTile room = new DungeonTile(TileType.ROOM);
		DungeonTile entrance = new DungeonTile(TileType.ENTRANCE);
		DungeonTile exit = new DungeonTile(TileType.EXIT);
		
		VectorField<DungeonTile> selected = null;
		int complexityDelta = 0;
		int selectedComplexity = 0;
		
		int trials = 0;
		for(int i=0; i<40; i++) {
			VectorField<DungeonTile> field = new VectorField<>(64,64);
			int complexity = HallwayGenerator.generateInto(field, hall, room, entrance, exit);
			trials++;
			
			if (complexity > MAX_COMPLEXITY || complexity < MIN_COMPLEXITY) {
				i--;
				continue;
			}
			
			if (selected == null || complexity < complexityDelta) {
				selected = field;
				complexityDelta = Math.abs(complexity-IDEAL_COMPLEXITY);
				selectedComplexity = complexity;
			}
		}
		CoPo.log.info("Generated a dungeon with complexity {} (target {}), after {} trials", selectedComplexity, IDEAL_COMPLEXITY, trials);
		plan = selected;
	}
	
	public boolean noPlayersOnline() {
		for (DungeonPlayer dp : players) {
			if (FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(dp.getProfile().getId()) != null) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("Conquered", conquered);
		NBTTagList li = new NBTTagList();
		for (DungeonPlayer p : players) {
			li.appendTag(p.serializeNBT());
		}
		tag.setTag("Players", li);
		NBTTagCompound planTag = new NBTTagCompound();
		NBTTagList cells = new NBTTagList();
		for (int x = 0; x < plan.getWidth(); x++) {
			for (int y = 0; y < plan.getHeight(); y++) {
				DungeonTile tile = plan.get(x, y);
				if (tile != null) {
					NBTTagCompound entry = new NBTTagCompound();
					entry.setInteger("X", x);
					entry.setInteger("Y", y);
					entry.setTag("Content", tile.serializeNBT());
					cells.appendTag(entry);
				}
			}
		}
		planTag.setTag("Cells", cells);
		if (plan.hasTag()) {
			planTag.setTag("Tag", plan.getTag());
		}
		planTag.setInteger("Width", plan.getWidth());
		planTag.setInteger("Height", plan.getHeight());
		tag.setTag("Plan", planTag);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		conquered = nbt.getBoolean("Conquered");
		NBTTagList li = nbt.getTagList("Players", NBT.TAG_COMPOUND);
		players.clear();
		playerLookup.clear();
		for (int i = 0; i < li.tagCount(); i++) {
			DungeonPlayer p = new DungeonPlayer();
			p.deserializeNBT(li.getCompoundTagAt(i));
			addPlayer(p);
		}
		NBTTagCompound planTag = nbt.getCompoundTag("Plan");
		plan = new VectorField<>(planTag.getInteger("Width"), planTag.getInteger("Height"));
		plan.setTag(planTag.getCompoundTag("Tag"));
		NBTTagList cells = new NBTTagList();
		for (int i = 0; i < cells.tagCount(); i++) {
			NBTTagCompound entry = cells.getCompoundTagAt(i);
			DungeonTile tile = new DungeonTile(TileType.HALLWAY);
			tile.deserializeNBT(entry.getCompoundTag("Content"));
			plan.put(entry.getInteger("X"), entry.getInteger("Y"), tile);
		}
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}

	public VectorField<DungeonTile> getPlan() {
		return plan;
	}
	
	public List<DungeonPlayer> getPlayers() {
		return playersView;
	}
	
	public DungeonPlayer getPlayer(EntityPlayer entity) {
		if (entity == null) return null;
		return getPlayer(entity.getGameProfile().getId());
	}
	
	public DungeonPlayer getPlayer(UUID id) {
		if (id == null) return null;
		return playerLookup.get(id);
	}
	
	public void addPlayer(DungeonPlayer player) {
		players.add(player);
		playerLookup.put(player.getProfile().getId(), player);
	}
	
	public void removePlayer(DungeonPlayer player) {
		players.remove(player);
		playerLookup.remove(player.getProfile().getId());
	}
	
	public boolean isConquered() {
		return conquered;
	}
	
	public void setConquered(boolean conquered) {
		this.conquered = conquered;
	}

	public boolean canBeFreed() {
		return isConquered() && players.isEmpty();
	}

	public Vec2f findEntranceTile() {
		float xAcc = 0;
		float zAcc = 0;
		int found = 0;
		for (int x = 0; x < plan.getWidth(); x++) {
			for (int y = 0; y < plan.getHeight(); y++) {
				DungeonTile t = plan.get(x, y);
				if (t != null && t.type == TileType.ENTRANCE) {
					xAcc += x;
					zAcc += y;
					found++;
				}
			}
		}
		if (found <= 0) return null;
		return new Vec2f(xAcc/found, zAcc/found);
	}

}
