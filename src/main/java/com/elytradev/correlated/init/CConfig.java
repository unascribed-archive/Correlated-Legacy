package com.elytradev.correlated.init;

import com.elytradev.correlated.EnergyUnit;
import com.elytradev.correlated.ImportMode;
import com.elytradev.correlated.init.config.ConfigKey;

import net.minecraftforge.common.config.Configuration;

public class CConfig {

	private static Configuration config;
	
	public static boolean easyProcessors;
	
	public static int controllerPUsage;
	public static int driveBayPUsage;
	public static int memoryBayPUsage;
	public static int terminalPUsage;
	public static int interfacePUsage;
	public static int beamPUsage;
	public static int opticalPUsage;
	
	public static int controllerCapacity;
	public static int controllerCap;
	public static int controllerErrorUsage_MultipleControllers;
	public static int controllerErrorUsage_NetworkTooBig;
	
	public static int drive1MiBPUsage;
	public static int drive4MiBPUsage;
	public static int drive16MiBPUsage;
	public static int drive64MiBPUsage;
	public static int drive128MiBPUsage;
	public static int voidDrivePUsage;
	
	public static double rfConversionRate;
	public static double fuConversionRate;
	public static double teslaConversionRate;
	public static double euConversionRate;
	public static double jConversionRate;
	public static double mjConversionRate;
	
	public static EnergyUnit preferredUnit;
	
	public static boolean weldthrowerHurts;
	public static boolean restrictCreativeDrives;
	
	public static ImportMode importMode;
	
	public static int limboDimId;
	
	
	
	
	private static final ConfigKey<Boolean> EASY_PROCESSORS = ConfigKey.create(
			"Crafting", "easyProcessors", false,
			
			"If true, processors can be crafted without finding them in vanilla",
			"dungeons. Will be removed when the limbo dungeon is added.");
	
	
	
	private static final ConfigKey<Boolean> WELDTHROWER_HURTS = ConfigKey.create(
			"Balance", "weldthrowerHurts", true,
			
			"If enabled, the Weldthrower will damage mobs and set them on fire.");
	
	private static final ConfigKey<Boolean> RESTRICT_CREATIVE_DRIVES = ConfigKey.create(
			"Balance", "restrictCreativeDrives", true,
			
			"If enabled, non-Creative players cannot put Vending or Creative",
			"drives into a Drive Bay, or remove them.");
	
	
	
	private static final ConfigKey<Double> RF_CONVERSION_RATE = ConfigKey.create(
			"PowerConversion", "rf", 1.0,
			
			"RF (Thermal Expansion) to Potential conversion rate. Can be",
			"fractional. Only used for input, energy cannot be taken out of a",
			"Correlated system.");
	private static final ConfigKey<Double> EU_CONVERSION_RATE = ConfigKey.create(
			"PowerConversion", "eu", 0.25,
			
			"EU (IndustrialCraft) to Potential conversion rate. Can be",
			"fractional. Only used for input, energy cannot be taken out of a",
			"Correlated system. Default is 1 EU = 4 P.");
	private static final ConfigKey<Double> TESLA_CONVERSION_RATE = ConfigKey.create(
			"PowerConversion", "tesla", 1.0,
			
			"Tesla to Potential conversion rate. Can be fractional. Should be",
			"the same as the RF rate, as recommended by the Tesla devs. Only",
			"used for input, energy cannot be taken out of a Correlated system.");
	private static final ConfigKey<Double> FORGE_CONVERSION_RATE = ConfigKey.create(
			"PowerConversion", "forge", 1.0,
			
			"Forge to Potential conversion rate. Can be fractional. Should be",
			"the same as the RF rate, as recommended by the Forge devs. Only",
			"used for input, energy cannot be taken out of a Correlated system.");
	private static final ConfigKey<Double> JOULE_CONVERSION_RATE = ConfigKey.create(
			"PowerConversion", "joule", 2.5,
			
			"Joule (Mekanism) to Potential conversion rate. Can be fractional.",
			"Only used for input, energy cannot be taken out of a Correlated",
			"system. Default is 5 J = 2 P.");
	private static final ConfigKey<Double> MJ_CONVERSION_RATE = ConfigKey.create(
			"PowerConversion", "mj", 0.1,
			
			"MJ (BuildCraft) to Potential conversion rate. Can be fractional.",
			"Only used for input, energy cannot be taken out of a Correlated",
			"system. Default is 1 MJ = 10 P.");
	
	
	
	private static final ConfigKey<EnergyUnit> PREFERRED_UNIT = ConfigKey.create(
			"Display", "preferredUnit", EnergyUnit.POTENTIAL,
			
			"The preferred energy unit to show in GUIs. Case insensitive.",
			"Possible values:",
			"  Potential / P: Correlated's native energy system [default]",
			"  Redstone Flux / RF: Thermal Expansion's energy system",
			"  Energy Units / EU: IndustrialCraft's energy system",
			"  Tesla / T: The Tesla energy system",
			"  Minecraft Joules / MJ: BuildCraft's energy system",
			"  Joules / J: Mekanism's energy system",
			"  Danks / Dk: Alternate name for Tesla",
			"  Forge Units / FU: The Forge energy system",
			"  Forge Energy / FE: Alternate name for Forge Units");
	
	
	
	private static final ConfigKey<Integer> CONTROLLER_P_USAGE = ConfigKey.create(
			"PowerUsage", "controller", 32,
			
			"The P/t used by the Controller.");
	private static final ConfigKey<Integer> DRIVE_BAY_P_USAGE = ConfigKey.create(
			"PowerUsage", "driveBay", 8,
			
			"The P/t used by the Drive Bay.");
	private static final ConfigKey<Integer> MEMORY_BAY_P_USAGE = ConfigKey.create(
			"PowerUsage", "memoryBay", 4,
			
			"The P/t used by the Memory Bay.");
	private static final ConfigKey<Integer> TERMINAL_P_USAGE = ConfigKey.create(
			"PowerUsage", "terminal", 4,
			
			"The P/t used by the Terminal.");
	private static final ConfigKey<Integer> INTERFACE_P_USAGE = ConfigKey.create(
			"PowerUsage", "interface", 8,
			
			"The P/t used by the Interface.");
	private static final ConfigKey<Integer> BEAM_P_USAGE = ConfigKey.create(
			"PowerUsage", "beam", 24,
			
			"The P/t used by the Microwave Beam.");
	private static final ConfigKey<Integer> OPTICAL_P_USAGE = ConfigKey.create(
			"PowerUsage", "optical", 8,
			
			"The P/t used by the Optical Transceiver.");
	
	private static final ConfigKey<Integer> DRIVE_1MIB_P_USAGE = ConfigKey.create(
			"PowerUsage", "1mDrive", 1,
			
			"The P/t used by the 1MiB Drive.");
	private static final ConfigKey<Integer> DRIVE_4MIB_P_USAGE = ConfigKey.create(
			"PowerUsage", "4mDrive", 2,
			
			"The P/t used by the 4MiB Drive.");
	private static final ConfigKey<Integer> DRIVE_16MIB_P_USAGE = ConfigKey.create(
			"PowerUsage", "16mDrive", 4,
			
			"The P/t used by the 16MiB Drive.");
	private static final ConfigKey<Integer> DRIVE_64MIB_P_USAGE = ConfigKey.create(
			"PowerUsage", "64mDrive", 8,
			
			"The P/t used by the 64MiB Drive.");
	private static final ConfigKey<Integer> DRIVE_128MIB_P_USAGE = ConfigKey.create(
			"PowerUsage", "128mDrive", 16,
			
			"The P/t used by the 128MiB Drive.");
	
	private static final ConfigKey<Integer> DRIVE_VOID_P_USAGE = ConfigKey.create(
			"PowerUsage", "voidDrive", 4,
			
			"The P/t used by the Void Drive.");
	
	
	
	private static final ConfigKey<Integer> CONTROLLER_CAPACITY = ConfigKey.create(
			"PowerFineTuning", "controllerCapacity", 64000,
			
			"The Potential stored by the controller.");
	private static final ConfigKey<Integer> CONTROLLER_CAP = ConfigKey.create(
			"PowerFineTuning", "controllerCap", 640,
			
			"The maximum P/t the controller can use, and therefore a network.");
	
	private static final ConfigKey<Integer> CONTROLLER_ERROR_USAGE_MULTIPLE_CONTROLLERS = ConfigKey.create(
			"PowerFineTuning", "controllerErrorUsage_MultipleControllers", 4,
			
			"The P/t used by the controller when it detects another controller",
			"in its network and is erroring.");
	private static final ConfigKey<Integer> CONTROLLER_ERROR_USAGE_NETWORK_TOO_BIG = ConfigKey.create(
			"PowerFineTuning", "controllerErrorUsage_NetworkTooBig", 640,
			
			"The P/t used by the controller when it reaches the network scan",
			"limit.");
	
	
	
	private static final ConfigKey<Integer> LIMBO_DIM_ID = ConfigKey.create(
			"IDs", "limboDimId", -31,
			
			"The dimension ID for the glitch dungeon.");
	
	
	
	private static final ConfigKey<ImportMode> IMPORT_MODE = ConfigKey.create(
			"Import", "mode", ImportMode.REFUND_ALL,
			
			"The mode for the old network importer, which will run on any 1.x",
			"networks loaded with Correlated 2.x.",
			"Possible values are:",
			"  refund_all: Refund components, convert drives into Data Cores,",
			"              and refund Interface contents. [default]",
			"",
			"  refund_some: Convert drives into Data Cores and refund Interface",
			"               contents, but do not refund crafting ingredients.",
			"               Useful if you used MineTweaker to change the",
			"               recipes. Blocks will still be refunded.",
			"",
			"  refund_content: Convert drives into Data Cores and refund",
			"                  Interface contents, but do not refund anything",
			"                  else.",
			"",
			"  destroy: Outright delete the network, and all items that were",
			"           contained in it. If you use this option, PLEASE state it",
			"           prominently on your modpack page, and warn people.",
			"",
			"  leave: Leave the network alone. May result in glitchy drives",
			"         holding more data than they should be able to, crashes,",
			"         and general strangeness. Not recommended."
			);
	
	public static void setConfig(Configuration config) {
		CConfig.config = config;
	}
	
	public static void load() {
		config.load();
		
		easyProcessors = EASY_PROCESSORS.get(config);
		
		weldthrowerHurts = WELDTHROWER_HURTS.get(config);
		restrictCreativeDrives = RESTRICT_CREATIVE_DRIVES.get(config);
		
		rfConversionRate = RF_CONVERSION_RATE.get(config);
		euConversionRate = EU_CONVERSION_RATE.get(config);
		teslaConversionRate = TESLA_CONVERSION_RATE.get(config);
		fuConversionRate = FORGE_CONVERSION_RATE.get(config);
		jConversionRate = JOULE_CONVERSION_RATE.get(config);
		mjConversionRate = MJ_CONVERSION_RATE.get(config);
		
		preferredUnit = PREFERRED_UNIT.get(config);
		
		controllerPUsage = CONTROLLER_P_USAGE.get(config);
		driveBayPUsage = DRIVE_BAY_P_USAGE.get(config);
		memoryBayPUsage = MEMORY_BAY_P_USAGE.get(config);
		terminalPUsage = TERMINAL_P_USAGE.get(config);
		interfacePUsage = INTERFACE_P_USAGE.get(config);
		beamPUsage = BEAM_P_USAGE.get(config);
		opticalPUsage = OPTICAL_P_USAGE.get(config);
		
		controllerCapacity = CONTROLLER_CAPACITY.get(config);
		controllerCap = CONTROLLER_CAP.get(config);
		controllerErrorUsage_MultipleControllers = CONTROLLER_ERROR_USAGE_MULTIPLE_CONTROLLERS.get(config);
		controllerErrorUsage_NetworkTooBig = CONTROLLER_ERROR_USAGE_NETWORK_TOO_BIG.get(config);
		
		drive1MiBPUsage = DRIVE_1MIB_P_USAGE.get(config);
		drive4MiBPUsage = DRIVE_4MIB_P_USAGE.get(config);
		drive16MiBPUsage = DRIVE_16MIB_P_USAGE.get(config);
		drive64MiBPUsage = DRIVE_64MIB_P_USAGE.get(config);
		drive128MiBPUsage = DRIVE_128MIB_P_USAGE.get(config);
		voidDrivePUsage = DRIVE_VOID_P_USAGE.get(config);
		
		limboDimId = LIMBO_DIM_ID.get(config);
		
		importMode = IMPORT_MODE.get(config);
		
		config.getCategory("PowerUsage").remove("drivePow");
		config.getCategory("PowerUsage").remove("driveDiv");
		
		config.getCategory("PowerConversion").remove("t");
		config.getCategory("PowerConversion").remove("fu");
		config.getCategory("PowerConversion").remove("j");
	}

	public static void save() {
		EASY_PROCESSORS.set(config, easyProcessors);
		
		WELDTHROWER_HURTS.set(config, weldthrowerHurts);
		RESTRICT_CREATIVE_DRIVES.set(config, restrictCreativeDrives);
		
		RF_CONVERSION_RATE.set(config, rfConversionRate);
		EU_CONVERSION_RATE.set(config, euConversionRate);
		TESLA_CONVERSION_RATE.set(config, teslaConversionRate);
		FORGE_CONVERSION_RATE.set(config, fuConversionRate);
		JOULE_CONVERSION_RATE.set(config, jConversionRate);
		MJ_CONVERSION_RATE.set(config, mjConversionRate);
		
		PREFERRED_UNIT.set(config, preferredUnit);
		
		CONTROLLER_P_USAGE.set(config, controllerPUsage);
		DRIVE_BAY_P_USAGE.set(config, driveBayPUsage);
		MEMORY_BAY_P_USAGE.set(config, memoryBayPUsage);
		TERMINAL_P_USAGE.set(config, terminalPUsage);
		INTERFACE_P_USAGE.set(config, interfacePUsage);
		BEAM_P_USAGE.set(config, beamPUsage);
		OPTICAL_P_USAGE.set(config, opticalPUsage);
		
		CONTROLLER_CAPACITY.set(config, controllerCapacity);
		CONTROLLER_CAP.set(config, controllerCap);
		CONTROLLER_ERROR_USAGE_MULTIPLE_CONTROLLERS.set(config, controllerErrorUsage_MultipleControllers);
		CONTROLLER_ERROR_USAGE_NETWORK_TOO_BIG.set(config, controllerErrorUsage_NetworkTooBig);
		
		DRIVE_1MIB_P_USAGE.set(config, drive1MiBPUsage);
		DRIVE_4MIB_P_USAGE.set(config, drive4MiBPUsage);
		DRIVE_16MIB_P_USAGE.set(config, drive16MiBPUsage);
		DRIVE_64MIB_P_USAGE.set(config, drive64MiBPUsage);
		DRIVE_128MIB_P_USAGE.set(config, drive128MiBPUsage);
		DRIVE_VOID_P_USAGE.set(config, voidDrivePUsage);
		
		LIMBO_DIM_ID.set(config, limboDimId);
		
		IMPORT_MODE.set(config, importMode);
		
		config.save();
	}
	
}
