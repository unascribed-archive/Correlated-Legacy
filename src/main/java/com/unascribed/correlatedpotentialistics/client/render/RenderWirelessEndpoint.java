package com.unascribed.correlatedpotentialistics.client.render;

import org.lwjgl.opengl.GL11;

import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.block.BlockWirelessEndpoint.State;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class RenderWirelessEndpoint {
	static final IBlockAccess dummy = new IBlockAccess() {

		@Override
		public TileEntity getTileEntity(BlockPos pos) {
			return null;
		}

		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
			return 0;
		}

		@Override
		public IBlockState getBlockState(BlockPos pos) {
			return CoPo.wireless_endpoint.getDefaultState();
		}

		@Override
		public boolean isAirBlock(BlockPos pos) {
			return false;
		}

		@Override
		public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
			return null;
		}

		@Override
		public boolean extendedLevelsInChunkCache() {
			return false;
		}

		@Override
		public int getStrongPower(BlockPos pos, EnumFacing direction) {
			return 0;
		}

		@Override
		public WorldType getWorldType() {
			return WorldType.DEFAULT;
		}

		@Override
		public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
			return false;
		}
		
	};

	public static void renderBaseForItem() {
		Tessellator tess = Tessellator.getInstance();
		WorldRenderer wr = tess.getWorldRenderer();
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher()
				.getModelFromBlockState(CoPo.wireless_endpoint.getDefaultState(), dummy, BlockPos.ORIGIN);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		for (EnumFacing ef : EnumFacing.VALUES) {
			for (BakedQuad quad : model.getFaceQuads(ef)) {
				LightUtil.renderQuadColor(wr, quad, 0xFFFFFFFF);
			}
		}
		for (BakedQuad quad : model.getGeneralQuads()) {
			LightUtil.renderQuadColor(wr, quad, 0xFFFFFFFF);
		}
		tess.draw();
	}

	public static void drawGlow(State state) {
		WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
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
