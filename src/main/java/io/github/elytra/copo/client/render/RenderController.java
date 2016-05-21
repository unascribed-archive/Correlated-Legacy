package io.github.elytra.copo.client.render;

import org.lwjgl.opengl.GL11;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.block.BlockController;
import io.github.elytra.copo.block.BlockController.State;
import io.github.elytra.copo.tile.TileEntityController;
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
		if (bs.getBlock() != CoPo.controller) return;

		float lastX = OpenGlHelper.lastBrightnessX;
		float lastY = OpenGlHelper.lastBrightnessY;

		State state = bs.getValue(BlockController.state);
		String tex;
		switch (state) {
			case BOOTING:
				tex = "correlatedpotentialistics:blocks/controller_booting";
				break;
			case ERROR:
				tex = "correlatedpotentialistics:blocks/controller_error";
				break;
			case OFF:
				tex = "correlatedpotentialistics:blocks/controller_off";
				break;
			case POWERED:
				tex = "correlatedpotentialistics:blocks/controller";
				break;
			default:
				return;
		}
		TextureAtlasSprite tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(tex);
		float minU = tas.getMinU();
		float maxU = tas.getMaxU();
		float minV = tas.getMinV();
		float maxV = tas.getMaxV();

		float diffU = maxU-minU;
		float diffV = maxV-minV;
		minU += (diffU/8);
		minV += (diffV/8);
		maxU -= (diffU/8);
		maxV -= (diffV/8);

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

		wr.pos(0.125, 0.125, 1.001).tex(minU, minV).endVertex();
		wr.pos(0.875, 0.125, 1.001).tex(maxU, minV).endVertex();
		wr.pos(0.875, 0.875, 1.001).tex(maxU, maxV).endVertex();
		wr.pos(0.125, 0.875, 1.001).tex(minU, maxV).endVertex();

		wr.pos(0.125, 0.125, -0.001).tex(minU, minV).endVertex();
		wr.pos(0.125, 0.875, -0.001).tex(minU, maxV).endVertex();
		wr.pos(0.875, 0.875, -0.001).tex(maxU, maxV).endVertex();
		wr.pos(0.875, 0.125, -0.001).tex(maxU, minV).endVertex();

		wr.pos(0.125, -0.001, 0.125).tex(minU, minV).endVertex();
		wr.pos(0.875, -0.001, 0.125).tex(maxU, minV).endVertex();
		wr.pos(0.875, -0.001, 0.875).tex(maxU, maxV).endVertex();
		wr.pos(0.125, -0.001, 0.875).tex(minU, maxV).endVertex();

		wr.pos(0.125, 1.001, 0.875).tex(minU, maxV).endVertex();
		wr.pos(0.875, 1.001, 0.875).tex(maxU, maxV).endVertex();
		wr.pos(0.875, 1.001, 0.125).tex(maxU, minV).endVertex();
		wr.pos(0.125, 1.001, 0.125).tex(minU, minV).endVertex();

		wr.pos(1.001, 0.125, 0.125).tex(minU, minV).endVertex();
		wr.pos(1.001, 0.875, 0.125).tex(maxU, minV).endVertex();
		wr.pos(1.001, 0.875, 0.875).tex(maxU, maxV).endVertex();
		wr.pos(1.001, 0.125, 0.875).tex(minU, maxV).endVertex();

		wr.pos(-0.001, 0.125, 0.875).tex(minU, maxV).endVertex();
		wr.pos(-0.001, 0.875, 0.875).tex(maxU, maxV).endVertex();
		wr.pos(-0.001, 0.875, 0.125).tex(maxU, minV).endVertex();
		wr.pos(-0.001, 0.125, 0.125).tex(minU, minV).endVertex();

		tess.draw();
		wr.setTranslation(0, 0, 0);

		if (state != State.OFF) {
			GlStateManager.enableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
		}
	}

}
