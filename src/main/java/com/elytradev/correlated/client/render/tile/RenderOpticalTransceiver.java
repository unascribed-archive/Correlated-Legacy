package com.elytradev.correlated.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.elytradev.correlated.block.BlockWireless;
import com.elytradev.correlated.block.BlockWireless.State;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.tile.TileEntityOpticalTransceiver;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderOpticalTransceiver extends TileEntitySpecialRenderer<TileEntityOpticalTransceiver> {

	@Override
	public void render(TileEntityOpticalTransceiver te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
		
		IBlockState bs = te.getWorld().getBlockState(te.getPos());
		if (bs.getBlock() != CBlocks.WIRELESS) return;

		float lastX = OpenGlHelper.lastBrightnessX;
		float lastY = OpenGlHelper.lastBrightnessY;

		State state = bs.getValue(BlockWireless.STATE);
		String texStr;
		switch (state) {
			case DEAD:
				return;
			case OK:
				texStr = "correlated:blocks/accessory/optical_linked";
				break;
			case ERROR:
				texStr = "correlated:blocks/accessory/optical_error";
				break;
			default:
				return;
		}
		TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texStr);
		
		float minU = tex.getMinU();
		float maxU = tex.getMaxU();
		float minV = tex.getMinV();
		float maxV = tex.getMaxV();

		GlStateManager.color(1, 1, 1);

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.disableLighting();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder wr = tess.getBuffer();
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		wr.pos(0, 1.001, 0).tex(maxU, maxV).endVertex();
		wr.pos(0, 1.001, 1).tex(maxU, minV).endVertex();
		wr.pos(1, 1.001, 1).tex(minU, minV).endVertex();
		wr.pos(1, 1.001, 0).tex(minU, maxV).endVertex();
		tess.draw();
		
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		wr.pos(1, -0.001, 0).tex(minU, maxV).endVertex();
		wr.pos(1, -0.001, 1).tex(minU, minV).endVertex();
		wr.pos(0, -0.001, 1).tex(maxU, minV).endVertex();
		wr.pos(0, -0.001, 0).tex(maxU, maxV).endVertex();
		tess.draw();
		
		for (int i = 0; i < 4; i++) {
			GlStateManager.color(1, 1, 1);
			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			wr.pos(0, 0, -0.001).tex(maxU, maxV).endVertex();
			wr.pos(0, 1, -0.001).tex(maxU, minV).endVertex();
			wr.pos(1, 1, -0.001).tex(minU, minV).endVertex();
			wr.pos(1, 0, -0.001).tex(minU, maxV).endVertex();
			tess.draw();
			GlStateManager.translate(1, 0, 0);
			GlStateManager.rotate(-90, 0, 1, 0);
		}
		GlStateManager.popMatrix();
		

		GlStateManager.enableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
	}
	
}
