package com.unascribed.correlatedpotentialistics.client.render;

import org.lwjgl.opengl.GL11;

import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.block.BlockDriveBay;
import com.unascribed.correlatedpotentialistics.item.ItemDrive;
import com.unascribed.correlatedpotentialistics.tile.TileEntityDriveBay;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class RenderDriveBay extends TileEntitySpecialRenderer<TileEntityDriveBay> {
	private static class RenderDummy extends Render<Entity> {
		protected RenderDummy(RenderManager renderManager) {
			super(renderManager);
		}

		@Override
		public void renderLivingLabel(Entity entityIn, String str, double x, double y, double z, int maxDistance) {
			super.renderLivingLabel(entityIn, str, x, y, z, maxDistance);
		}
		
		@Override
		protected ResourceLocation getEntityTexture(Entity entity) {
			return null;
		}
	}
	private static final ResourceLocation DRIVE = new ResourceLocation("correlatedpotentialistics", "textures/blocks/drive_bay_drive.png");
	private RenderDummy renderDummy;
	private final Entity DUMMY_ENTITY = new Entity(null) {
		@Override protected void writeEntityToNBT(NBTTagCompound tagCompound) { }
		@Override protected void readEntityFromNBT(NBTTagCompound tagCompund) { }
		@Override protected void entityInit() { }
	};
	@Override
	public void renderTileEntityAt(TileEntityDriveBay te, double x, double y, double z, float partialTicks, int destroyStage) {
		IBlockState bs = te.getWorld().getBlockState(te.getPos());
		if (bs.getBlock() != CoPo.drive_bay) return;
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockDriveBay.facing);
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
		GlStateManager.translate(0, 0, -0.0001f);
		GlStateManager.enableTexture2D();
		Minecraft.getMinecraft().getTextureManager().bindTexture(DRIVE);
		
		Tessellator tess = Tessellator.getInstance();
		WorldRenderer wr = tess.getWorldRenderer();
		float oldX = OpenGlHelper.lastBrightnessX;
		float oldY = OpenGlHelper.lastBrightnessY;
		
		
		int light = te.getWorld().getCombinedLight(te.getPos().offset(facing), 0);
        int j = light % 65536;
        int k = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
        
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (int i = 0; i < 8; i++) {
			if (te.hasDriveInSlot(i)) {
				ItemStack drive = te.getDriveInSlot(i);
				if (drive.getItem() instanceof ItemDrive) {
					ItemDrive itemDrive = (ItemDrive)drive.getItem();
					drawDriveBox(itemDrive.getBaseColor(drive), i, 0, 0);
				}
			}
		}
		tess.draw();
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (int i = 0; i < 8; i++) {
			if (te.hasDriveInSlot(i)) {
				ItemStack drive = te.getDriveInSlot(i);
				if (drive.getItem() instanceof ItemDrive) {
					ItemDrive itemDrive = (ItemDrive)drive.getItem();
					drawDriveBox(itemDrive.getFullnessColor(drive), i, 0, 0.5f);
				}
			}
		}
		tess.draw();
		
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (int i = 0; i < 8; i++) {
			if (te.hasDriveInSlot(i)) {
				ItemStack drive = te.getDriveInSlot(i);
				if (drive.getItem() instanceof ItemDrive) {
					ItemDrive itemDrive = (ItemDrive)drive.getItem();
					drawDriveBox(itemDrive.getTierColor(drive), i, 0.5f, 0);
				}
			}
		}
		tess.draw();
		
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		
		MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
		if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK && mop.getBlockPos().equals(te.getPos())) {
			if (te.getBlockType() instanceof BlockDriveBay) {
				BlockDriveBay block = (BlockDriveBay)te.getBlockType();
				float hitX = (float)(mop.hitVec.xCoord-te.getPos().getX());
				float hitY = (float)(mop.hitVec.yCoord-te.getPos().getY());
				float hitZ = (float)(mop.hitVec.zCoord-te.getPos().getZ());
				int slot = block.getLookedAtSlot(bs, mop.sideHit, hitX, hitY, hitZ);
				if (slot != -1 && te.hasDriveInSlot(slot)) {
					ItemStack drive = te.getDriveInSlot(slot);
					if (drive.hasDisplayName()) {
						double nameXZ = 0.325+((7-slot)%2)*(6f/16f);
						double nameX = 0;
						double nameY = 0.5+((7-slot)/2)*(3/16f);
						double nameZ = 0;
						switch (mop.sideHit) {
							case EAST:
								nameZ = nameXZ;
								nameX = mop.hitVec.xCoord-te.getPos().getX();
								break;
							case WEST:
								nameZ = nameXZ;
								nameX = mop.hitVec.xCoord-te.getPos().getX();
								nameZ = 1-nameZ;
								break;
							case NORTH:
								nameX = nameXZ;
								nameZ = mop.hitVec.zCoord-te.getPos().getZ();
								break;
							case SOUTH:
								nameX = nameXZ;
								nameZ = mop.hitVec.zCoord-te.getPos().getZ();
								nameX = 1-nameX;
								break;
							default:
								break;
						}
						nameX += mop.sideHit.getFrontOffsetX()*(2/16f);
						nameY += mop.sideHit.getFrontOffsetY()*(2/16f);
						nameZ += mop.sideHit.getFrontOffsetZ()*(2/16f);
						DUMMY_ENTITY.worldObj = te.getWorld();
						DUMMY_ENTITY.posX = DUMMY_ENTITY.prevPosX = te.getPos().getX()+0.5;
						DUMMY_ENTITY.posY = DUMMY_ENTITY.prevPosY = te.getPos().getY()+0.5;
						DUMMY_ENTITY.posZ = DUMMY_ENTITY.prevPosZ = te.getPos().getZ()+0.5;
						DUMMY_ENTITY.height = -0.75f;
						if (renderDummy == null) {
							renderDummy = new RenderDummy(Minecraft.getMinecraft().getRenderManager());
						}
						GlStateManager.pushMatrix();
						GlStateManager.scale(0.5f, 0.5f, 0.5f);
						renderDummy.renderLivingLabel(DUMMY_ENTITY, drive.getDisplayName(), (x+nameX)*2, (y+nameY)*2, (z+nameZ)*2, 64);
						GlStateManager.popMatrix();
					}
				}
			}
		}
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldX, oldY);
	}
	private void drawDriveBox(int color, int slot, float u, float v) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int a = 255;
		
		float x = 3/16f;
		float y = 2/16f;
		float z = 0/16f;
		
		float w = 4/16f;
		float h = 2/16f;
		float d = 1/16f;
		
		int renderSlot = 7-slot;
		y += (renderSlot/2)*(3/16f);
		x += (renderSlot%2)*(6/16f);
		
		float antiBleed = 0.0001f;
		float m = 0.001f; // meld
		
		Tessellator tess = Tessellator.getInstance();
		WorldRenderer wr = tess.getWorldRenderer();
		
		{
			// Right
			float minU = u+(5/12f);
			float maxU = u+(6/12f);
			
			float minV = v+(1/8f);
			float maxV = v+(3/8f);
			
			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;
			
			wr.pos(x  , y  , z+m).tex(minU, maxV).color(r, g, b, a).normal(-1, 0, 0).endVertex();
			wr.pos(x  , y+h, z+m).tex(minU, minV).color(r, g, b, a).normal(-1, 0, 0).endVertex();
			wr.pos(x  , y+h, z-d).tex(maxU, minV).color(r, g, b, a).normal(-1, 0, 0).endVertex();
			wr.pos(x  , y  , z-d).tex(maxU, maxV).color(r, g, b, a).normal(-1, 0, 0).endVertex();
		}
		{
			// Front
			float minU = u+(1/12f);
			float maxU = u+(5/12f);
			
			float minV = v+(1/8f);
			float maxV = v+(3/8f);
			
			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;
			
			wr.pos(x  , y  , z-d).tex(maxU, maxV).color(r, g, b, a).normal(0, 0, -1).endVertex();
			wr.pos(x  , y+h, z-d).tex(maxU, minV).color(r, g, b, a).normal(0, 0, -1).endVertex();
			wr.pos(x+w, y+h, z-d).tex(minU, minV).color(r, g, b, a).normal(0, 0, -1).endVertex();
			wr.pos(x+w, y  , z-d).tex(minU, maxV).color(r, g, b, a).normal(0, 0, -1).endVertex();
		}
		{
			// Left
			float minU = u+(0/12f);
			float maxU = u+(1/12f);
			
			float minV = v+(1/8f);
			float maxV = v+(3/8f);
			
			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;
			
			wr.pos(x+w, y  , z-d).tex(maxU, maxV).color(r, g, b, a).normal(1, 0, 0).endVertex();
			wr.pos(x+w, y+h, z-d).tex(maxU, minV).color(r, g, b, a).normal(1, 0, 0).endVertex();
			wr.pos(x+w, y+h, z+m).tex(minU, minV).color(r, g, b, a).normal(1, 0, 0).endVertex();
			wr.pos(x+w, y  , z+m).tex(minU, maxV).color(r, g, b, a).normal(1, 0, 0).endVertex();
		}
		{
			// Top
			float minU = u+(1/12f);
			float maxU = u+(5/12f);
			
			float minV = v+(0/8f);
			float maxV = v+(1/8f);
			
			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;
			
			wr.pos(x+w, y+h, z+m).tex(minU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x+w, y+h, z-d).tex(minU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x  , y+h, z-d).tex(maxU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x  , y+h, z+m).tex(maxU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
		}
		{
			// Botom
			float minU = u+(1/12f);
			float maxU = u+(5/12f);
			
			float minV = v+(3/8f);
			float maxV = v+(4/8f);
			
			minU += antiBleed;
			maxU -= antiBleed;
			minV += antiBleed;
			maxV -= antiBleed;
			
			wr.pos(x  , y  , z+m).tex(maxU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x  , y  , z-d).tex(maxU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x+w, y  , z-d).tex(minU, maxV).color(r, g, b, a).normal(0, 1, 0).endVertex();
			wr.pos(x+w, y  , z+m).tex(minU, minV).color(r, g, b, a).normal(0, 1, 0).endVertex();
		}
	}

}
