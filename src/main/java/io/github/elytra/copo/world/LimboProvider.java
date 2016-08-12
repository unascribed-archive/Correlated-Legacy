package io.github.elytra.copo.world;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.math.Vec2i;
import io.github.elytra.copo.network.SetGlitchingStateMessage;
import io.github.elytra.copo.network.SetGlitchingStateMessage.GlitchState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LimboProvider extends WorldProvider {

	private static final DamageSource constraint_violation = new DamageSource("correlatedpotentialistics.constraint_violation")
			.setDamageAllowedInCreativeMode().setDamageBypassesArmor().setDamageIsAbsolute();
	
	private DungeonGrid grid;
	private DungeonScribe scribe;
	private final Map<EntityPlayerMP, Vec2i> constraints = new WeakHashMap<>();
	private final Map<UUID, DungeonPlayer> entering = Maps.newHashMap();
	private final Set<UUID> leaving = Sets.newHashSet();
	private LimboTeleporter teleporter;
	
	@Override
	public DimensionType getDimensionType() {
		return CoPo.limbo;
	}

	@Override
	public void createBiomeProvider() {
		this.biomeProvider = new BiomeProviderSingle(Biomes.VOID);
		this.hasNoSky = true;
		grid = new DungeonGrid();
		grid.deserializeNBT(worldObj.getWorldInfo().getDimensionData(getDimensionType()));
		scribe = new DungeonScribe(worldObj);
		if (worldObj instanceof WorldServer) {
			teleporter = new LimboTeleporter((WorldServer)worldObj, scribe, grid);
		}
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		return new LimboChunkGenerator(this.worldObj, this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.worldObj.getSeed(), grid);
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		return 0.0F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
		return new Vec3d(0.4, 0.4, 0.4);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isSkyColored() {
		return false;
	}
	
	@Override
	public boolean canRespawnHere() {
		return false;
	}
	
	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		return leaving.remove(player.getGameProfile().getId()) ? 0 : CoPo.limboDimId;
	}
	
	public LimboTeleporter getTeleporter() {
		return teleporter;
	}
	
	@Override
	public boolean isSurfaceWorld() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight() {
		return 8.0F;
	}

	@Override
	public boolean canCoordinateBeSpawn(int x, int z) {
		return this.worldObj.getGroundAboveSeaLevel(new BlockPos(x, 0, z)).getMaterial().blocksMovement();
	}

	@Override
	public BlockPos getSpawnCoordinate() {
		return new BlockPos(0, 64, 0);
	}

	@Override
	public int getAverageGroundLevel() {
		return 50;
	}
	
	@Override
	public String getWelcomeMessage() {
		return "Entering \u00A7kAhasFDDS d fHFDS h";
	}
	
	@Override
	public String getDepartMessage() {
		return "Leaving \u00A7kAhasFDDS d fHFDS h";
	}
	
	@Override
	public boolean canDropChunk(int x, int z) {
		Dungeon d = grid.getFromChunk(x, z);
		return d == null || d.noPlayersOnline();
	}
	
	@Override
	public void onPlayerAdded(EntityPlayerMP player) {
		int x = (int)Math.floor((player.posX/Dungeon.NODE_SIZE)/Dungeon.DUNGEON_SIZE);
		int z = (int)Math.floor((player.posZ/Dungeon.NODE_SIZE)/Dungeon.DUNGEON_SIZE);
		constraints.put(player, new Vec2i(x, z));
		CoPo.log.info("Constraining {} to dungeon {}", player.getName(), constraints.get(player));
	}
	
	@Override
	public void onPlayerRemoved(EntityPlayerMP player) {
	}
	
	@Override
	public void onWorldSave() {
		worldObj.getWorldInfo().setDimensionData(getDimensionType(), grid.serializeNBT());
	}
	
	@Override
	public void onWorldUpdateEntities() {
		Iterator<Map.Entry<EntityPlayerMP, Vec2i>> iter = constraints.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<EntityPlayerMP, Vec2i> en = iter.next();
			EntityPlayerMP player = en.getKey();
			if (player.isDead) {
				CoPo.log.info("Removing constraints on {}", player.getName());
				if (leaving.contains(player.getGameProfile().getId())) {
					Dungeon d = grid.getFromBlock((int)player.posX, (int)player.posZ);
					if (d != null) {
						DungeonPlayer p = d.getPlayer(player);
						if (p != null) {
							d.removePlayer(p);
							player.setDead();
							MinecraftServer srv = player.mcServer;
							player.getServerWorld().addScheduledTask(() -> {
								EntityPlayerMP nw = srv.getPlayerList().getPlayerByUUID(p.getProfile().getId());
								nw.readFromNBT(p.getOldPlayer());
							});
						}
					}
				}
				iter.remove();
				continue;
			}
			player.setGameType(GameType.ADVENTURE);
			int dX = (int)Math.floor((player.posX/Dungeon.NODE_SIZE)/Dungeon.DUNGEON_SIZE);
			int dZ = (int)Math.floor((player.posZ/Dungeon.NODE_SIZE)/Dungeon.DUNGEON_SIZE);
			if (dX != en.getValue().x || dZ != en.getValue().y) {
				// mess with them a bit
				CoPo.inst.network.sendTo(new SetGlitchingStateMessage(GlitchState.CORRUPTING), player);
				en.getKey().attackEntityFrom(constraint_violation, 75000);
				en.getKey().setDead(); // just in case
			}
		}
	}

	public void addLeavingPlayer(UUID id) {
		leaving.add(id);
	}

	public void addEnteringPlayer(DungeonPlayer player) {
		entering.put(player.getProfile().getId(), player);
	}
	
	public boolean isEntering(UUID id) {
		return entering.containsKey(id);
	}
	
	public DungeonPlayer popEntering(UUID id) {
		return entering.remove(id);
	}

}
