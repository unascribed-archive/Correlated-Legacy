package com.elytradev.correlated.client.debug;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.IRenderHandler;

public class Line extends IRenderHandler {

	private final double x1;
	private final double y1;
	private final double z1;
	
	private final double x2;
	private final double y2;
	private final double z2;
	
	private final float r;
	private final float g;
	private final float b;
	
	private final float width;
	
	
	
	public Line(double x1, double y1, double z1,
			double x2, double y2, double z2,
			float r, float g, float b,
			float thickness) {
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		this.r = r;
		this.g = g;
		this.b = b;
		this.width = thickness;
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
		vb.pos(x1, y1, z1).endVertex();
		vb.pos(x2, y2, z2).endVertex();
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
		result = prime * result + Float.floatToIntBits(width);
		long temp;
		temp = Double.doubleToLongBits(x1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z2);
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
		Line other = (Line) obj;
		if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b)) {
			return false;
		}
		if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g)) {
			return false;
		}
		if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r)) {
			return false;
		}
		if (Float.floatToIntBits(width) != Float.floatToIntBits(other.width)) {
			return false;
		}
		if (Double.doubleToLongBits(x1) != Double.doubleToLongBits(other.x1)) {
			return false;
		}
		if (Double.doubleToLongBits(x2) != Double.doubleToLongBits(other.x2)) {
			return false;
		}
		if (Double.doubleToLongBits(y1) != Double.doubleToLongBits(other.y1)) {
			return false;
		}
		if (Double.doubleToLongBits(y2) != Double.doubleToLongBits(other.y2)) {
			return false;
		}
		if (Double.doubleToLongBits(z1) != Double.doubleToLongBits(other.z1)) {
			return false;
		}
		if (Double.doubleToLongBits(z2) != Double.doubleToLongBits(other.z2)) {
			return false;
		}
		return true;
	}
	
	

}
