package com.elytradev.correlated.client.render.tile;

import com.elytradev.correlated.block.BlockWireless;
import com.elytradev.correlated.block.BlockWireless.State;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.tile.TileEntityBeaconLens;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RenderBeaconLens extends TileEntitySpecialRenderer<TileEntityBeaconLens> {

	@Override
	public void renderTileEntityAt(TileEntityBeaconLens te, double x, double y, double z, float partialTicks, int destroyStage) {
		IBlockState bs = te.getWorld().getBlockState(te.getPos());
		if (bs.getBlock() != CBlocks.WIRELESS) return;
		State state = bs.getValue(BlockWireless.STATE);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		float oldX = OpenGlHelper.lastBrightnessX;
		float oldY = OpenGlHelper.lastBrightnessY;
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.color(1, 1, 1);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderMicrowaveBeam.drawGlow(state);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldX, oldY);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		
		super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
	}
	
}
