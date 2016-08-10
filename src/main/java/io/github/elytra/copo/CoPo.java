package io.github.elytra.copo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import io.github.elytra.copo.block.BlockController;
import io.github.elytra.copo.block.BlockDriveBay;
import io.github.elytra.copo.block.BlockInterface;
import io.github.elytra.copo.block.BlockVT;
import io.github.elytra.copo.block.BlockWirelessEndpoint;
import io.github.elytra.copo.block.item.ItemBlockController;
import io.github.elytra.copo.block.item.ItemBlockDriveBay;
import io.github.elytra.copo.block.item.ItemBlockInterface;
import io.github.elytra.copo.block.item.ItemBlockVT;
import io.github.elytra.copo.block.item.ItemBlockWirelessEndpoint;
import io.github.elytra.copo.compat.WailaCompatibility;
import io.github.elytra.copo.entity.EntityAutomaton;
import io.github.elytra.copo.entity.EntityThrownItem;
import io.github.elytra.copo.item.ItemCoPoRecord;
import io.github.elytra.copo.item.ItemDrive;
import io.github.elytra.copo.item.ItemKeycard;
import io.github.elytra.copo.item.ItemMisc;
import io.github.elytra.copo.item.ItemWeldthrower;
import io.github.elytra.copo.item.ItemWirelessTerminal;
import io.github.elytra.copo.network.CoPoGuiHandler;
import io.github.elytra.copo.network.EnterDungeonMessage;
import io.github.elytra.copo.network.LeaveDungeonMessage;
import io.github.elytra.copo.network.SetAutomatonNameMessage;
import io.github.elytra.copo.network.SetGlitchingStateMessage;
import io.github.elytra.copo.network.SetSearchQueryMessage;
import io.github.elytra.copo.network.SetSlotSizeMessage;
import io.github.elytra.copo.network.StartWeldthrowingMessage;
import io.github.elytra.copo.tile.TileEntityController;
import io.github.elytra.copo.tile.TileEntityDriveBay;
import io.github.elytra.copo.tile.TileEntityInterface;
import io.github.elytra.copo.tile.TileEntityVT;
import io.github.elytra.copo.tile.TileEntityWirelessReceiver;
import io.github.elytra.copo.tile.TileEntityWirelessTransmitter;
import io.github.elytra.copo.world.LimboProvider;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.DimensionManager;
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
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid="correlatedpotentialistics", name="Correlated Potentialistics", version="@VERSION@",
	updateJSON="http://unascribed.com/update-check/correlated-potentialistics.json")
public class CoPo {
	public static Logger log;

	@Instance
	public static CoPo inst;
	@SidedProxy(clientSide="io.github.elytra.copo.client.ClientProxy", serverSide="io.github.elytra.copo.Proxy")
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
	public static ItemKeycard keycard;
	
	
	public static SoundEvent weldthrow;
	public static SoundEvent glitchbgm;
	public static SoundEvent glitchfloppy;
	public static SoundEvent glitchboot;
	public static SoundEvent convert;
	public static SoundEvent glitchtravel;
	public static SoundEvent automaton_idle;
	public static SoundEvent automaton_hurt;
	
	
	public static List<ItemCoPoRecord> recordItems = Lists.newArrayList();
	public static List<String> records = Lists.newArrayList();
	
	public static CreativeTabs creativeTab = new CreativeTabs("correlatedPotentialistics") {
		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(controller);
		}
	};

	public static int limboDimId;
	public static DimensionType limbo;

	public SimpleNetworkWrapper network;
	
	public boolean easyProcessors;
	public double defaultWirelessRange;
	
	public int controllerRfUsage;
	public int driveBayRfUsage;
	public int terminalRfUsage;
	public int interfaceRfUsage;
	public int transmitterRfUsage;
	public int receiverRfUsage;
	
	public int driveRfUsagePow;
	public int driveRfUsageDiv;
	
	public int voidDriveUsage;
	
	public boolean weldthrowerHurts;

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		log = LogManager.getLogger("CorrelatedPotentialistics");

		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
		easyProcessors = config.getBoolean("easyProcessors", "Crafting", false, "If true, processors can be crafted without finding one in a dungeon.");
		defaultWirelessRange = config.getFloat("defaultWirelessRange", "Balance", 64, 1, 65536, "The default radius of wireless transmitters, in blocks.");
		weldthrowerHurts = config.getBoolean("weldthrowerHurts", "Balance", true, "If enabled, the Weldthrower will damage mobs and set them on fire.");
		
		controllerRfUsage = config.getInt("controller", "PowerUsage", 32, 0, 640, "The RF/t used by the Controller.");
		driveBayRfUsage = config.getInt("driveBay", "PowerUsage", 8, 0, 640, "The RF/t used by the Drive Bay.");
		terminalRfUsage = config.getInt("terminal", "PowerUsage", 4, 0, 640, "The RF/t used by the Terminal.");
		interfaceRfUsage = config.getInt("interface", "PowerUsage", 12, 0, 640, "The RF/t used by the Interface.");
		transmitterRfUsage = config.getInt("transmitter", "PowerUsage", 24, 0, 640, "The RF/t used by the Wireless Transmitter.");
		receiverRfUsage = config.getInt("receiver", "PowerUsage", 24, 0, 640, "The RF/t used by the Wireless Receiver.");
		
		driveRfUsagePow = config.getInt("drivePow", "PowerUsage", 2, 0, 8, "Drive power usage is (pow**tier)/div");
		driveRfUsageDiv = config.getInt("driveDiv", "PowerUsage", 2, 0, 8, "Drive power usage is (pow**tier)/div");
		
		voidDriveUsage = config.getInt("voidDrive", "PowerUsage", 4, 0, 640, "The RF/t used by the Void Drive.");
		
		limboDimId = config.getInt("limboDimId", "IDs", -31, -256, 256, "The dimension ID for the glitch dungeon.");
		
		config.save();
		
		// for some reason plugin message channels have a maximum length of 20 characters
		network = NetworkRegistry.INSTANCE.newSimpleChannel("CrelatedPtntialstics");
		registerMessage(SetSearchQueryMessage.class, Side.SERVER);
		registerMessage(SetSearchQueryMessage.class, Side.CLIENT);
		registerMessage(SetSlotSizeMessage.class, Side.CLIENT);
		registerMessage(StartWeldthrowingMessage.class, Side.CLIENT);
		registerMessage(SetGlitchingStateMessage.class, Side.CLIENT);
		registerMessage(EnterDungeonMessage.class, Side.SERVER);
		registerMessage(SetAutomatonNameMessage.class, Side.SERVER);
		registerMessage(LeaveDungeonMessage.class, Side.SERVER);

		EntityRegistry.registerModEntity(EntityThrownItem.class, "thrown_item", 0, this, 64, 10, true);
		EntityRegistry.registerModEntity(EntityAutomaton.class, "automaton", 1, this, 64, 1, true);
		
		EntityRegistry.registerEgg(EntityAutomaton.class, 0x37474F, 0x00F8C1);
		
		limbo = DimensionType.register("Limbo", "_copodungeon", limboDimId, LimboProvider.class, false);
		DimensionManager.registerDimension(limboDimId, limbo);
		
		registerSound("weldthrow");
		registerSound("glitchbgm");
		registerSound("glitchfloppy");
		registerSound("glitchboot");
		registerSound("convert");
		registerSound("glitchtravel");
		registerSound("automaton_hurt");
		registerSound("automaton_idle");
		
		registerRecord("danslarue.xm");
		registerRecord("jesuisbaguette.xm");
		registerRecord("papillons.xm");
		registerRecord("dreidl.mod");
		registerRecord("oak.mod");
		registerRecord("king.mod");
		registerRecord("comrades.mod");
		registerRecord("devenirmondefi.mod");
		registerRecord("ngenracer.mod");
		registerRecord("sevensixteen.mod");
		registerRecord("ombres.mod");
		registerRecord("sacrecharlemagne.mod");
		registerRecord("danone.mod");
		registerRecord("spark.mod");
		registerRecord("genesis.mod");
		registerRecord("greyatari.mod");
		registerRecord("ella.mod");
		registerRecord("framboise.mod");
		registerRecord("grecque.mod");
		registerRecord("que.mod");
		registerRecord("suddenlyisee.mod");
		registerRecord("sixsixtythreefoureightytwo.mod");
		registerRecord("pinkssideoftown.mod");
		registerRecord("thirteen.mod");
		registerRecord("irokos.mod");
		
		register(new BlockController().setHardness(2), ItemBlockController.class, "controller", 4);
		register(new BlockDriveBay().setHardness(2), ItemBlockDriveBay.class, "drive_bay", 0);
		register(new BlockVT().setHardness(2), ItemBlockVT.class, "vt", 0);
		register(new BlockInterface().setHardness(2), ItemBlockInterface.class, "iface", 0);
		register(new BlockWirelessEndpoint().setHardness(2), ItemBlockWirelessEndpoint.class, "wireless_endpoint", -4);

		register(new ItemMisc(), "misc", -2);
		register(new ItemDrive(), "drive", -1);
		register(new ItemWirelessTerminal(), "wireless_terminal", 0);
		register(new ItemWeldthrower(), "weldthrower", 0);
		register(new ItemKeycard(), "keycard", -2);

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
	
	private int discriminator = 0;
	
	private <T extends IMessage & IMessageHandler<T, IMessage>> void registerMessage(Class<T> msg, Side side) {
		network.registerMessage(msg, msg, discriminator++, side);
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
	
	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent e) {
		CoPoWorldData d = getDataFor(e.player.worldObj);
		UUID id = e.player.getGameProfile().getId();
		if (d.getPlayerRespawnData().containsKey(id)) {
			// for BTM
			//e.player.readFromNBT(d.getPlayerRespawnData().remove(id));
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

	private void registerRecord(String str) {
		try {
			records.add(str);
			String basename = str.substring(0, str.indexOf('.'));
			ResourceLocation loc = new ResourceLocation("correlatedpotentialistics", basename);
			SoundEvent snd = new SoundEvent(loc);
			GameRegistry.register(snd, loc);
			ItemCoPoRecord item = new ItemCoPoRecord(basename, snd);
			item.setRegistryName(basename+"_record");
			item.setUnlocalizedName("record");
			item.setCreativeTab(creativeTab);
			GameRegistry.register(item);
			proxy.registerItemModel(item, 0);
			recordItems.add(item);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registerSound(String str) {
		ResourceLocation loc = new ResourceLocation("correlatedpotentialistics", str);
		SoundEvent snd = new SoundEvent(loc);
		GameRegistry.register(snd, loc);
		try {
			Field f = this.getClass().getField(str);
			f.set(null, snd);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

