package io.github.elytra.copo.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class LimboTeleporter extends Teleporter {
	public LimboTeleporter(WorldServer worldIn) {
		super(worldIn);
	}

	@Override
	public boolean makePortal(Entity entityIn) {
		return false;
	}
	
	@Override
	public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
		if (entityIn instanceof EntityPlayerMP) {
			((EntityPlayerMP) entityIn).connection.setPlayerLocation(0, 64, 0, entityIn.rotationYaw, entityIn.rotationPitch);
		} else {
			entityIn.setLocationAndAngles(0, 64, 0, entityIn.rotationYaw, entityIn.rotationPitch);
		}
		return true;
	}
	
	@Override
	public void placeInPortal(Entity entityIn, float rotationYaw) {
		if (entityIn instanceof EntityPlayerMP) {
			((EntityPlayerMP) entityIn).connection.setPlayerLocation(0, 64, 0, entityIn.rotationYaw, entityIn.rotationPitch);
		} else {
			entityIn.setLocationAndAngles(0, 64, 0, entityIn.rotationYaw, entityIn.rotationPitch);
		}
	}
	
}
