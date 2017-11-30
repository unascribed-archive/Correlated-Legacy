package com.elytradev.correlated.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.elytradev.correlated.ColorType;
import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockController;
import com.elytradev.correlated.block.BlockController.State;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.proxy.ClientProxy;
import com.elytradev.correlated.tile.TileEntityController;
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

public class RenderController extends TileEntitySpecialRenderer<TileEntityController> {

	@Override
	public void render(TileEntityController te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		IBlockState bs = te.getWorld().getBlockState(te.getPos());
		if (bs.getBlock() != CBlocks.CONTROLLER) return;

		float lastX = OpenGlHelper.lastBrightnessX;
		float lastY = OpenGlHelper.lastBrightnessY;

		boolean cheaty = bs.getValue(BlockController.CHEATY);
		TextureAtlasSprite powerLight = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlated:blocks/controller/controller_power_light");
		TextureAtlasSprite memoryLight = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlated:blocks/controller/controller_memory_light");
		
		float minUPowerLight = powerLight.getMinU();
		float maxUPowerLight = powerLight.getMaxU();
		float minVPowerLight = powerLight.getMinV();
		float maxVPowerLight = powerLight.getMaxV();
		float minUMemoryLight = memoryLight.getMinU();
		float maxUMemoryLight = memoryLight.getMaxU();
		float minVMemoryLight = memoryLight.getMinV();
		float maxVMemoryLight = memoryLight.getMaxV();

		GlStateManager.color(1, 1, 1);

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder wr = tess.getBuffer();
		
		if (bs.getValue(BlockController.STATE) != State.OFF) {
			int powerIdx = 254-(int)(((float)te.getClientEnergy()/te.getClientEnergyMax())*254f);
			int memoryIdx = (int)(((float)te.getClientMemoryUsed()/te.getClientMemoryMax())*254f);
			if (memoryIdx > 254) memoryIdx = 254;
			int powerColor = cheaty ? ColorType.OTHER.getColor("cheaty") : ColorType.FADE.getColor(512+powerIdx);
			int memoryColor = ColorType.FADE.getColor(512+memoryIdx);
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			GlStateManager.disableLighting();
			for (int i = 0; i < 4; i++) {
				float r = ((powerColor >> 16) & 0xFF) / 255f;
				float g = ((powerColor >>  8) & 0xFF) / 255f;
				float b = ((powerColor      ) & 0xFF) / 255f;
				GlStateManager.color(r, g, b);
				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				wr.pos(0, 0, -0.001).tex(maxUPowerLight, maxVPowerLight).endVertex();
				wr.pos(0, 1, -0.001).tex(maxUPowerLight, minVPowerLight).endVertex();
				wr.pos(1, 1, -0.001).tex(minUPowerLight, minVPowerLight).endVertex();
				wr.pos(1, 0, -0.001).tex(minUPowerLight, maxVPowerLight).endVertex();
				tess.draw();
				r = ((memoryColor >> 16) & 0xFF) / 255f;
				g = ((memoryColor >>  8) & 0xFF) / 255f;
				b = ((memoryColor      ) & 0xFF) / 255f;
				GlStateManager.color(r, g, b);
				wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				wr.pos(0, 0, -0.001).tex(maxUMemoryLight, maxVMemoryLight).endVertex();
				wr.pos(0, 1, -0.001).tex(maxUMemoryLight, minVMemoryLight).endVertex();
				wr.pos(1, 1, -0.001).tex(minUMemoryLight, minVMemoryLight).endVertex();
				wr.pos(1, 0, -0.001).tex(minUMemoryLight, maxVMemoryLight).endVertex();
				tess.draw();
				GlStateManager.translate(1, 0, 0);
				GlStateManager.rotate(-90, 0, 1, 0);
			}
			
			GlStateManager.enableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
		}
		if (((ClientProxy)Correlated.proxy).controllerAbt != null) {
			((ClientProxy)Correlated.proxy).controllerAbt.render(te, 0, 0, 0, partialTicks);
		}
		GlStateManager.popMatrix();
	}

}
