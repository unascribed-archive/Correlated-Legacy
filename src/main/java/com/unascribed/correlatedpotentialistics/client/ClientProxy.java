package com.unascribed.correlatedpotentialistics.client;

import java.util.List;

import com.google.common.collect.Lists;
import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.Proxy;
import com.unascribed.correlatedpotentialistics.client.render.RenderController;
import com.unascribed.correlatedpotentialistics.client.render.RenderDriveBay;
import com.unascribed.correlatedpotentialistics.client.render.RenderVT;
import com.unascribed.correlatedpotentialistics.client.render.RenderWirelessReceiver;
import com.unascribed.correlatedpotentialistics.client.render.RenderWirelessTransmitter;
import com.unascribed.correlatedpotentialistics.item.ItemMisc;
import com.unascribed.correlatedpotentialistics.tile.TileEntityController;
import com.unascribed.correlatedpotentialistics.tile.TileEntityDriveBay;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT;
import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessReceiver;
import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessTransmitter;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.registry.GameData;

public class ClientProxy extends Proxy {
	public static float ticks = 0;
	@SuppressWarnings("deprecation")
	@Override
	public void preInit() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityController.class, new RenderController());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDriveBay.class, new RenderDriveBay());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVT.class, new RenderVT());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWirelessReceiver.class, new RenderWirelessReceiver());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWirelessTransmitter.class, new RenderWirelessTransmitter());
		
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CoPo.wireless_endpoint), 0, TileEntityWirelessReceiver.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CoPo.wireless_endpoint), 1, TileEntityWirelessTransmitter.class);

		MinecraftForge.EVENT_BUS.register(this);

		int idx = 0;
		for (String s : ItemMisc.items) {
			ModelLoader.setCustomModelResourceLocation(CoPo.misc, idx++, new ModelResourceLocation(new ResourceLocation("correlatedpotentialistics", s), "inventory"));
		}
	}
	@Override
	public void registerItemModel(Item item, int variants) {
		ResourceLocation loc = GameData.getItemRegistry().getNameForObject(item);
		if (variants < -1) {
			variants = (variants*-1)-1;
			loc = new ResourceLocation("correlatedpotentialistics", "tesrstack");
		}
		if (variants == -1) {
			List<ItemStack> li = Lists.newArrayList();
			item.getSubItems(item, CoPo.creativeTab, li);
			for (ItemStack is : li) {
				ModelLoader.setCustomModelResourceLocation(item, is.getItemDamage(), new ModelResourceLocation(loc, "inventory"));
			}
		} else if (variants == 0) {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(loc, "inventory"));
		} else if (variants > 0) {
			for (int i = 0; i < variants; i++) {
				ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(loc, "inventory"+i));
			}
		}
	}
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (e.phase == Phase.START) {
			ticks++;
		}
	}
	@SubscribeEvent
	public void onRenderTick(RenderTickEvent e) {
		if (e.phase == Phase.START) {
			ticks = ((int)ticks)+e.renderTickTime;
		}
	}
	@SubscribeEvent
	public void onStitch(TextureStitchEvent.Pre e) {
		e.map.registerSprite(new ResourceLocation("correlatedpotentialistics", "blocks/wireless_endpoint_error"));
		e.map.registerSprite(new ResourceLocation("correlatedpotentialistics", "blocks/wireless_endpoint_linked"));
	}
}
