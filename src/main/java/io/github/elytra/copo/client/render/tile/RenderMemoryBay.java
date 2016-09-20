package io.github.elytra.copo.client.render.tile;

import org.lwjgl.opengl.GL11;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.block.BlockMemoryBay;
import io.github.elytra.copo.client.render.ProtrudingBoxRenderer;
import io.github.elytra.copo.item.ItemMemory;
import io.github.elytra.copo.tile.TileEntityMemoryBay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class RenderMemoryBay extends TileEntitySpecialRenderer<TileEntityMemoryBay> {
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
	private static final ResourceLocation DRIVE = new ResourceLocation("correlatedpotentialistics", "textures/misc/memory.png");
	private RenderDummy renderDummy;
	private final Entity DUMMY_ENTITY = new Entity(null) {
		@Override protected void writeEntityToNBT(NBTTagCompound tagCompound) { }
		@Override protected void readEntityFromNBT(NBTTagCompound tagCompund) { }
		@Override protected void entityInit() { }
	};
	public static final ProtrudingBoxRenderer pbr = new ProtrudingBoxRenderer()
			.slotCount(12)
			.columns(2)
			
			.width(4)
			.height(1)
			.depth(1)
			
			.textureWidth(6)
			.textureHeight(6)
			
			.x(3)
			.y(2)
			.z(0)
			
			.xPadding(2)
			.yPadding(1);
	@Override
	public void renderTileEntityAt(TileEntityMemoryBay te, double x, double y, double z, float partialTicks, int destroyStage) {
		IBlockState bs = te.getWorld().getBlockState(te.getPos());
		if (bs.getBlock() != CoPo.memory_bay) return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockMemoryBay.FACING);
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
		VertexBuffer wr = tess.getBuffer();
		float oldX = OpenGlHelper.lastBrightnessX;
		float oldY = OpenGlHelper.lastBrightnessY;


		int light = te.getWorld().getCombinedLight(te.getPos().offset(facing), 0);
		int j = light % 65536;
		int k = light / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);

		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (int i = 0; i < 12; i++) {
			if (te.hasMemoryInSlot(i)) {
				ItemStack memory = te.getMemoryInSlot(i);
				if (memory.getItem() instanceof ItemMemory) {
					pbr.render(-1, i, 0, 0);
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
		for (int i = 0; i < 12; i++) {
			if (te.hasMemoryInSlot(i)) {
				ItemStack memory = te.getMemoryInSlot(i);
				if (memory.getItem() instanceof ItemMemory) {
					ItemMemory itemMemory = (ItemMemory)memory.getItem();
					pbr.render(itemMemory.getTierColor(memory), i, 0f, 0.5f);
				}
			}
		}
		tess.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();

		RayTraceResult mop = Minecraft.getMinecraft().objectMouseOver;
		if (mop != null && mop.typeOfHit == Type.BLOCK && mop.getBlockPos().equals(te.getPos())) {
			if (te.getBlockType() instanceof BlockMemoryBay) {
				BlockMemoryBay block = (BlockMemoryBay)te.getBlockType();
				float hitX = (float)(mop.hitVec.xCoord-te.getPos().getX());
				float hitY = (float)(mop.hitVec.yCoord-te.getPos().getY());
				float hitZ = (float)(mop.hitVec.zCoord-te.getPos().getZ());
				int slot = block.getLookedAtSlot(bs, mop.sideHit, hitX, hitY, hitZ);
				if (slot != -1 && te.hasMemoryInSlot(slot)) {
					ItemStack memory = te.getMemoryInSlot(slot);
					if (memory.hasDisplayName()) {
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
						renderDummy.renderLivingLabel(DUMMY_ENTITY, memory.getDisplayName(), (x+nameX)*2, (y+nameY)*2, (z+nameZ)*2, 64);
						GlStateManager.popMatrix();
					}
				}
			}
		}
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldX, oldY);
	}

}
