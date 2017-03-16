package com.elytradev.correlated.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockTerminal;
import com.elytradev.correlated.client.render.ProtrudingBoxRenderer;
import com.elytradev.correlated.tile.TileEntityTerminal;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class RenderTerminal extends TileEntitySpecialRenderer<TileEntityTerminal> {

	private static final ResourceLocation FLOPPY = new ResourceLocation("correlated", "textures/misc/floppy.png");
	public static final ProtrudingBoxRenderer pbr = new ProtrudingBoxRenderer()
			.slotCount(1)
			.columns(1)
			
			.width(6)
			.height(1)
			.depth(1)
			
			.textureWidth(8)
			.textureHeight(3)
			
			.x(5)
			.y(2)
			.z(0);
	
	@Override
	public void renderTileEntityAt(TileEntityTerminal te, double x, double y, double z, float partialTicks, int destroyStage) {
		IBlockState bs = te.getWorld().getBlockState(te.getPos());
		if (bs.getBlock() != Correlated.terminal) return;
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		EnumFacing facing = bs.getValue(BlockTerminal.FACING);
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

		Tessellator tess = Tessellator.getInstance();
		VertexBuffer wr = tess.getBuffer();
		
		float lastX = OpenGlHelper.lastBrightnessX;
		float lastY = OpenGlHelper.lastBrightnessY;
		if (bs.getValue(BlockTerminal.LIT)) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			GlStateManager.disableLighting();
	
			String path = "correlated:blocks/accessory/terminal_glow";
			
			if (te.isErroring()) {
				path = "correlated:blocks/accessory/terminal_error_glow";
			}
			
			TextureAtlasSprite tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(path);
			float minU = tas.getMinU();
			float maxU = tas.getMaxU();
			float minV = tas.getMinV();
			float maxV = tas.getMaxV();
			
			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			wr.pos(0, 0, 0).tex(maxU, maxV).endVertex();
			wr.pos(0, 1, 0).tex(maxU, minV).endVertex();
			wr.pos(1, 1, 0).tex(minU, minV).endVertex();
			wr.pos(1, 0, 0).tex(minU, maxV).endVertex();
			tess.draw();
	
			
			GlStateManager.enableLighting();
		}
		
		int light = te.getWorld().getCombinedLight(te.getPos().offset(facing), 0);
		int j = light % 65536;
		int k = light / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
		
		if (bs.getValue(BlockTerminal.FLOPPY)) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(FLOPPY);
			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
			pbr.render(-1, 0, 0, 0);
			tess.draw();
		}
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
		
		

		GlStateManager.popMatrix();
	}

}
