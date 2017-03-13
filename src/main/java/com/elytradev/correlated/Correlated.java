package com.elytradev.correlated;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elytradev.correlated.block.BlockController;
import com.elytradev.correlated.block.BlockDriveBay;
import com.elytradev.correlated.block.BlockDecor;
import com.elytradev.correlated.block.BlockGlowingDecor;
import com.elytradev.correlated.block.BlockGlowingDecor.Variant;
import com.elytradev.correlated.block.BlockGlowingDecorSlab;
import com.elytradev.correlated.block.BlockDecorSlab;
import com.elytradev.correlated.block.BlockDecorStairs;
import com.elytradev.correlated.block.BlockImporterChest;
import com.elytradev.correlated.block.BlockInterface;
import com.elytradev.correlated.block.BlockMemoryBay;
import com.elytradev.correlated.block.BlockWireless;
import com.elytradev.correlated.block.BlockTerminal;
import com.elytradev.correlated.block.item.ItemBlockController;
import com.elytradev.correlated.block.item.ItemBlockDriveBay;
import com.elytradev.correlated.block.item.ItemBlockDecor;
import com.elytradev.correlated.block.item.ItemBlockGlowingDecor;
import com.elytradev.correlated.block.item.ItemBlockInterface;
import com.elytradev.correlated.block.item.ItemBlockMemoryBay;
import com.elytradev.correlated.block.item.ItemBlockWireless;
import com.elytradev.correlated.block.item.ItemBlockTerminal;
import com.elytradev.correlated.crafting.CRecipes;
import com.elytradev.correlated.crafting.DriveRecipe;
import com.elytradev.correlated.entity.EntityAutomaton;
import com.elytradev.correlated.entity.EntityThrownItem;
import com.elytradev.correlated.entity.automaton.Opcode;
import com.elytradev.correlated.function.Consumer;
import com.elytradev.correlated.item.ItemCorrelatedRecord;
import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.item.ItemFloppy;
import com.elytradev.correlated.item.ItemKeycard;
import com.elytradev.correlated.item.ItemMemory;
import com.elytradev.correlated.item.ItemMisc;
import com.elytradev.correlated.item.ItemModule;
import com.elytradev.correlated.item.ItemWeldthrower;
import com.elytradev.correlated.item.ItemWirelessTerminal;
import com.elytradev.correlated.network.APNRequestMessage;
import com.elytradev.correlated.network.APNResponseMessage;
import com.elytradev.correlated.network.AddStatusLineMessage;
import com.elytradev.correlated.network.AutomatonSpeakMessage;
import com.elytradev.correlated.network.ChangeAPNMessage;
import com.elytradev.correlated.network.CorrelatedGuiHandler;
import com.elytradev.correlated.network.EnterDungeonMessage;
import com.elytradev.correlated.network.InsertAllMessage;
import com.elytradev.correlated.network.LeaveDungeonMessage;
import com.elytradev.correlated.network.RecipeTransferMessage;
import com.elytradev.correlated.network.SaveProgramMessage;
import com.elytradev.correlated.network.SetAutomatonNameMessage;
import com.elytradev.correlated.network.SetEditorStatusMessage;
import com.elytradev.correlated.network.SetGlitchingStateMessage;
import com.elytradev.correlated.network.SetSearchQueryClientMessage;
import com.elytradev.correlated.network.SetSearchQueryServerMessage;
import com.elytradev.correlated.network.SetSlotSizeMessage;
import com.elytradev.correlated.network.ShowTerminalErrorMessage;
import com.elytradev.correlated.network.SignalStrengthMessage;
import com.elytradev.correlated.network.StartWeldthrowingMessage;
import com.elytradev.correlated.proxy.Proxy;
import com.elytradev.correlated.tile.TileEntityBeaconLens;
import com.elytradev.correlated.tile.TileEntityController;
import com.elytradev.correlated.tile.TileEntityDriveBay;
import com.elytradev.correlated.tile.TileEntityImporterChest;
import com.elytradev.correlated.tile.TileEntityInterface;
import com.elytradev.correlated.tile.TileEntityMemoryBay;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;
import com.elytradev.correlated.tile.TileEntityNetworkImporter;
import com.elytradev.correlated.tile.TileEntityOldWirelessReceiver;
import com.elytradev.correlated.tile.TileEntityOldWirelessTransmitter;
import com.elytradev.correlated.tile.TileEntityOpticalReceiver;
import com.elytradev.correlated.tile.TileEntityPotentialisticsImporter;
import com.elytradev.correlated.tile.TileEntityTerminal;
import com.elytradev.correlated.tile.TileEntityVTImporter;
import com.elytradev.correlated.world.LimboProvider;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import com.elytradev.probe.api.IProbeDataProvider;

import com.elytradev.concrete.NetworkContext;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.Type;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

@Mod(modid="correlated", name="Correlated", version="@VERSION@",
	updateJSON="http://unascribed.com/update-check/correlated.json")
public class Correlated {
	public static Logger log;

	@Instance
	public static Correlated inst;
	@SidedProxy(clientSide="com.elytradev.correlated.proxy.ClientProxy", serverSide="com.elytradev.correlated.proxy.Proxy")
	public static Proxy proxy;

	
	@CapabilityInject(IProbeDataProvider.class)
	public static Capability<?> PROBE;
	

	public static BlockController controller;
	public static BlockDriveBay drive_bay;
	public static BlockMemoryBay memory_bay;
	public static BlockTerminal terminal;
	public static BlockInterface iface;
	public static BlockImporterChest importer_chest;
	public static BlockWireless wireless;
	public static BlockDecor decor_block;
	public static BlockGlowingDecor glowing_decor_block;
	
	public static BlockDecorStairs dungeoncrete_stairs;
	public static BlockDecorStairs dungeoncrete_grate_stairs;
	public static BlockDecorStairs dungeoncrete_largetile_stairs;
	public static BlockDecorStairs dungeoncrete_vertical_stairs;
	public static BlockDecorStairs elucid_brick_stairs;
	public static BlockDecorStairs elucid_grit_stairs;
	public static BlockDecorStairs elucid_scale_stairs;
	public static BlockDecorStairs plating_stairs;
	public static BlockDecorStairs lithographene_off_stairs;
	public static BlockDecorStairs lithographene_off_variant_stairs;
	public static BlockDecorStairs lithographene_on_stairs;
	public static BlockDecorStairs lithographene_on_variant_stairs;
	
	public static BlockDecorSlab decor_slab;
	public static BlockGlowingDecorSlab glowing_decor_slab;
	public static BlockDecorSlab decor_double_slab;
	public static BlockGlowingDecorSlab glowing_decor_double_slab;

	public static ItemMisc misc;
	public static ItemDrive drive;
	public static ItemMemory memory;
	public static ItemModule module;
	public static ItemFloppy floppy;
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
	public static SoundEvent drive_disassemble;
	public static SoundEvent data_core_shatter;
	
	
	public static List<ItemCorrelatedRecord> recordItems = Lists.newArrayList();
	public static List<String> records = Lists.newArrayList();
	
	public static CreativeTabs creativeTab = new CreativeTabs("correlated") {
		ItemStack stack = null;
		@Override
		public ItemStack getTabIconItem() {
			if (stack == null) {
				stack = new ItemStack(misc, 1, 9);
			}
			return stack;
		}
	};

	public static int limboDimId;
	public static DimensionType limbo;

	public NetworkContext network;
	
	public boolean easyProcessors;
	public double defaultWirelessRange;
	
	public int controllerRfUsage;
	public int driveBayRfUsage;
	public int memoryBayRfUsage;
	public int terminalRfUsage;
	public int interfaceRfUsage;
	public int beamRfUsage;
	public int opticalRfUsage;
	
	public int controllerCapacity;
	public int controllerCap;
	public int controllerErrorUsage_MultipleControllers;
	public int controllerErrorUsage_NetworkTooBig;
	
	public int driveRfUsagePow;
	public int driveRfUsageDiv;
	
	public int voidDriveUsage;
	
	public boolean weldthrowerHurts;
	
	public boolean refundContent;
	public boolean refundBlocks;
	public boolean refundComponents;
	public boolean importNetworks;
	
	public boolean jeiAvailable = false;

	public Consumer<String> jeiQueryUpdater = (s) -> {};
	public Supplier<String> jeiQueryReader = () -> "";
	

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		log = LogManager.getLogger("Correlated");

		// TODO this badly needs cleanup
		
		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
		easyProcessors = config.getBoolean("easyProcessors", "Crafting", false, "If true, processors can be crafted without going to the limbo dungeon. Not recommended.");
		
		defaultWirelessRange = config.getFloat("defaultWirelessRange", "Balance", 64, 1, 65536, "The default radius of wireless transmitters, in blocks.");
		weldthrowerHurts = config.getBoolean("weldthrowerHurts", "Balance", true, "If enabled, the Weldthrower will damage mobs and set them on fire.");
		
		controllerRfUsage = config.getInt("controller", "PowerUsage", 32, 0, Integer.MAX_VALUE, "The FU/t used by the Controller.");
		driveBayRfUsage = config.getInt("driveBay", "PowerUsage", 8, 0, 640, "The FU/t used by the Drive Bay.");
		memoryBayRfUsage = config.getInt("memoryBay", "PowerUsage", 4, 0, 640, "The FU/t used by the Memory Bay.");
		terminalRfUsage = config.getInt("terminal", "PowerUsage", 4, 0, 640, "The FU/t used by the Terminal.");
		interfaceRfUsage = config.getInt("interface", "PowerUsage", 8, 0, 640, "The FU/t used by the Interface.");
		beamRfUsage = config.getInt("beam", "PowerUsage", 24, 0, 640, "The FU/t used by the Microwave Beam.");
		opticalRfUsage = config.getInt("optical", "PowerUsage", 24, 0, 640, "The FU/t used by the Optical Receiver.");
		
		controllerCapacity = config.getInt("controllerCapacity", "PowerFineTuning", 64000, 0, Integer.MAX_VALUE, "The FU stored by the controller.");
		controllerCap = config.getInt("controllerCap", "PowerFineTuning", 640, 0, Integer.MAX_VALUE, "The maximum FU/t the controller can use, and therefore a network.");
		controllerErrorUsage_MultipleControllers = config.getInt("controllerErrorUsage_MultipleControllers", "PowerFineTuning", 4, 0, Integer.MAX_VALUE, "The FU/t used by the controller when it detects another controller in its network and is erroring.");
		controllerErrorUsage_NetworkTooBig = config.getInt("controllerErrorUsage_NetworkTooBig", "PowerFineTuning", 640, 0, Integer.MAX_VALUE, "The FU/t used by the controller when it reaches the network scan limit.");
		
		driveRfUsagePow = config.getInt("drivePow", "PowerUsage", 2, 0, 8, "Drive power usage is (pow**tier)/div");
		driveRfUsageDiv = config.getInt("driveDiv", "PowerUsage", 2, 0, 8, "Drive power usage is (pow**tier)/div");
		
		voidDriveUsage = config.getInt("voidDrive", "PowerUsage", 4, 0, 640, "The FU/t used by the Void Drive.");
		
		limboDimId = config.getInt("limboDimId", "IDs", -31, -256, 256, "The dimension ID for the glitch dungeon.");
		
		String importModeStr = config.getString("mode", "Import", "refund_all",
				"The mode for the old network importer, which will run on any 1.x networks loaded with Correlated 2.x. Possible values are:\n"
				+ "refund_all: Refund components, convert drives into Data Cores, and refund Interface contents. [default]\n"
				+ "refund_some: Convert drives into Data Cores and refund Interface contents, but do not refund crafting ingredients. Useful if you used MineTweaker to change the recipes. Blocks will still be refunded.\n"
				+ "refund_content: Convert drives into Data Cores and refund Interface contents, but do not refund anything else.\n"
				+ "destroy: Outright delete the network, and all items that were contained in it. If you use this option, PLEASE state it prominently on your modpack page, and warn people.\n"
				+ "leave: Leave the network alone. May result in glitchy drives holding more data than they should be able to, crashes, and general strangeness. Not recommended.\n\n");
		switch (importModeStr.toLowerCase(Locale.ROOT).trim()) {
			default:
				log.warn("Import mode set to unknown value {}, assuming refund_all", importModeStr);
			case "refund_all": {
				refundContent = true;
				refundBlocks = true;
				refundComponents = true;
				importNetworks = true;
				break;
			}
			case "refund_some": {
				refundContent = true;
				refundBlocks = true;
				refundComponents = false;
				importNetworks = true;
				break;
			}
			case "refund_content": {
				refundContent = true;
				refundBlocks = false;
				refundComponents = false;
				importNetworks = true;
				break;
			}
			case "destroy": {
				log.warn("Network importer mode is set to DESTROY. Old CoPo 1.x networks WILL BE LOST FOREVER.");
				refundContent = false;
				refundBlocks = false;
				refundComponents = false;
				importNetworks = true;
				break;
			}
			case "leave": {
				importNetworks = false;
				break;
			}
		}
		
		
		config.save();
		
		network = NetworkContext.forChannel("Correlated");
		network.register(SetSearchQueryClientMessage.class);
		network.register(SetSearchQueryServerMessage.class);
		network.register(SetSlotSizeMessage.class);
		network.register(StartWeldthrowingMessage.class);
		network.register(SetGlitchingStateMessage.class);
		network.register(EnterDungeonMessage.class);
		network.register(SetAutomatonNameMessage.class);
		network.register(LeaveDungeonMessage.class);
		network.register(AddStatusLineMessage.class);
		network.register(AutomatonSpeakMessage.class);
		network.register(SetEditorStatusMessage.class);
		network.register(SaveProgramMessage.class);
		network.register(RecipeTransferMessage.class);
		network.register(ShowTerminalErrorMessage.class);
		network.register(InsertAllMessage.class);
		network.register(ChangeAPNMessage.class);
		network.register(SignalStrengthMessage.class);
		network.register(APNRequestMessage.class);
		network.register(APNResponseMessage.class);

		EntityRegistry.registerModEntity(new ResourceLocation("correlated", "thrown_item"), EntityThrownItem.class, "thrown_item", 0, this, 64, 10, true);
		EntityRegistry.registerModEntity(new ResourceLocation("correlated", "automaton"), EntityAutomaton.class, "automaton", 1, this, 64, 1, true);
		
		EntityRegistry.registerEgg(new ResourceLocation("correlated", "automaton"), 0x37474F, 0x00F8C1);
		
		limbo = DimensionType.register("Limbo", "_correlateddungeon", limboDimId, LimboProvider.class, false);
		DimensionManager.registerDimension(limboDimId, limbo);
		
		registerSound("weldthrow");
		registerSound("glitchbgm");
		registerSound("glitchfloppy");
		registerSound("glitchboot");
		registerSound("convert");
		registerSound("glitchtravel");
		registerSound("automaton_hurt");
		registerSound("automaton_idle");
		registerSound("drive_disassemble");
		registerSound("data_core_shatter");
		
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
		
		register(new BlockController().setHardness(2), ItemBlockController.class, "controller", 16);
		register(new BlockDriveBay().setHardness(2), ItemBlockDriveBay.class, "drive_bay", 0);
		register(new BlockMemoryBay().setHardness(2), ItemBlockMemoryBay.class, "memory_bay", 0);
		register(new BlockTerminal().setHardness(2), ItemBlockTerminal.class, "terminal", 0);
		register(new BlockInterface().setHardness(2), ItemBlockInterface.class, "iface", 0);
		register(new BlockImporterChest().setHardness(2), null, "importer_chest", 0);
		register(new BlockWireless().setHardness(2), ItemBlockWireless.class, "wireless", 3);
		
		register(new BlockDecor().setHardness(2), ItemBlockDecor.class, "decor_block", BlockDecor.Variant.VALUES.length);
		register(new BlockGlowingDecor().setHardness(2), ItemBlockGlowingDecor.class, "glowing_decor_block", BlockGlowingDecor.Variant.VALUES.length);
		
		for (BlockDecor.Variant v : BlockDecor.Variant.VALUES) {
			register(new BlockDecorStairs(decor_block.getDefaultState().withProperty(BlockDecor.variant, v)), ItemBlock.class, v.getName()+"_stairs", 0);
		}
		for (BlockGlowingDecor.Variant v : BlockGlowingDecor.Variant.VALUES) {
			if (v == Variant.LANTERN) continue;
			register(new BlockDecorStairs(glowing_decor_block.getDefaultState().withProperty(BlockGlowingDecor.variant, v)), ItemBlock.class, v.getName()+"_stairs", 0);
		}
		
		register(new BlockDecorSlab(false).setHardness(2), null, "decor_slab", BlockDecor.Variant.VALUES.length);
		register(new BlockGlowingDecorSlab(false).setHardness(2), null, "glowing_decor_slab", BlockGlowingDecor.Variant.VALUES.length);
		
		register(new BlockDecorSlab(true).setHardness(2), null, "decor_double_slab", BlockDecor.Variant.VALUES.length);
		register(new BlockGlowingDecorSlab(true).setHardness(2), null, "glowing_decor_double_slab", BlockGlowingDecor.Variant.VALUES.length);
		
		Item dungeonslabitem = new ItemSlab(decor_slab, decor_slab, decor_double_slab)
				.setHasSubtypes(true).setRegistryName(decor_slab.getRegistryName());
		GameRegistry.register(dungeonslabitem);
		proxy.registerItemModel(dungeonslabitem, BlockDecor.Variant.VALUES.length);
		
		Item lithographeneslabitem = new ItemSlab(glowing_decor_slab, glowing_decor_slab, glowing_decor_double_slab)
				.setHasSubtypes(true).setRegistryName(glowing_decor_slab.getRegistryName());
		GameRegistry.register(lithographeneslabitem);
		proxy.registerItemModel(lithographeneslabitem, BlockGlowingDecor.Variant.VALUES.length);

		register(new ItemMisc(), "misc", -2);
		register(new ItemDrive(), "drive", -1);
		register(new ItemMemory(), "memory", -1);
		register(new ItemModule(), "module", ItemModule.types.length);
		register(new ItemFloppy(), "floppy", -2);
		register(new ItemWirelessTerminal(), "wireless_terminal", 0);
		register(new ItemWeldthrower(), "weldthrower", 0);
		register(new ItemKeycard(), "keycard", -2);

		RecipeSorter.register("correlated:drive", DriveRecipe.class, Category.SHAPED, "after:minecraft:shaped");
		CRecipes.register();

		GameRegistry.registerTileEntity(TileEntityController.class, "correlated:controller_new");
		GameRegistry.registerTileEntity(TileEntityDriveBay.class, "correlated:drive_bay");
		GameRegistry.registerTileEntity(TileEntityMemoryBay.class, "correlated:memory_bay");
		GameRegistry.registerTileEntity(TileEntityTerminal.class, "correlated:terminal");
		GameRegistry.registerTileEntity(TileEntityInterface.class, "correlated:interface");
		GameRegistry.registerTileEntity(TileEntityImporterChest.class, "correlated:importer_chest");
		GameRegistry.registerTileEntity(TileEntityMicrowaveBeam.class, "correlated:microwave_beam");
		GameRegistry.registerTileEntity(TileEntityOpticalReceiver.class, "correlated:optical");
		GameRegistry.registerTileEntity(TileEntityBeaconLens.class, "correlated:beacon_lens");
		
		GameRegistry.registerTileEntity(TileEntityNetworkImporter.class, "correlatedpotentialistics:controller");
		GameRegistry.registerTileEntity(TileEntityVTImporter.class, "correlatedpotentialistics:vt");
		
		GameRegistry.registerTileEntity(TileEntityOldWirelessReceiver.class, "correlated:wireless_receiver");
		GameRegistry.registerTileEntity(TileEntityOldWirelessTransmitter.class, "correlated:wireless_transmitter");
		
		GameRegistry.registerTileEntity(TileEntityPotentialisticsImporter.A.class, "correlatedpotentialistics:controller_new");
		GameRegistry.registerTileEntity(TileEntityPotentialisticsImporter.B.class, "correlatedpotentialistics:drive_bay");
		GameRegistry.registerTileEntity(TileEntityPotentialisticsImporter.C.class, "correlatedpotentialistics:memory_bay");
		GameRegistry.registerTileEntity(TileEntityPotentialisticsImporter.D.class, "correlatedpotentialistics:terminal");
		GameRegistry.registerTileEntity(TileEntityPotentialisticsImporter.E.class, "correlatedpotentialistics:interface");
		GameRegistry.registerTileEntity(TileEntityPotentialisticsImporter.F.class, "correlatedpotentialistics:wireless_receiver");
		GameRegistry.registerTileEntity(TileEntityPotentialisticsImporter.G.class, "correlatedpotentialistics:wireless_transmitter");
		GameRegistry.registerTileEntity(TileEntityPotentialisticsImporter.H.class, "correlatedpotentialistics:importer_chest");
		
		Opcode.init();
		
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new CorrelatedGuiHandler());
		MinecraftForge.EVENT_BUS.register(this);
		proxy.preInit();
	}

	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.postInit();
	}
	
	@EventHandler
	public void onMissingMappings(FMLMissingMappingsEvent e) {
		for (MissingMapping mm : e.getAll()) {
			if (mm.resourceLocation.getResourceDomain().equals("correlatedpotentialistics")) {
				ResourceLocation newloc;
				if (mm.resourceLocation.getResourcePath().equals("vt")) {
					newloc = new ResourceLocation("correlated", "terminal");
				} else {
					newloc = new ResourceLocation("correlated", mm.resourceLocation.getResourcePath());
				}
				if (mm.type == Type.BLOCK) {
					mm.remap(Block.REGISTRY.getObject(newloc));
				} else if (mm.type == Type.ITEM) {
					mm.remap(Item.REGISTRY.getObject(newloc));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLootAdd(LootTableLoadEvent e) {
		if (e.getName().toString().startsWith("minecraft:chests/")) {
			e.getTable().getPool("main").addEntry(new LootEntryItem(misc, 45, 0, new LootFunction[0], new LootCondition[0], "correlated:processor"));
		}
	}
	
	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent e) {
		CorrelatedWorldData d = getDataFor(e.player.world);
		UUID id = e.player.getGameProfile().getId();
		if (d.getPlayerRespawnData().containsKey(id)) {
			e.player.readFromNBT(d.getPlayerRespawnData().remove(id));
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load e) {
		if (!e.getWorld().isRemote) {
			e.getWorld().addEventListener(new WorldEventListener());
		}
	}
	
	public static CorrelatedWorldData getDataFor(World w) {
		CorrelatedWorldData data = (CorrelatedWorldData)w.getPerWorldStorage().getOrLoadData(CorrelatedWorldData.class, "correlated");
		if (data == null) {
			data = new CorrelatedWorldData("correlated");
			w.getPerWorldStorage().setData("correlated", data);
		}
		data.setWorld(w);
		return data;
	}

	private void registerRecord(String str) {
		try {
			records.add(str);
			String basename = str.substring(0, str.indexOf('.'));
			ResourceLocation loc = new ResourceLocation("correlated", basename);
			SoundEvent snd = new SoundEvent(loc);
			GameRegistry.register(snd, loc);
			ItemCorrelatedRecord item = new ItemCorrelatedRecord(basename, snd);
			item.setRegistryName("record_"+basename);
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
		ResourceLocation loc = new ResourceLocation("correlated", str);
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
		block.setUnlocalizedName("correlated."+name);
		block.setCreativeTab(creativeTab);
		block.setRegistryName(name);
		GameRegistry.register(block);
		if (item != null) {
			try {
				ItemBlock ib = item.getConstructor(Block.class).newInstance(block);
				ib.setRegistryName(name);
				GameRegistry.register(ib);
			} catch (Exception e1) {
				Throwables.propagate(e1);
			}
			proxy.registerItemModel(Item.getItemFromBlock(block), itemVariants);
		}
		try {
			this.getClass().getField(name).set(this, block);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void register(Item item, String name, int variants) {
		item.setUnlocalizedName("correlated."+name);
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

	public static void sendUpdatePacket(TileEntity te) {
		sendUpdatePacket(te, te.getUpdateTag());
	}
	
	public static void sendUpdatePacket(TileEntity te, NBTTagCompound nbt) {
		WorldServer ws = (WorldServer)te.getWorld();
		Chunk c = te.getWorld().getChunkFromBlockCoords(te.getPos());
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(te.getPos(), te.getBlockMetadata(), nbt);
		for (EntityPlayerMP player : te.getWorld().getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue())) {
			if (ws.getPlayerChunkMap().isPlayerWatchingChunk(player, c.xPosition, c.zPosition)) {
				player.connection.sendPacket(packet);
			}
		}
	}

}


