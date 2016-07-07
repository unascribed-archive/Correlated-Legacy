package io.github.elytra.copo.world;

import io.github.elytra.copo.CoPo;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LimboProvider extends WorldProvider {

	@Override
	public DimensionType getDimensionType() {

		return CoPo.limbo;
	}

	@Override
	public void createBiomeProvider() {
		this.biomeProvider = new BiomeProviderSingle(Biomes.VOID);
		this.hasNoSky = true;
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		return new LimboChunkProvider(this.worldObj, this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.worldObj.getSeed());
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
		return new BlockPos(100, 50, 0);
	}

	@Override
	public int getAverageGroundLevel() {
		return 50;
	}
	
}
