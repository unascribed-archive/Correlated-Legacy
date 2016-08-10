package io.github.elytra.copo.client;

import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ParticleWeldthrower extends ParticleRedstone {

	public ParticleWeldthrower(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, float scale) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, scale, 0, 0, 0);
	}
	
	public void setMotion(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}
	
	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		float f = (this.particleAge + p_189214_1_) / this.particleMaxAge;
		f = MathHelper.clamp_float(f, 0.0F, 1.0F);
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 240;
		int k = i >> 16 & 255;
		return j | k << 16;
	}

}
