package io.github.elytra.copo.client.render.tile;

import org.lwjgl.opengl.GL11;

import io.github.elytra.copo.block.BlockWirelessEndpoint.State;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class RenderWirelessEndpoint {

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
				tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:blocks/wireless_endpoint_error");
				break;
			case LINKED:
				tas = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:blocks/wireless_endpoint_linked");
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
