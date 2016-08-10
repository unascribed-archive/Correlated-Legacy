package io.github.elytra.copo.world;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.CoPoWorldData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;

public class LimboTeleporter extends Teleporter {
	private final WorldServer world;
	private final DungeonGrid grid;
	private final DungeonScribe scribe;
	public LimboTeleporter(WorldServer world, DungeonScribe scribe, DungeonGrid grid) {
		super(world);
		this.world = world;
		this.scribe = scribe;
		this.grid = grid;
	}

	@Override
	public boolean makePortal(Entity entityIn) {
		return false;
	}
	
	@Override
	public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
		placeInPortal(entityIn, rotationYaw);
		return true;
	}
	
	@Override
	public void placeInPortal(Entity entityIn, float rotationYaw) {
		/*Vec2i freeSpot = grid.findFreeSpot();
		Stopwatch sw = Stopwatch.createUnstarted();
		sw.start();
		scribe.erase(freeSpot);
		sw.stop();
		CoPo.log.info("Freed space at {} in {}", freeSpot, sw);
		sw.reset();
		sw.start();
		Dungeon d = new Dungeon();
		d.generateNewPlan();
		grid.set(freeSpot, d);
		sw.stop();
		CoPo.log.info("Generated new dungeon at {} in {}", freeSpot, sw);
		sw.reset();
		sw.start();
		scribe.write(d);
		sw.stop();
		CoPo.log.info("Scribed new dungeon at {} in {}", freeSpot, sw);
		Vec2f entrance = d.findEntranceTile();
		int x = (d.x*Dungeon.NODE_SIZE)*Dungeon.DUNGEON_SIZE;
		int z = (d.z*Dungeon.NODE_SIZE)*Dungeon.DUNGEON_SIZE;
		x += (entrance.x*Dungeon.NODE_SIZE);
		z += (entrance.y*Dungeon.NODE_SIZE);
		x += (Dungeon.NODE_SIZE/2);
		z += (Dungeon.NODE_SIZE/2);
		int y = 51;*/
		// for BTM
		int x = 0;
		int y = 64;
		int z = 0;
		BlockPos pos = new BlockPos(x, y-1, z);
		if (world.isAirBlock(pos)) {
			world.setBlockState(pos, Blocks.STONE.getDefaultState());
		}
		WorldProvider provider = world.provider;
		if (entityIn instanceof EntityPlayerMP) {
			EntityPlayerMP player = ((EntityPlayerMP)entityIn);
			if (provider instanceof LimboProvider) {
				DungeonPlayer dp = ((LimboProvider)provider).popEntering(player.getGameProfile().getId());
				CoPoWorldData data = CoPo.getDataFor(world.getMinecraftServer().worldServerForDimension(0));
				data.getPlayerRespawnData().put(dp.getProfile().getId(), dp.getOldPlayer());
				data.markDirty();
				//d.addPlayer(dp);
			}
			((EntityPlayerMP) entityIn).connection.setPlayerLocation(x+0.5, y, z+0.5, rotationYaw, entityIn.rotationPitch);
		} else {
			entityIn.setLocationAndAngles(x+0.5, y, z+0.5, rotationYaw, entityIn.rotationPitch);
		}
	}
	
}
