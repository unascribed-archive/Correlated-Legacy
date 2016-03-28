package com.unascribed.correlatedpotentialistics.client.render;

import org.lwjgl.opengl.GL11;

import com.unascribed.correlatedpotentialistics.block.BlockVT;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

public class RenderVT extends TileEntitySpecialRenderer<TileEntityVT> {

	@Override
	public void renderTileEntityAt(TileEntityVT te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockVT.facing);
		switch (facing) {
			case NORTH:
				break;
			case WEST:
				GlStateManager.rotate(90, 0, 1, 0);
				GlStateManager.translate(-1, 0, 0);
				break;
			case SOUTH:
				GlStateManager.rotate(180, 0, 1, 0);
				GlStateManager.translate(-1, 0, -1);
				break;
			case EAST:
				GlStateManager.rotate(270, 0, 1, 0);
				GlStateManager.translate(0, 0, -1);
				break;
			default:
				break;
		}
		GlStateManager.translate(0, 0, -0.005f);
		
		GlStateManager.color(1, 1, 1);
		
		float lastX = OpenGlHelper.lastBrightnessX;
		float lastY = OpenGlHelper.lastBrightnessY;
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.disableLighting();
		
		TextureAtlasSprite tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:blocks/vt");
		float minTU = tas.getMinU();
		float maxTU = tas.getMaxU();
		float minTV = tas.getMinV();
		float maxTV = tas.getMaxV();
		
		float diffU = maxTU-minTU;
		float diffV = maxTV-minTV;
		
		float fx = 3/16f;
		float fy = 4/16f;
		
		float minU = (minTU+(diffU*fx));
		float minV = (minTV+(diffV*fy));
		float maxU = (maxTU-(diffU*fx));
		float maxV = (maxTV-(diffV*fy));
		
		Tessellator tess = Tessellator.getInstance();
		WorldRenderer wr = tess.getWorldRenderer();
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		wr.pos(  fx,   fy, 0).tex(maxU, maxV).endVertex();
		wr.pos(  fx, 1-fy, 0).tex(maxU, minV).endVertex();
		wr.pos(1-fx, 1-fy, 0).tex(minU, minV).endVertex();
		wr.pos(1-fx,   fy, 0).tex(minU, maxV).endVertex();
		tess.draw();
		
		GlStateManager.enableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
		
		GlStateManager.popMatrix();
	}

}
