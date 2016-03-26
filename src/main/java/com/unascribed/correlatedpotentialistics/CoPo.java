package com.unascribed.correlatedpotentialistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.unascribed.correlatedpotentialistics.block.BlockController;
import com.unascribed.correlatedpotentialistics.block.BlockDriveBay;
import com.unascribed.correlatedpotentialistics.block.BlockVT;
import com.unascribed.correlatedpotentialistics.block.item.ItemBlockController;
import com.unascribed.correlatedpotentialistics.block.item.ItemBlockDriveBay;
import com.unascribed.correlatedpotentialistics.block.item.ItemBlockVT;
import com.unascribed.correlatedpotentialistics.compat.WailaCompatibility;
import com.unascribed.correlatedpotentialistics.item.ItemDrive;
import com.unascribed.correlatedpotentialistics.item.ItemMisc;
import com.unascribed.correlatedpotentialistics.network.CoPoGuiHandler;
import com.unascribed.correlatedpotentialistics.network.SetSearchQueryMessage;
import com.unascribed.correlatedpotentialistics.tile.TileEntityController;
import com.unascribed.correlatedpotentialistics.tile.TileEntityDriveBay;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid="correlatedpotentialistics", name="Correlated Potentialistics", version="0.1.1")
public class CoPo {
	public static Logger log;
	
	@Instance
	public static CoPo inst;
	@SidedProxy(clientSide="com.unascribed.correlatedpotentialistics.client.ClientProxy", serverSide="com.unascribed.correlatedpotentialistics.Proxy")
	public static Proxy proxy;
	
	public static BlockController controller;
	public static BlockDriveBay drive_bay;
	public static BlockVT vt;
	
	public static ItemMisc misc;
	public static ItemDrive drive;
	
	public static CreativeTabs creativeTab = new CreativeTabs("correlatedPotentialistics") {
		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(controller);
		}
	};
	
	public SimpleNetworkWrapper network;
	
	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		log = LogManager.getLogger("CorrelatedPotentialistics");
		
		// for some reason plugin message channels have a maximum length of 20 characters
		network = NetworkRegistry.INSTANCE.newSimpleChannel("CrelatedPtntialstics");
		network.registerMessage(SetSearchQueryMessage.class, SetSearchQueryMessage.class, 0, Side.SERVER);
		
		register(new BlockController(), ItemBlockController.class, "controller", 4);
		register(new BlockDriveBay(), ItemBlockDriveBay.class, "drive_bay", 0);
		register(new BlockVT(), ItemBlockVT.class, "vt", 0);
		
		register(new ItemMisc(), "misc", -2);
		register(new ItemDrive(), "drive", -1);
		
		CRecipes.register();
		
		ItemStack processor = new ItemStack(CoPo.misc, 1, 0);
		ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, new WeightedRandomChestContent(processor, 1, 4, 4));
		ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_LIBRARY, new WeightedRandomChestContent(processor, 1, 1, 3));
		ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CROSSING, new WeightedRandomChestContent(processor, 1, 1, 2));
		ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new WeightedRandomChestContent(processor, 1, 1, 1));
		ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(processor, 1, 1, 2));
		ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, new WeightedRandomChestContent(processor, 1, 1, 3));
		
		GameRegistry.registerTileEntity(TileEntityController.class, "correlatedpotentialistics:controller");
		GameRegistry.registerTileEntity(TileEntityDriveBay.class, "correlatedpotentialistics:drive_bay");
		GameRegistry.registerTileEntity(TileEntityVT.class, "correlatedpotentialistics:vt");
		if (Loader.isModLoaded("Waila")) {
			WailaCompatibility.init();
		}
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new CoPoGuiHandler());
		proxy.preInit();
	}

	private void register(Block block, Class<? extends ItemBlock> item, String name, int itemVariants) {
		block.setUnlocalizedName("correlatedpotentialistics."+name);
		block.setCreativeTab(creativeTab);
		GameRegistry.registerBlock(block, item, name);
		proxy.registerItemModel(Item.getItemFromBlock(block), itemVariants);
		try {
			this.getClass().getField(name).set(this, block);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void register(Item item, String name, int variants) {
		item.setUnlocalizedName("correlatedpotentialistics."+name);
		item.setCreativeTab(creativeTab);
		GameRegistry.registerItem(item, name);
		proxy.registerItemModel(item, variants);
		try {
			this.getClass().getField(name).set(this, item);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
