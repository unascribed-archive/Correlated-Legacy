package com.elytradev.correlated.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.block.BlockMicrowaveBeam;
import com.elytradev.correlated.block.BlockMicrowaveBeam.State;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class RenderMicrowaveBeam extends TileEntitySpecialRenderer<TileEntityMicrowaveBeam> {
	private final IBlockState beamBlockState = Correlated.microwave_beam.getDefaultState();
	@Override
	public void renderTileEntityAt(TileEntityMicrowaveBeam te, double x, double y, double z, float partialTicks, int destroyStage) {
		State state = State.DEAD;
		if (te != null) {
			if (te.hasWorld()) {
				IBlockState bs = te.getWorld().getBlockState(te.getPos());
				if (bs.getBlock() != Correlated.microwave_beam) return;
				state = bs.getValue(BlockMicrowaveBeam.state);
			} else {
				return;
			}
		}
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer wr = tess.getBuffer();
		Vec3d facing;
		float yaw;
		float pitch;
		if (te == null) {
			yaw = -90;
			pitch = 30;
			facing = new Vec3d(0, 0, 0);
		} else {
			yaw = te.getYaw(partialTicks);
			pitch = te.getPitch(partialTicks);
			facing = te.getFacing(partialTicks);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.pushMatrix();
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			if (te == null) {
				renderBaseForItem(beamBlockState);
			}
			GlStateManager.translate(0.5, 0.875, 0.5);
			GlStateManager.disableLighting();
			
			float nXF = (float)facing.xCoord;
			float nYF = (float)facing.yCoord;
			float nZF = (float)facing.zCoord;
			
			Vec3d right = facing.crossProduct(new Vec3d(0, 1, 0)).normalize();
			
			float nXR = (float)right.xCoord;
			float nYR = (float)right.yCoord;
			float nZR = (float)right.zCoord;
			
			Vec3d up = right.crossProduct(facing).normalize();
			
			float nXU = (float)up.xCoord;
			float nYU = (float)up.yCoord;
			float nZU = (float)up.zCoord;

			if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
				GlStateManager.disableTexture2D();
				GL11.glLineWidth(3);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glColor3f(0, 0, 1);
				GL11.glVertex3f(0, 0, 0);
				GL11.glVertex3f(nXR, nYR, nZR);
				GL11.glColor3f(1, 0, 0);
				GL11.glVertex3f(0, 0, 0);
				GL11.glVertex3f(nXF, nYF, nZF);
				GL11.glColor3f(0, 1, 0);
				GL11.glVertex3f(0, 0, 0);
				GL11.glVertex3f(nXU, nYU, nZU);
				GL11.glColor3f(1, 1, 1);
				GL11.glEnd();
				GL11.glLineWidth(1);
				GlStateManager.enableTexture2D();
			}
			
			GlStateManager.color(1,1,1);
			
			GlStateManager.disableRescaleNormal();
			
			GlStateManager.rotate(yaw, 0, 1, 0);
			GlStateManager.rotate(pitch, 1, 0, 0);
			GlStateManager.translate(-0.3125, -0.3125, -0.375);
			GlStateManager.scale(0.625, 0.625, 0.125);
			TextureAtlasSprite top = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlated:blocks/top");
			TextureAtlasSprite trc = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlated:blocks/lumtorch");
			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
			
			
			wr.pos(0.3, 0.4, 1).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(12)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.7, 0.4, 1).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(12)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.7, 0.4, 3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.3, 0.4, 3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(-nXU, -nYU, -nZU).endVertex();
			
			wr.pos(0.3, 0.6, 3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.7, 0.6, 3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.7, 0.6, 1).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(12)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.3, 0.6, 1).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(12)).normal(nXU, nYU, nZU).endVertex();
			
			wr.pos(0.6, 0.3, 1).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(12)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.7, 1).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(12)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.7, 3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.3, 3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(nXR, nYR, nZR).endVertex();
			
			wr.pos(0.4, 0.3, 3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.7, 3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.7, 1).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(12)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.3, 1).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(12)).normal(-nXR, -nYR, -nZR).endVertex();
			
			
			
			
			
			
			
			
			wr.pos(0, 1, 0).tex(top.getInterpolatedU(3), top.getInterpolatedV(13)).normal(nXF, nYF, nZF).endVertex();
			wr.pos(1, 1, 0).tex(top.getInterpolatedU(13), top.getInterpolatedV(13)).normal(nXF, nYF, nZF).endVertex();
			wr.pos(1, 0, 0).tex(top.getInterpolatedU(13), top.getInterpolatedV(3)).normal(nXF, nYF, nZF).endVertex();
			wr.pos(0, 0, 0).tex(top.getInterpolatedU(3), top.getInterpolatedV(3)).normal(nXF, nYF, nZF).endVertex();
			
			wr.pos(0, 0, 1).tex(top.getInterpolatedU(15.5), top.getInterpolatedV(3)).normal(-nXF, -nYF, -nZF).endVertex();
			wr.pos(1, 0, 1).tex(top.getInterpolatedU(16), top.getInterpolatedV(3)).normal(-nXF, -nYF, -nZF).endVertex();
			wr.pos(1, 1, 1).tex(top.getInterpolatedU(16), top.getInterpolatedV(13)).normal(-nXF, -nYF, -nZF).endVertex();
			wr.pos(0, 1, 1).tex(top.getInterpolatedU(15.5), top.getInterpolatedV(13)).normal(-nXF, -nYF, -nZF).endVertex();
			
			wr.pos(0, 0, 0).tex(top.getInterpolatedU(3), top.getInterpolatedV(14)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(1, 0, 0).tex(top.getInterpolatedU(13), top.getInterpolatedV(14)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(1, 0, 1).tex(top.getInterpolatedU(13), top.getInterpolatedV(16)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0, 0, 1).tex(top.getInterpolatedU(3), top.getInterpolatedV(16)).normal(-nXU, -nYU, -nZU).endVertex();
			
			wr.pos(0, 1, 1).tex(top.getInterpolatedU(3), top.getInterpolatedV(0)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(1, 1, 1).tex(top.getInterpolatedU(13), top.getInterpolatedV(0)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(1, 1, 0).tex(top.getInterpolatedU(13), top.getInterpolatedV(2)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0, 1, 0).tex(top.getInterpolatedU(3), top.getInterpolatedV(2)).normal(nXU, nYU, nZU).endVertex();
			
			wr.pos(1, 0, 1).tex(top.getInterpolatedU(0), top.getInterpolatedV(3)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(1, 0, 0).tex(top.getInterpolatedU(2), top.getInterpolatedV(3)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(1, 1, 0).tex(top.getInterpolatedU(2), top.getInterpolatedV(13)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(1, 1, 1).tex(top.getInterpolatedU(0), top.getInterpolatedV(13)).normal(nXR, nYR, nZR).endVertex();
			
			wr.pos(0, 0, 0).tex(top.getInterpolatedU(14), top.getInterpolatedV(3)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0, 0, 1).tex(top.getInterpolatedU(16), top.getInterpolatedV(3)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0, 1, 1).tex(top.getInterpolatedU(16), top.getInterpolatedV(13)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0, 1, 0).tex(top.getInterpolatedU(14), top.getInterpolatedV(13)).normal(-nXR, -nYR, -nZR).endVertex();
			
			
			
			wr.pos(0.3, 0.4, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.7, 0.4, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.7, 0.4, 0).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(12)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.3, 0.4, 0).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(12)).normal(-nXU, -nYU, -nZU).endVertex();
			
			wr.pos(0.3, 0.6, 0).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(12)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.7, 0.6, 0).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(12)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.7, 0.6, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.3, 0.6, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(nXU, nYU, nZU).endVertex();
			
			wr.pos(0.6, 0.3, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.7, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.7, 0).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(12)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.3, 0).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(12)).normal(nXR, nYR, nZR).endVertex();
			
			wr.pos(0.4, 0.3, 0).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(12)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.7, 0).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(12)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.7, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.3, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(-nXR, -nYR, -nZR).endVertex();
			
			
			tess.draw();
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			
			wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
			
			wr.pos(0.4, 0.6, -3).tex(trc.getInterpolatedU(7), trc.getInterpolatedV(8)).normal(nXF, nYF, nZF).endVertex();
			wr.pos(0.6, 0.6, -3).tex(trc.getInterpolatedU(9), trc.getInterpolatedV(8)).normal(nXF, nYF, nZF).endVertex();
			wr.pos(0.6, 0.4, -3).tex(trc.getInterpolatedU(9), trc.getInterpolatedV(6)).normal(nXF, nYF, nZF).endVertex();
			wr.pos(0.4, 0.4, -3).tex(trc.getInterpolatedU(7), trc.getInterpolatedV(6)).normal(nXF, nYF, nZF).endVertex();

			wr.pos(0.3, 0.4, -3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(5)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.7, 0.4, -3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(5)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.7, 0.4, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(-nXU, -nYU, -nZU).endVertex();
			wr.pos(0.3, 0.4, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(-nXU, -nYU, -nZU).endVertex();
			
			wr.pos(0.3, 0.6, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.7, 0.6, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.7, 0.6, -3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(5)).normal(nXU, nYU, nZU).endVertex();
			wr.pos(0.3, 0.6, -3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(5)).normal(nXU, nYU, nZU).endVertex();
			
			wr.pos(0.6, 0.3, -3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(5)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.7, -3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(5)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.7, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(nXR, nYR, nZR).endVertex();
			wr.pos(0.6, 0.3, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(nXR, nYR, nZR).endVertex();
			
			wr.pos(0.4, 0.3, -1.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(9)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.7, -1.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(9)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.7, -3.5).tex(trc.getInterpolatedU(10), trc.getInterpolatedV(5)).normal(-nXR, -nYR, -nZR).endVertex();
			wr.pos(0.4, 0.3, -3.5).tex(trc.getInterpolatedU(6), trc.getInterpolatedV(5)).normal(-nXR, -nYR, -nZR).endVertex();
			
			tess.draw();
		GlStateManager.popMatrix();
		drawGlow(state);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
	
	public static void renderBaseForItem(IBlockState state) {
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer wr = tess.getBuffer();
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		for (EnumFacing ef : EnumFacing.VALUES) {
			for (BakedQuad quad : model.getQuads(state, ef, 0)) {
				LightUtil.renderQuadColor(wr, quad, 0xFFFFFFFF);
			}
		}
		for (BakedQuad quad : model.getQuads(state, null, 0)) {
			LightUtil.renderQuadColor(wr, quad, 0xFFFFFFFF);
		}
		tess.draw();
	}
	
	public static void drawGlow(State state) {
		VertexBuffer wr = Tessellator.getInstance().getBuffer();
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		TextureAtlasSprite tas = null;
		switch (state) {
			case ERROR:
				tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlated:blocks/wireless_endpoint_error");
				break;
			case LINKED:
				tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlated:blocks/wireless_endpoint_linked");
				break;
			default:
				break;
		}
		
		if (tas != null) {
			float minU = tas.getMinU();
			float maxU = tas.getMaxU();
			float minV = tas.getMinV();
			float maxV = tas.getMaxV();
			
			
			wr.pos(0, 0, 1.001).tex(minU, maxV).endVertex();
			wr.pos(1, 0, 1.001).tex(maxU, maxV).endVertex();
			wr.pos(1, 1, 1.001).tex(maxU, minV).endVertex();
			wr.pos(0, 1, 1.001).tex(minU, minV).endVertex();

			wr.pos(0, 0, -0.001).tex(minU, maxV).endVertex();
			wr.pos(0, 1, -0.001).tex(minU, minV).endVertex();
			wr.pos(1, 1, -0.001).tex(maxU, minV).endVertex();
			wr.pos(1, 0, -0.001).tex(maxU, maxV).endVertex();

			wr.pos(1.001, 0, 1).tex(minU, maxV).endVertex();
			wr.pos(1.001, 0, 0).tex(maxU, maxV).endVertex();
			wr.pos(1.001, 1, 0).tex(maxU, minV).endVertex();
			wr.pos(1.001, 1, 1).tex(minU, minV).endVertex();

			wr.pos(-0.001, 0, 0).tex(minU, maxV).endVertex();
			wr.pos(-0.001, 0, 1).tex(maxU, maxV).endVertex();
			wr.pos(-0.001, 1, 1).tex(maxU, minV).endVertex();
			wr.pos(-0.001, 1, 0).tex(minU, minV).endVertex();
		}
		Tessellator.getInstance().draw();
	}
	
}
