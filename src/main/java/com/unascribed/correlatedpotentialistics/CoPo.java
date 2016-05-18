package com.unascribed.correlatedpotentialistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;
import com.unascribed.correlatedpotentialistics.block.BlockController;
import com.unascribed.correlatedpotentialistics.block.BlockDriveBay;
import com.unascribed.correlatedpotentialistics.block.BlockInterface;
import com.unascribed.correlatedpotentialistics.block.BlockVT;
import com.unascribed.correlatedpotentialistics.block.BlockWirelessEndpoint;
import com.unascribed.correlatedpotentialistics.block.item.ItemBlockController;
import com.unascribed.correlatedpotentialistics.block.item.ItemBlockDriveBay;
import com.unascribed.correlatedpotentialistics.block.item.ItemBlockInterface;
import com.unascribed.correlatedpotentialistics.block.item.ItemBlockVT;
import com.unascribed.correlatedpotentialistics.block.item.ItemBlockWirelessEndpoint;
import com.unascribed.correlatedpotentialistics.compat.WailaCompatibility;
import com.unascribed.correlatedpotentialistics.item.ItemDrive;
import com.unascribed.correlatedpotentialistics.item.ItemMisc;
import com.unascribed.correlatedpotentialistics.item.ItemWeldthrower;
import com.unascribed.correlatedpotentialistics.item.ItemWirelessTerminal;
import com.unascribed.correlatedpotentialistics.network.CoPoGuiHandler;
import com.unascribed.correlatedpotentialistics.network.SetSearchQueryMessage;
import com.unascribed.correlatedpotentialistics.network.SetSlotSizeMessage;
import com.unascribed.correlatedpotentialistics.tile.TileEntityController;
import com.unascribed.correlatedpotentialistics.tile.TileEntityDriveBay;
import com.unascribed.correlatedpotentialistics.tile.TileEntityInterface;
import com.unascribed.correlatedpotentialistics.tile.TileEntityVT;
import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessReceiver;
import com.unascribed.correlatedpotentialistics.tile.TileEntityWirelessTransmitter;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid="correlatedpotentialistics", name="Correlated Potentialistics", version="@VERSION@",
	updateJSON="http://unascribed.com/update-check/correlated-potentialistics.json")
public class CoPo {
	public static Logger log;

	@Instance
	public static CoPo inst;
	@SidedProxy(clientSide="com.unascribed.correlatedpotentialistics.client.ClientProxy", serverSide="com.unascribed.correlatedpotentialistics.Proxy")
	public static Proxy proxy;

	public static BlockController controller;
	public static BlockDriveBay drive_bay;
	public static BlockVT vt;
	public static BlockInterface iface;
	public static BlockWirelessEndpoint wireless_endpoint;

	public static ItemMisc misc;
	public static ItemDrive drive;
	public static ItemWirelessTerminal wireless_terminal;
	public static ItemWeldthrower weldthrower;
	
	public static SoundEvent weldthrow;

	public static CreativeTabs creativeTab = new CreativeTabs("correlatedPotentialistics") {
		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(controller);
		}
	};

	public SimpleNetworkWrapper network;
	
	public boolean easyProcessors;

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		log = LogManager.getLogger("CorrelatedPotentialistics");

		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
		easyProcessors = config.getBoolean("easyProcessors", "Crafting", false, "If true, processors can be crafted without finding one in a dungeon.");
		config.save();
		
		// for some reason plugin message channels have a maximum length of 20 characters
		network = NetworkRegistry.INSTANCE.newSimpleChannel("CrelatedPtntialstics");
		network.registerMessage(SetSearchQueryMessage.class, SetSearchQueryMessage.class, 0, Side.SERVER);
		network.registerMessage(SetSearchQueryMessage.class, SetSearchQueryMessage.class, 1, Side.CLIENT);
		network.registerMessage(SetSlotSizeMessage.class, SetSlotSizeMessage.class, 2, Side.CLIENT);

		register(new BlockController().setHardness(2), ItemBlockController.class, "controller", 4);
		register(new BlockDriveBay().setHardness(2), ItemBlockDriveBay.class, "drive_bay", 0);
		register(new BlockVT().setHardness(2), ItemBlockVT.class, "vt", 0);
		register(new BlockInterface().setHardness(2), ItemBlockInterface.class, "iface", 0);
		register(new BlockWirelessEndpoint().setHardness(2), ItemBlockWirelessEndpoint.class, "wireless_endpoint", -4);

		register(new ItemMisc(), "misc", -2);
		register(new ItemDrive(), "drive", -1);
		register(new ItemWirelessTerminal(), "wireless_terminal", 0);
		register(new ItemWeldthrower(), "weldthrower", 0);
		
		ResourceLocation loc = new ResourceLocation("correlatedpotentialistics", "weldthrow");
		GameRegistry.register(weldthrow = new SoundEvent(loc), loc);

		CRecipes.register();

		GameRegistry.registerTileEntity(TileEntityController.class, "correlatedpotentialistics:controller");
		GameRegistry.registerTileEntity(TileEntityDriveBay.class, "correlatedpotentialistics:drive_bay");
		GameRegistry.registerTileEntity(TileEntityVT.class, "correlatedpotentialistics:vt");
		GameRegistry.registerTileEntity(TileEntityInterface.class, "correlatedpotentialistics:interface");
		GameRegistry.registerTileEntity(TileEntityWirelessReceiver.class, "correlatedpotentialistics:wireless_receiver");
		GameRegistry.registerTileEntity(TileEntityWirelessTransmitter.class, "correlatedpotentialistics:wireless_transmitter");
		if (Loader.isModLoaded("Waila")) {
			WailaCompatibility.init();
		}
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new CoPoGuiHandler());
		MinecraftForge.EVENT_BUS.register(this);
		proxy.preInit();
	}
	
	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.postInit();
	}
	
	@SubscribeEvent
	public void onLootAdd(LootTableLoadEvent e) {
		if (e.getName().toString().startsWith("minecraft:chests/")) {
			e.getTable().getPool("main").addEntry(new LootEntryItem(misc, 45, 0, new LootFunction[0], new LootCondition[0], "correlatedpotentialistics:processor"));
		}
	}
	
	public static CoPoWorldData getDataFor(World w) {
		CoPoWorldData data = (CoPoWorldData)w.getPerWorldStorage().getOrLoadData(CoPoWorldData.class, "correlatedpotentialistics");
		if (data == null) {
			data = new CoPoWorldData("correlatedpotentialistics");
			w.getPerWorldStorage().setData("correlatedpotentialistics", data);
		}
		return data;
	}

	private void register(Block block, Class<? extends ItemBlock> item, String name, int itemVariants) {
		block.setUnlocalizedName("correlatedpotentialistics."+name);
		block.setCreativeTab(creativeTab);
		block.setRegistryName(name);
		GameRegistry.register(block);
		try {
			ItemBlock ib = item.getConstructor(Block.class).newInstance(block);
			ib.setRegistryName(name);
			GameRegistry.register(ib);
		} catch (Exception e1) {
			Throwables.propagate(e1);
		}
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
		item.setRegistryName(name);
		GameRegistry.register(item);
		proxy.registerItemModel(item, variants);
		try {
			this.getClass().getField(name).set(this, item);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
