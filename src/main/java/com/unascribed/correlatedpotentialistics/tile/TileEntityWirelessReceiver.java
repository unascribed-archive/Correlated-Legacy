package com.unascribed.correlatedpotentialistics.tile;

import static net.minecraft.util.MathHelper.*;

import net.minecraft.util.Vec3;

public class TileEntityWirelessReceiver extends TileEntityWirelessEndpoint {
	
	
	public float getYaw() {
		return hasWorldObj() ? worldObj.getTotalWorldTime()%360 : 45;
	}

	public float getPitch() {
		return hasWorldObj() ? (float) (((Math.sin(worldObj.getTotalWorldTime()/20f)+1)/2)*45)+30 : 45;
	}

	public Vec3 getFacing() {
		float yr = (float)Math.toRadians(getYaw());
		float pr = (float)Math.toRadians(getPitch());
		return new Vec3(-(sin(yr)*cos(pr)), sin(pr), -(cos(yr)*cos(pr)));
	}

}
