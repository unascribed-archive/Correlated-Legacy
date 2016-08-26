package io.github.elytra.copo.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;

public class ProtrudingBoxRenderer {
	private int slotCount;
	private float textureWidth;
	private float textureHeight;
	private int cols;
	
	private int width;
	private int height;
	private int depth;
	
	private int xPadding;
	private int yPadding;
	
	private int x;
	private int y;
	private int z;
	
	public ProtrudingBoxRenderer slotCount(int slotCount) {
		this.slotCount = slotCount;
		return this;
	}

	public ProtrudingBoxRenderer textureWidth(float textureWidth) {
		this.textureWidth = textureWidth;
		return this;
	}
	
	public ProtrudingBoxRenderer textureHeight(float textureHeight) {
		this.textureHeight = textureHeight;
		return this;
	}

	public ProtrudingBoxRenderer columns(int cols) {
		this.cols = cols;
		return this;
	}

	public ProtrudingBoxRenderer width(int width) {
		this.width = width;
		return this;
	}

	public ProtrudingBoxRenderer height(int height) {
		this.height = height;
		return this;
	}

	public ProtrudingBoxRenderer depth(int depth) {
		this.depth = depth;
		return this;
	}

	public ProtrudingBoxRenderer xPadding(int xPadding) {
		this.xPadding = xPadding;
		return this;
	}

	public ProtrudingBoxRenderer yPadding(int yPadding) {
		this.yPadding = yPadding;
		return this;
	}

	public ProtrudingBoxRenderer x(int x) {
		this.x = x;
		return this;
	}

	public ProtrudingBoxRenderer y(int y) {
		this.y = y;
		return this;
	}

	public ProtrudingBoxRenderer z(int z) {
		this.z = z;
		return this;
	}



	public void render(int color, int slot, float u, float v) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int a = 255;

		float x = this.x/16f;
		float y = this.y/16f;
		float z = this.z/16f;

		float w = width/16f;
		float h = height/16f;
		float d = depth/16f;

		int renderSlot = (slotCount-1)-slot;
		x += (renderSlot%cols)*((width+xPadding)/16f);
		y += (renderSlot/cols)*((height+yPadding)/16f);

		float antiBleed = 0.001f;
		float m = 0.001f; // meld

		Tessellator tess = Tessellator.getInstance();
		VertexBuffer wr = tess.getBuffer();

		{
			// Right
			float minU = u+((width+depth)/textureWidth);
			float maxU = u+((width+depth+depth)/textureWidth);

			float minV = v+((depth)/textureHeight);
			float maxV = v+((height+depth)/textureHeight);

			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;

			wr.pos(x  , y  , z+m).tex(minU, maxV).color(r, g, b, a).normal(-1, 0, 0).endVertex();
			wr.pos(x  , y+h, z+m).tex(minU, minV).color(r, g, b, a).normal(-1, 0, 0).endVertex();
			wr.pos(x  , y+h, z-d).tex(maxU, minV).color(r, g, b, a).normal(-1, 0, 0).endVertex();
			wr.pos(x  , y  , z-d).tex(maxU, maxV).color(r, g, b, a).normal(-1, 0, 0).endVertex();
		}
		{
			// Front
			float minU = u+((depth)/textureWidth);
			float maxU = u+((depth+width)/textureWidth);

			float minV = v+((depth)/textureHeight);
			float maxV = v+((depth+height)/textureHeight);

			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;

			wr.pos(x  , y  , z-d).tex(maxU, maxV).color(r, g, b, a).normal(0, 0, -1).endVertex();
			wr.pos(x  , y+h, z-d).tex(maxU, minV).color(r, g, b, a).normal(0, 0, -1).endVertex();
			wr.pos(x+w, y+h, z-d).tex(minU, minV).color(r, g, b, a).normal(0, 0, -1).endVertex();
			wr.pos(x+w, y  , z-d).tex(minU, maxV).color(r, g, b, a).normal(0, 0, -1).endVertex();
		}
		{
			// Left
			float minU = u+(0/textureWidth);
			float maxU = u+((depth)/textureWidth);

			float minV = v+((depth)/textureHeight);
			float maxV = v+((depth+height)/textureHeight);

			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;

			wr.pos(x+w, y  , z-d).tex(maxU, maxV).color(r, g, b, a).normal(1, 0, 0).endVertex();
			wr.pos(x+w, y+h, z-d).tex(maxU, minV).color(r, g, b, a).normal(1, 0, 0).endVertex();
			wr.pos(x+w, y+h, z+m).tex(minU, minV).color(r, g, b, a).normal(1, 0, 0).endVertex();
			wr.pos(x+w, y  , z+m).tex(minU, maxV).color(r, g, b, a).normal(1, 0, 0).endVertex();
		}
		{
			// Top
			float minU = u+((depth)/textureWidth);
			float maxU = u+((depth+width)/textureWidth);

			float minV = v+(0/textureHeight);
			float maxV = v+((depth)/textureHeight);

			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;

			wr.pos(x+w, y+h, z+m).tex(minU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x+w, y+h, z-d).tex(minU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x  , y+h, z-d).tex(maxU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x  , y+h, z+m).tex(maxU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
		}
		{
			// Bottom
			float minU = u+((depth)/textureWidth);
			float maxU = u+((depth+width)/textureWidth);

			float minV = v+((depth+height)/textureHeight);
			float maxV = v+((depth+height+depth)/textureHeight);

			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;

			wr.pos(x  , y  , z+m).tex(maxU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x  , y  , z-d).tex(maxU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x+w, y  , z-d).tex(minU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x+w, y  , z+m).tex(minU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
		}
	}
}
