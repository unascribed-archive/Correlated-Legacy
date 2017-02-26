package com.elytradev.correlated.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockController;
import com.elytradev.correlated.block.BlockController.State;
import com.elytradev.correlated.tile.TileEntityController;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderController extends TileEntitySpecialRenderer<TileEntityController> {

	@Override
	public void renderTileEntityAt(TileEntityController te, double x, double y, double z, float partialTicks, int destroyStage) {
		IBlockState bs = te.getWorld().getBlockState(te.getPos());
		if (bs.getBlock() != Correlated.controller) return;

		float lastX = OpenGlHelper.lastBrightnessX;
		float lastY = OpenGlHelper.lastBrightnessY;

		State state = bs.getValue(BlockController.state);
		String topTex;
		switch (state) {
			case BOOTING:
				topTex = "correlated:blocks/controller_booting";
				break;
			case ERROR:
				topTex = "correlated:blocks/controller_error";
				break;
			case OFF:
				return;
			case POWERED:
				topTex = bs.getValue(BlockController.cheaty) ? "correlated:blocks/controller_creative" : "correlated:blocks/controller";
				break;
			default:
				return;
		}
		String sideTex = topTex+"_side";
		TextureAtlasSprite top = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(topTex);
		TextureAtlasSprite side = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(sideTex);
		float minUTop = top.getMinU();
		float maxUTop = top.getMaxU();
		float minVTop = top.getMinV();
		float maxVTop = top.getMaxV();
		float minUSide = side.getMinU();
		float maxUSide = side.getMaxU();
		float minVSide = side.getMinV();
		float maxVSide = side.getMaxV();

		GlStateManager.color(1, 1, 1);

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		if (state != State.OFF) {
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			GlStateManager.disableLighting();
		}

		Tessellator tess = Tessellator.getInstance();
		VertexBuffer wr = tess.getBuffer();
		wr.setTranslation(x, y, z);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		wr.pos(0, 0, 1.001).tex(minUSide, maxVSide).endVertex();
		wr.pos(1, 0, 1.001).tex(maxUSide, maxVSide).endVertex();
		wr.pos(1, 1, 1.001).tex(maxUSide, minVSide).endVertex();
		wr.pos(0, 1, 1.001).tex(minUSide, minVSide).endVertex();

		wr.pos(0, 0, -0.001).tex(maxUSide, maxVSide).endVertex();
		wr.pos(0, 1, -0.001).tex(maxUSide, minVSide).endVertex();
		wr.pos(1, 1, -0.001).tex(minUSide, minVSide).endVertex();
		wr.pos(1, 0, -0.001).tex(minUSide, maxVSide).endVertex();

		wr.pos(0, 1.001, 1).tex(minUTop, maxVTop).endVertex();
		wr.pos(1, 1.001, 1).tex(maxUTop, maxVTop).endVertex();
		wr.pos(1, 1.001, 0).tex(maxUTop, minVTop).endVertex();
		wr.pos(0, 1.001, 0).tex(minUTop, minVTop).endVertex();

		wr.pos(1.001, 0, 0).tex(maxUSide, maxVSide).endVertex();
		wr.pos(1.001, 1, 0).tex(maxUSide, minVSide).endVertex();
		wr.pos(1.001, 1, 1).tex(minUSide, minVSide).endVertex();
		wr.pos(1.001, 0, 1).tex(minUSide, maxVSide).endVertex();

		wr.pos(-0.001, 0, 1).tex(maxUSide, maxVSide).endVertex();
		wr.pos(-0.001, 1, 1).tex(maxUSide, minVSide).endVertex();
		wr.pos(-0.001, 1, 0).tex(minUSide, minVSide).endVertex();
		wr.pos(-0.001, 0, 0).tex(minUSide, maxVSide).endVertex();

		tess.draw();
		wr.setTranslation(0, 0, 0);

		if (state != State.OFF) {
			GlStateManager.enableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
		}
	}

}
