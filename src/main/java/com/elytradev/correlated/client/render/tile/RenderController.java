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
		String tex;
		switch (state) {
			case BOOTING:
				tex = "correlated:blocks/controller_booting";
				break;
			case ERROR:
				tex = "correlated:blocks/controller_error";
				break;
			case OFF:
				return;
			case POWERED:
				tex = bs.getValue(BlockController.cheaty) ? "correlated:blocks/controller_creative" : "correlated:blocks/controller";
				break;
			default:
				return;
		}
		TextureAtlasSprite tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(tex);
		float minU = tas.getMinU();
		float maxU = tas.getMaxU();
		float minV = tas.getMinV();
		float maxV = tas.getMaxV();

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

		wr.pos(0, 0, 1.001).tex(maxU, minV).endVertex();
		wr.pos(1, 0, 1.001).tex(minU, minV).endVertex();
		wr.pos(1, 1, 1.001).tex(minU, maxV).endVertex();
		wr.pos(0, 1, 1.001).tex(maxU, maxV).endVertex();

		wr.pos(0, 0, -0.001).tex(minU, minV).endVertex();
		wr.pos(0, 1, -0.001).tex(minU, maxV).endVertex();
		wr.pos(1, 1, -0.001).tex(maxU, maxV).endVertex();
		wr.pos(1, 0, -0.001).tex(maxU, minV).endVertex();

		wr.pos(0, -0.001, 0).tex(maxU, minV).endVertex();
		wr.pos(1, -0.001, 0).tex(minU, minV).endVertex();
		wr.pos(1, -0.001, 1).tex(minU, maxV).endVertex();
		wr.pos(0, -0.001, 1).tex(maxU, maxV).endVertex();

		wr.pos(0, 1.001, 1).tex(minU, maxV).endVertex();
		wr.pos(1, 1.001, 1).tex(maxU, maxV).endVertex();
		wr.pos(1, 1.001, 0).tex(maxU, minV).endVertex();
		wr.pos(0, 1.001, 0).tex(minU, minV).endVertex();

		wr.pos(1.001, 0, 0).tex(maxU, minV).endVertex();
		wr.pos(1.001, 1, 0).tex(minU, minV).endVertex();
		wr.pos(1.001, 1, 1).tex(minU, maxV).endVertex();
		wr.pos(1.001, 0, 1).tex(maxU, maxV).endVertex();

		wr.pos(-0.001, 0, 1).tex(minU, maxV).endVertex();
		wr.pos(-0.001, 1, 1).tex(maxU, maxV).endVertex();
		wr.pos(-0.001, 1, 0).tex(maxU, minV).endVertex();
		wr.pos(-0.001, 0, 0).tex(minU, minV).endVertex();

		tess.draw();
		wr.setTranslation(0, 0, 0);

		if (state != State.OFF) {
			GlStateManager.enableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
		}
	}

}
