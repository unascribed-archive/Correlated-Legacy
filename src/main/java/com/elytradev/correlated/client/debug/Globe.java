package com.elytradev.correlated.client.debug;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.IRenderHandler;

public class Globe extends IRenderHandler {

	private final double x;
	private final double y;
	private final double z;
	
	private final float r;
	private final float g;
	private final float b;
	
	private final float width;
	private final float radius;
	
	
	
	public Globe(double x, double y, double z, float r, float g, float b, float width, float radius) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.r = r;
		this.g = g;
		this.b = b;
		this.width = width;
		this.radius = radius;
	}



	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		GL11.glLineWidth(width);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();
		GlStateManager.disableTexture2D();
		GlStateManager.color(r, g, b);
		vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		int steps = 16;
		for (int i = 0; i < steps; i++) {
			for (int j = 0; j < steps; j++) {
				float theta = (i/(float)steps) * (float)(Math.PI*2);
				float phi = (j/(float)steps) * (float)(Math.PI*2);
				
				double xOfs = MathHelper.cos(theta) * MathHelper.sin(phi) * (radius/2);
				double yOfs = MathHelper.sin(theta) * MathHelper.sin(phi) * (radius/2);
				double zOfs = -MathHelper.cos(phi) * (radius/2);
				
				vb.pos(x+xOfs, y+yOfs, z+zOfs).endVertex();
			}
		}
		tess.draw();
		GlStateManager.enableTexture2D();
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(b);
		result = prime * result + Float.floatToIntBits(g);
		result = prime * result + Float.floatToIntBits(r);
		result = prime * result + Float.floatToIntBits(radius);
		result = prime * result + Float.floatToIntBits(width);
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Globe other = (Globe) obj;
		if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b)) {
			return false;
		}
		if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g)) {
			return false;
		}
		if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r)) {
			return false;
		}
		if (Float.floatToIntBits(radius) != Float
				.floatToIntBits(other.radius)) {
			return false;
		}
		if (Float.floatToIntBits(width) != Float.floatToIntBits(other.width)) {
			return false;
		}
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) {
			return false;
		}
		return true;
	}
	
}
