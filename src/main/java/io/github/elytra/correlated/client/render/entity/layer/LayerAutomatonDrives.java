package io.github.elytra.correlated.client.render.entity.layer;

import org.lwjgl.opengl.GL11;

import io.github.elytra.correlated.Correlated;
import io.github.elytra.correlated.client.render.tile.RenderDriveBay;
import io.github.elytra.correlated.entity.EntityAutomaton;
import io.github.elytra.correlated.item.ItemDrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LayerAutomatonDrives implements LayerRenderer<EntityAutomaton> {
	private static final ResourceLocation DRIVE = new ResourceLocation("correlated", "textures/misc/drive.png");
	
	private final EntityEquipmentSlot[] slots = {
			EntityEquipmentSlot.CHEST,
			EntityEquipmentSlot.LEGS
		};
	
	@Override
	public void doRenderLayer(EntityAutomaton ent, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.rotate(netHeadYaw, 0, 1, 0);
		GlStateManager.rotate(180, 1, 0, 0);
		GlStateManager.translate(-0.787f, -1.978f, -0.282f);
		float s = 1.145f;
		GlStateManager.scale(s, s, s);
		GlStateManager.enableTexture2D();
		Minecraft.getMinecraft().getTextureManager().bindTexture(DRIVE);

		
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer wr = tess.getBuffer();
		float oldX = OpenGlHelper.lastBrightnessX;
		float oldY = OpenGlHelper.lastBrightnessY;

		boolean lit = ent.isPowered();

		int light = ent.getBrightnessForRender(partialTicks);
		int j = light % 65536;
		int k = light / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);

		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (EntityEquipmentSlot slot : slots) {
			if (ent.getItemStackFromSlot(slot) != null) {
				ItemStack drive = ent.getItemStackFromSlot(slot);
				if (drive.getItem() instanceof ItemDrive) {
					ItemDrive itemDrive = (ItemDrive)drive.getItem();
					RenderDriveBay.pbr.render(itemDrive.getBaseColor(drive), slot == EntityEquipmentSlot.CHEST ? 0 : 2, 0, 0);
				}
			}
		}
		tess.draw();

		if (lit) {
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			GlStateManager.disableLighting();
		}
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (EntityEquipmentSlot slot : slots) {
			if (ent.getItemStackFromSlot(slot) != null) {
				ItemStack drive = ent.getItemStackFromSlot(slot);
				if (drive.getItem() instanceof ItemDrive) {
					ItemDrive itemDrive = (ItemDrive)drive.getItem();
					RenderDriveBay.pbr.render(lit ? itemDrive.getFullnessColor(drive) : Correlated.proxy.getColor("other", 48), slot == EntityEquipmentSlot.CHEST ? 0 : 2, 0, 0.5f);
				}
			}
		}
		tess.draw();

		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (EntityEquipmentSlot slot : slots) {
			if (ent.getItemStackFromSlot(slot) != null) {
				ItemStack drive = ent.getItemStackFromSlot(slot);
				if (drive.getItem() instanceof ItemDrive) {
					ItemDrive itemDrive = (ItemDrive)drive.getItem();
					RenderDriveBay.pbr.render(itemDrive.getTierColor(drive), slot == EntityEquipmentSlot.CHEST ? 0 : 2, 0.5f, 0);
				}
			}
		}
		tess.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldX, oldY);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

}
