package com.elytradev.correlated.world;

import java.util.Arrays;

import com.elytradev.correlated.math.Vec2i;
import com.elytradev.hallways.DungeonTile;
import com.elytradev.hallways.VectorField;

import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class DungeonScribe {
	private World world;
	
	public DungeonScribe(World world) {
		this.world = world;
	}
	
	public void erase(Vec2i d) {
		int startX = ((d.x*Dungeon.NODE_SIZE)*Dungeon.DUNGEON_SIZE);
		int startZ = ((d.y*Dungeon.NODE_SIZE)*Dungeon.DUNGEON_SIZE);
		int endX = (((d.x+1)*Dungeon.NODE_SIZE)*Dungeon.DUNGEON_SIZE);
		int endZ = (((d.y+1)*Dungeon.NODE_SIZE)*Dungeon.DUNGEON_SIZE);
		if (startX % 16 == 0 && startZ % 16 == 0
				&& endX % 16 == 0 && endZ % 16 == 0) {
			// falls on chunk boundaries, do it the fast (but hacky) way
			startX /= 16;
			startZ /= 16;
			endX /= 16;
			endZ /= 16;
			for (int x = startX; x < endX; x++) {
				for (int z = startZ; z < endZ; z++) {
					Chunk c = world.getChunkFromChunkCoords(x, z);
					// if the chunk is already empty, don't bother emptying it
					
					// this assumes there are no entities in the chunk, which is
					// *probably* true if it's empty — the most common case is
					// a newly generated chunk we created simply by scanning
					// in this loop
					if (c.getTopFilledSegment() == 0) continue;
					Arrays.fill(c.getBlockStorageArray(), Chunk.NULL_BLOCK_STORAGE);
					Arrays.fill(c.getBiomeArray(), (byte)Biome.getIdForBiome(Biomes.VOID));
					for (TileEntity te : c.getTileEntityMap().values()) {
						if (te != null) {
							te.invalidate();
						}
					}
					c.getTileEntityMap().clear();
					for (ClassInheritanceMultiMap<Entity> m : c.getEntityLists()) {
						for (Entity e : m) {
							e.setDead();
						}
					}
					c.generateSkylightMap();
					c.checkLight();
					c.setInhabitedTime(0);
					c.setChunkModified();
					c.resetRelightChecks();
				}
			}
		} else {
			MutableBlockPos pos = new MutableBlockPos();
			for (int x = startX; x < endX; x++) {
				for (int z = startZ; z < endZ; z++) {
					// pretty straightforward but also disappointingly slow
					
					// erase every individual block in the area, using a mutable
					// blockpos to reduce object spam
					pos.setPos(x, 0, z);
					for (int y = 0; y < world.getHeight(); y++) {
						pos.setY(y);
						world.setBlockToAir(pos);
						world.removeTileEntity(pos);
					}
					for (ClassInheritanceMultiMap<Entity> m : world.getChunkFromBlockCoords(pos).getEntityLists()) {
						for (Entity e : m) {
							if (e.posX >= startX && e.posX < endX
									&& e.posZ >= startZ && e.posZ < endZ) {
								e.setDead();
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Assumes the affected area has already been cleared. If this may not
	 * be true, call {@link erase} first.
	 */
	public void write(Dungeon d) {
		VectorField<DungeonTile> plan = d.getPlan();
		int startX = ((d.x*Dungeon.NODE_SIZE)*Dungeon.DUNGEON_SIZE);
		int startZ = ((d.z*Dungeon.NODE_SIZE)*Dungeon.DUNGEON_SIZE);
		MutableBlockPos pos = new MutableBlockPos();
		for (int x = 0; x < Dungeon.DUNGEON_SIZE; x++) {
			for (int z = 0; z < Dungeon.DUNGEON_SIZE; z++) {
				DungeonTile tile = plan.get(x, z);
				if (tile == null) continue;
				int bX = startX+(x*Dungeon.NODE_SIZE);
				int bZ = startZ+(z*Dungeon.NODE_SIZE);
				for (int cX = bX; cX < bX+8; cX++) {
					for (int cZ = bZ; cZ < bZ+8; cZ++) {
						pos.setPos(cX, 50, cZ);
						world.setBlockState(pos, Blocks.STONE.getDefaultState());
						if (cX == bX || cZ == bZ || cX == (bX+7) || cZ == (bZ+7)) {
							world.setBlockState(pos, Blocks.STONE.getDefaultState());
							pos.setY(51);
						}
					}					
				}
			}
		}
	}
}
