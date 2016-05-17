package com.unascribed.correlatedpotentialistics.client.render;

import org.lwjgl.opengl.GL11;

import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.block.BlockWirelessEndpoint;
import com.unascribed.correlatedpotentialistics.block.BlockWirelessEndpoint.Kind;
import com.unascribed.correlatedpotentialistics.block.BlockWirelessEndpoint.State;
import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessTransmitter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderWirelessTransmitter extends TileEntitySpecialRenderer<TileEntityWirelessTransmitter> {
	private final IBlockState transmitterBlockState = CoPo.wireless_endpoint.getDefaultState().withProperty(BlockWirelessEndpoint.kind, Kind.TRANSMITTER);
	@Override
	public void renderTileEntityAt(TileEntityWirelessTransmitter te, double x, double y, double z, float partialTicks, int destroyStage) {
		State state = State.DEAD;
		if (te != null) {
			if (te.hasWorldObj()) {
				IBlockState bs = te.getWorld().getBlockState(te.getPos());
				if (bs.getBlock() != CoPo.wireless_endpoint) return;
				state = bs.getValue(BlockWirelessEndpoint.state);
			} else {
				return;
			}
		}
		Tessellator tess = Tessellator.getInstance();
		WorldRenderer wr = tess.getWorldRenderer();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.pushMatrix();
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
			if (te == null) {
				RenderWirelessEndpoint.renderBaseForItem(transmitterBlockState);
			}
			GlStateManager.translate(0.5, 1, 0.5);
			GlStateManager.disableLighting();
			
			TextureAtlasSprite trc = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:blocks/lumtorch");
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			
			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.translate(-0.3125, -0.3125, 0.5);
			GlStateManager.scale(0.625, 0.625, 0.125);
			
			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			
			wr.pos(0.4, 0.6, -3).tex(trc.getInterpolatedU(7), trc.getInterpolatedV(8)).endVertex();
			wr.pos(0.6, 0.6, -3).tex(trc.getInterpolatedU(9), trc.getInterpolatedV(8)).endVertex();
			wr.pos(0.6, 0.4, -3).tex(trc.getInterpolatedU(9), trc.getInterpolatedV(6)).endVertex();
			wr.pos(0.4, 0.4, -3).tex(trc.getInterpolatedU(7), trc.getInterpolatedV(6)).endVertex();
	
			wr.pos(0.3, 0.4, -3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(5)).endVertex();
			wr.pos(0.7, 0.4, -3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(5)).endVertex();
			wr.pos(0.7, 0.4, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).endVertex();
			wr.pos(0.3, 0.4, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).endVertex();
			
			wr.pos(0.3, 0.6, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).endVertex();
			wr.pos(0.7, 0.6, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).endVertex();
			wr.pos(0.7, 0.6, -3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(5)).endVertex();
			wr.pos(0.3, 0.6, -3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(5)).endVertex();
			
			wr.pos(0.6, 0.3, -3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(5)).endVertex();
			wr.pos(0.6, 0.7, -3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(5)).endVertex();
			wr.pos(0.6, 0.7, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).endVertex();
			wr.pos(0.6, 0.3, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).endVertex();
			
			wr.pos(0.4, 0.3, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).endVertex();
			wr.pos(0.4, 0.7, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).endVertex();
			wr.pos(0.4, 0.7, -3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(5)).endVertex();
			wr.pos(0.4, 0.3, -3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(5)).endVertex();
			
			tess.draw();
		GlStateManager.popMatrix();
		RenderWirelessEndpoint.drawGlow(state);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
	
}
