package com.elytradev.correlated.proxy;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.opengl.KHRDebugCallback;
import org.lwjgl.opengl.OpenGLException;

import com.elytradev.correlated.CLog;
import com.elytradev.correlated.CSSColors;
import com.elytradev.correlated.ColorType;
import com.elytradev.correlated.ColorValues;
import com.elytradev.correlated.CorrelatedPluralRulesLoader;
import com.elytradev.correlated.block.BlockDecor;
import com.elytradev.correlated.block.BlockGlowingDecor;
import com.elytradev.correlated.block.BlockWireless;
import com.elytradev.correlated.client.CorrelatedMusicTicker;
import com.elytradev.correlated.client.DocumentationManager;
import com.elytradev.correlated.client.IBMFontRenderer;
import com.elytradev.correlated.client.ParticleWeldthrower;
import com.elytradev.correlated.client.anim.AnimatedBlockTexture;
import com.elytradev.correlated.client.gui.GuiAbortRetryFail;
import com.elytradev.correlated.client.gui.GuiFakeReboot;
import com.elytradev.correlated.client.gui.GuiGlitchedMainMenu;
import com.elytradev.correlated.client.gui.GuiSelectAPN;
import com.elytradev.correlated.client.render.entity.RenderAutomaton;
import com.elytradev.correlated.client.render.entity.RenderThrownItem;
import com.elytradev.correlated.client.render.tile.RenderBeaconLens;
import com.elytradev.correlated.client.render.tile.RenderController;
import com.elytradev.correlated.client.render.tile.RenderControllerItem;
import com.elytradev.correlated.client.render.tile.RenderControllerItemCheaty;
import com.elytradev.correlated.client.render.tile.RenderDriveBay;
import com.elytradev.correlated.client.render.tile.RenderImporterChest;
import com.elytradev.correlated.client.render.tile.RenderMemoryBay;
import com.elytradev.correlated.client.render.tile.RenderMicrowaveBeam;
import com.elytradev.correlated.client.render.tile.RenderOpticalTransceiver;
import com.elytradev.correlated.client.render.tile.RenderTerminal;
import com.elytradev.correlated.entity.EntityAutomaton;
import com.elytradev.correlated.entity.EntityThrownItem;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CItems;
import com.elytradev.correlated.init.CRecords;
import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.item.ItemKeycard;
import com.elytradev.correlated.item.ItemMemory;
import com.elytradev.correlated.item.ItemMisc;
import com.elytradev.correlated.item.ItemModule;
import com.elytradev.correlated.network.wireless.ChangeAPNMessage;
import com.elytradev.correlated.tile.TileEntityBeaconLens;
import com.elytradev.correlated.tile.TileEntityController;
import com.elytradev.correlated.tile.TileEntityDriveBay;
import com.elytradev.correlated.tile.TileEntityDummy;
import com.elytradev.correlated.tile.TileEntityMemoryBay;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;
import com.elytradev.correlated.tile.TileEntityOpticalTransceiver;
import com.elytradev.correlated.tile.TileEntityTerminal;
import com.elytradev.correlated.tile.importer.TileEntityImporterChest;
import com.elytradev.correlated.wifi.IWirelessClient;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.text.PluralRules.PluralType;
import com.ibm.icu.util.ULocale;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.client.audio.Sound.Type;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecIBXM;

// TODO this class is a mess
public class ClientProxy extends Proxy {
	private class IndexedColorValues implements ColorValues {
		private final int[] values;

		public IndexedColorValues(int[] values) {
			this.values = values;
		}

		@Override
		public int getColor(String name) {
			try {
				return values[Integer.parseInt(name)];
			} catch (NumberFormatException e) {
				return rand.nextInt();
			}
		}

		@Override
		public int getColor(int index) {
			return values[index];
		}
	}
	private class NamedColorValues implements ColorValues {
		private final TObjectIntMap<String> values;

		public NamedColorValues(TObjectIntMap<String> values) {
			this.values = values;
		}
		
		@Override
		public int getColor(int index) {
			return getColor(Integer.toString(index));
		}
		@Override
		public int getColor(String name) {
			return values.containsKey(name) ? values.get(name) : rand.nextInt();
		}
	}
	private class BrokenColorValues implements ColorValues {
		@Override
		public int getColor(int index) {
			return rand.nextInt();
		}
		@Override
		public int getColor(String name) {
			return rand.nextInt();
		}
	}
	public static final List<IRenderHandler> shapes = Lists.newArrayList();

	public static float ticks = 0;
	
	public static int glitchTicks = -1;
	public static boolean textGlitch = false;
	public static String seed;
	private BitSet glitchJpeg;
	private int jpegTexture = -1;
	private Random rand = new Random();
	
	private Map<ColorType, ColorValues> colors = Maps.newEnumMap(ColorType.class);
	
	private Future<BufferedImage> corruptionFuture;
	private ExecutorService jpegCorruptor = Executors.newSingleThreadExecutor((r) -> new Thread(r, "JPEG Corruption Thread"));
	private Callable<BufferedImage> jpegCorruptionTask = () -> {
		int tries = 0;
		while (true) {
			tries++;
			if (tries > 20) {
				return null;
			}
			int idx = rand.nextInt(glitchJpeg.size());
			glitchJpeg.flip(idx);
			try {
				BufferedImage jpeg = ImageIO.read(new ByteArrayInputStream(glitchJpeg.toByteArray()));
				return jpeg;
			} catch (IOException e1) {
				glitchJpeg.flip(idx);
				continue;
			}
		}
	};
	
	public static DocumentationManager documentationManager;
	
	public static MusicType enceladusType;

	private boolean checkedDebugSupport = false;
	
	public AnimatedBlockTexture controllerAbt;
	
	@SuppressWarnings("deprecation")
	@Override
	public void preInit() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityController.class, new RenderController());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDriveBay.class, new RenderDriveBay());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMemoryBay.class, new RenderMemoryBay());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTerminal.class, new RenderTerminal());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityImporterChest.class, new RenderImporterChest());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMicrowaveBeam.class, new RenderMicrowaveBeam());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOpticalTransceiver.class, new RenderOpticalTransceiver());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBeaconLens.class, new RenderBeaconLens());
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDummy.A.class, new RenderControllerItem());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDummy.B.class, new RenderControllerItemCheaty());
		
		RenderingRegistry.registerEntityRenderingHandler(EntityThrownItem.class, (rm) -> new RenderThrownItem(rm, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityAutomaton.class, RenderAutomaton::new);
		
		MinecraftForge.EVENT_BUS.register(this);

		((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener((rm) -> {
			colors.clear();
			for (ColorType type : ColorType.values()) {
				String lowerName = type.name().toLowerCase(Locale.ROOT);
				try {
					IResource res = rm.getResource(new ResourceLocation("correlated", "colors/"+lowerName+".png"));
					InputStream in = res.getInputStream();
					BufferedImage img = ImageIO.read(in);
					in.close();
					int[] rgb = new int[img.getWidth()*img.getHeight()];
					img.getRGB(0, 0, img.getWidth(), img.getHeight(), rgb, 0, img.getWidth());
					colors.put(type, new IndexedColorValues(rgb));
					CLog.info("Successfully loaded {} colors from PNG", lowerName);
				} catch (FileNotFoundException e) {
					try {
						IResource res = rm.getResource(new ResourceLocation("correlated", "colors/"+lowerName+".json"));
						InputStreamReader isr = new InputStreamReader(res.getInputStream(), Charsets.UTF_8);
						Gson gson = new Gson();
						JsonObject obj = gson.fromJson(isr, JsonObject.class);
						isr.close();
						TObjectIntMap<String> map = new TObjectIntHashMap<>(obj.size());
						for (Map.Entry<String, JsonElement> en : obj.entrySet()) {
							JsonElement ele = en.getValue();
							int color;
							if (ele.isJsonPrimitive()) {
								JsonPrimitive prim = ele.getAsJsonPrimitive();
								if (prim.isString()) {
									String s = prim.getAsString();
									color = CSSColors.parse(s);
								} else if (prim.isNumber()) {
									color = prim.getAsInt();
								} else {
									throw new IllegalArgumentException("invalid JSON object type at key "+en.getKey()+", expected string or number");
								}
							} else {
								throw new IllegalArgumentException("invalid JSON object type at key "+en.getKey()+", expected string or number");
							}
							map.put(en.getKey(), color);
						}
						colors.put(type, new NamedColorValues(map));
						CLog.info("Successfully loaded {} colors from JSON", lowerName);
					} catch (FileNotFoundException e1) {
						CLog.warn("Can't find {} colors in PNG or JSON format", lowerName, e1);
					} catch (Exception e1) {
						CLog.warn("Error while loading {} colors from JSON", lowerName, e1);
					}
				} catch (Exception e) {
					CLog.warn("Error while loading {} colors from PNG", lowerName, e);
				}
			}
			try {
				controllerAbt = AnimatedBlockTexture.read(rm, new ResourceLocation("correlated", "animations/controller_anim.json"));
			} catch (IOException e) {
				CLog.warn("Failed to load controller animations", e);
			}
		});
		
		enceladusType = EnumHelper.addEnum(MusicType.class, "CORRELATED_ENCELADUS", new Class<?>[] {SoundEvent.class, int.class, int.class}, CSoundEvents.ENCELADUS, 24000, 48000);
		
		List<String> pages = Lists.newArrayList();
		
		URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
		String src = url.toString();
		
		if (src.startsWith("jar:file:")) {
			try {
				String sub = src.substring(9);
				if (sub.contains("!")) {
					sub = sub.substring(0, sub.lastIndexOf('!'));
				}
				File f = new File(URLDecoder.decode(sub, "UTF-8"));
				JarFile jf = new JarFile(f);
				for (JarEntry en : Collections.list(jf.entries())) {
					String path = en.getName();
					if (path.startsWith("documentation/en_US/") && path.endsWith(".md")) {
						String str = path.substring(20).replace('/', '.');
						str = str.substring(0, str.length()-3);
						pages.add(str);
					}
				}
				jf.close();
			} catch (UnsupportedEncodingException e) {
				throw new AssertionError(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if (src.startsWith("file:")) {
			try {
				String path = URLDecoder.decode(src.substring(5), "UTF-8");
				String us = getClass().getName().replace('.', '/')+".class";
				if (!path.endsWith(us)) throw new RuntimeException("Loaded from a directory, but we can't find ourselves! "+src);
				File root = new File(path.substring(0, path.length()-us.length()));
				File enUS = new File(root, "documentation/en_US");
				search(enUS, enUS, pages);
			} catch (UnsupportedEncodingException e) {
				throw new AssertionError(e);
			}
		} else {
			throw new RuntimeException("No idea what we've been loaded from! "+src);
		}
		
		documentationManager = new DocumentationManager(pages);
	}
	
	@SubscribeEvent
	public void onModelRegister(ModelRegistryEvent e) {
		ModelLoader.setCustomModelResourceLocation(CItems.WELDTHROWER, 0, new ModelResourceLocation(new ResourceLocation("correlated", "weldthrower"), "inventory"));
		ModelLoader.setCustomModelResourceLocation(CItems.DOC_TABLET, 0, new ModelResourceLocation(new ResourceLocation("correlated", "doc_tablet"), "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(CItems.FLOPPY, 0, new ModelResourceLocation(new ResourceLocation("correlated", "floppy_write_enabled"), "inventory"));
		ModelLoader.setCustomModelResourceLocation(CItems.FLOPPY, 1, new ModelResourceLocation(new ResourceLocation("correlated", "floppy_write_disabled"), "inventory"));
		
		for (int i = 0; i < 4; i++) {
			ModelLoader.setCustomModelResourceLocation(CItems.HANDHELD_TERMINAL, 0, new ModelResourceLocation(new ResourceLocation("correlated", "handheld_terminal"), "inventory"));
		}
		ModelLoader.setCustomModelResourceLocation(CItems.HANDHELD_TERMINAL, 4, new ModelResourceLocation(new ResourceLocation("correlated", "handheld_terminal_hand"), "inventory"));
		
		int idx = 0;
		for (String s : ItemMisc.items) {
			ModelLoader.setCustomModelResourceLocation(CItems.MISC, idx++, new ModelResourceLocation(new ResourceLocation("correlated", s), "inventory"));
		}
		idx = 0;
		for (String s : ItemKeycard.colors) {
			ModelLoader.setCustomModelResourceLocation(CItems.KEYCARD, idx++, new ModelResourceLocation(new ResourceLocation("correlated", "keycard_"+s), "inventory"));
		}
		
		for (int i = 0; i < ItemDrive.tierSizes.length; i++) {
			ModelLoader.setCustomModelResourceLocation(CItems.DRIVE, i, new ModelResourceLocation("correlated:drive", "inventory"));
		}
		
		for (int i = 0; i < ItemMemory.tierSizes.length; i++) {
			ModelLoader.setCustomModelResourceLocation(CItems.MEMORY, i, new ModelResourceLocation("correlated:memory", "inventory"));
		}
		for (int i = 0; i < ItemModule.types.length; i++) {
			ModelLoader.setCustomModelResourceLocation(CItems.MODULE, i, new ModelResourceLocation("correlated:module", "inventory"));
		}

		ModelLoader.setCustomModelResourceLocation(CItems.DEBUGGINATOR, 0, new ModelResourceLocation(new ResourceLocation("correlated", "debugginator"), "inventory"));
		ModelLoader.setCustomModelResourceLocation(CItems.DEBUGGINATOR, 1, new ModelResourceLocation(new ResourceLocation("correlated", "debugginator_closed"), "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.CONTROLLER), 0, new ModelResourceLocation("correlated:controller_item", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.CONTROLLER), 8, new ModelResourceLocation("correlated:controller_item_cheaty", "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.DRIVE_BAY), 0, new ModelResourceLocation("correlated:drive_bay", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.MEMORY_BAY), 0, new ModelResourceLocation("correlated:memory_bay", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.TERMINAL), 0, new ModelResourceLocation("correlated:terminal", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.INTERFACE), 0, new ModelResourceLocation("correlated:interface", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.WIRELESS), 0, new ModelResourceLocation("correlated:microwave_beam_item", "inventory"));
		for (int i = 1; i < BlockWireless.Variant.VALUES.length; i++) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.WIRELESS), i, new ModelResourceLocation("correlated:wireless", "inventory"+i));
		}
		for (int i = 0; i < BlockDecor.Variant.VALUES.length; i++) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.DECOR_BLOCK), i, new ModelResourceLocation("correlated:decor_block", "inventory"+i));
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.DECOR_SLAB), i, new ModelResourceLocation("correlated:decor_slab", "inventory"+i));
		}
		for (int i = 0; i < BlockGlowingDecor.Variant.VALUES.length; i++) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.GLOWING_DECOR_BLOCK), i, new ModelResourceLocation("correlated:glowing_decor_block", "inventory"+i));
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.GLOWING_DECOR_SLAB), i, new ModelResourceLocation("correlated:glowing_decor_slab", "inventory"+i));
		}
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.DUNGEONCRETE_GRATE_STAIRS), 0, new ModelResourceLocation("correlated:dungeoncrete_grate_stairs", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.DUNGEONCRETE_LARGETILE_STAIRS), 0, new ModelResourceLocation("correlated:dungeoncrete_largetile_stairs", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.DUNGEONCRETE_STAIRS), 0, new ModelResourceLocation("correlated:dungeoncrete_stairs", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.DUNGEONCRETE_VERTICAL_STAIRS), 0, new ModelResourceLocation("correlated:dungeoncrete_vertical_stairs", "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.ELUCID_BRICK_STAIRS), 0, new ModelResourceLocation("correlated:elucid_brick_stairs", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.ELUCID_GRIT_STAIRS), 0, new ModelResourceLocation("correlated:elucid_grit_stairs", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.ELUCID_SCALE_STAIRS), 0, new ModelResourceLocation("correlated:elucid_scale_stairs", "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.LITHOGRAPHENE_OFF_STAIRS), 0, new ModelResourceLocation("correlated:lithographene_off_stairs", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.LITHOGRAPHENE_OFF_VARIANT_STAIRS), 0, new ModelResourceLocation("correlated:lithographene_off_variant_stairs", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.LITHOGRAPHENE_ON_STAIRS), 0, new ModelResourceLocation("correlated:lithographene_on_stairs", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.LITHOGRAPHENE_ON_VARIANT_STAIRS), 0, new ModelResourceLocation("correlated:lithographene_on_variant_stairs", "inventory"));
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CBlocks.PLATING_STAIRS), 0, new ModelResourceLocation("correlated:plating_stairs", "inventory"));
		
		for (Item item : CRecords.RECORD_ITEMS) {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}
	@Override
	public void clearShapes() {
		shapes.clear();
	}

	private void search(File root, File f, List<String> pages) {
		String prefix = root.getAbsolutePath()+"/";
		for (File child : f.listFiles()) {
			String abs = child.getAbsolutePath();
			if (!abs.startsWith(prefix)) {
				CLog.warn("Walked outside of documentation root - {}", child.getAbsolutePath());
				continue;
			}
			if (child.isDirectory()) {
				search(root, child, pages);
			} else if (child.getName().endsWith(".md")) {
				String trunc = abs.substring(prefix.length());
				pages.add(trunc.replace('/', '.').substring(0, trunc.length()-3));
			}
		}
	}
	@Override
	public ColorValues getColorValues(ColorType type) {
		if (!colors.containsKey(type)) return new BrokenColorValues();
		return colors.get(type);
	}
	@Override
	public void showAPNChangeMenu(BlockPos pos, boolean multiple, boolean client) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiSelectAPN(null, client, multiple, new IWirelessClient() {
			
			@Override
			public void setAPNs(Set<String> apn) {
				new ChangeAPNMessage(pos, apn).sendToServer();
			}
			
			@Override
			public Set<String> getAPNs() {
				return Collections.emptySet();
			}
			
			@Override
			public BlockPos getPosition() {
				return pos;
			}
			
			@Override
			public double getX() {
				return pos.getX()+0.5;
			}
			
			@Override
			public double getY() {
				return pos.getY()+0.5;
			}
			
			@Override
			public double getZ() {
				return pos.getZ()+0.5;
			}
			
		}));
	}
	@Override
	public void postInit() {
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CBlocks.WIRELESS), 0, TileEntityMicrowaveBeam.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CBlocks.CONTROLLER), 0, TileEntityDummy.A.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CBlocks.CONTROLLER), 8, TileEntityDummy.B.class);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if (!(stack.getItem() instanceof ItemModule)) return -1;
				ItemModule id = (ItemModule)stack.getItem();
				if (tintIndex == 1) {
					return id.getTypeColor(stack);
				}
				return -1;
			}
			
		}, CItems.MODULE);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if (!(stack.getItem() instanceof ItemMemory)) return -1;
				ItemMemory id = (ItemMemory)stack.getItem();
				if (tintIndex == 1) {
					return id.getTierColor(stack);
				}
				return -1;
			}
			
		}, CItems.MEMORY);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			@SuppressWarnings("fallthrough")
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if (!(stack.getItem() instanceof ItemDrive)) return -1;
				ItemDrive id = (ItemDrive)stack.getItem();
				if (tintIndex == 1) {
					return id.getFullnessColor(stack);
				} else if (tintIndex == 2) {
					return id.getTierColor(stack);
				} else if (tintIndex == 3) {
					switch (id.getPartitioningMode(stack)) {
						case NONE:
							return getColorValues(ColorType.OTHER).getColor("drive_mode_none");
						case WHITELIST:
							return getColorValues(ColorType.OTHER).getColor("drive_mode_whitelist");
						case BLACKLIST:
							return getColorValues(ColorType.OTHER).getColor("drive_mode_blacklist");
					}
				} else if (tintIndex >= 4 && tintIndex <= 6) {
					int uncolored;
					if (stack.getItemDamage() == 6 || stack.getItemDamage() == 7) {
						uncolored = getColorValues(ColorType.OTHER).getColor("creativedrive_unlit_light");
					} else if (stack.getItemDamage() == 4) {
						uncolored = getColorValues(ColorType.OTHER).getColor("voiddrive_unlit_light");
					} else {
						uncolored = getColorValues(ColorType.OTHER).getColor("drive_unlit_light");
					}

					int red = getColorValues(ColorType.OTHER).getColor("drive_prioritylight_high");
					int green = getColorValues(ColorType.OTHER).getColor("drive_prioritylight_low");
					
					int left = uncolored;
					int middle = uncolored;
					int right = uncolored;
					switch (id.getPriority(stack)) {
						case HIGHEST:
							right = red;
						case HIGHER:
							middle = red;
						case HIGH:
							left = red;
							break;
						case LOWEST:
							left = green;
						case LOWER:
							middle = green;
						case LOW:
							right = green;
							break;
						default:
							break;
					}
					if (tintIndex == 4) {
						return left;
					} else if (tintIndex == 5) {
						return middle;
					} else if (tintIndex == 6) {
						return right;
					}
				}
				return id.getBaseColor(stack);
			}
			
		}, CItems.DRIVE);
		
		if (enceladusType.getMusicLocation() != null) {
			CorrelatedMusicTicker cmt = new CorrelatedMusicTicker(Minecraft.getMinecraft(), Minecraft.getMinecraft().getMusicTicker());
			ReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getMinecraft(), cmt, "field_147126_aw", "mcMusicTicker", "aK");
		}
	}
	
	@Override
	public String i18nFormat(String key, Object[] format) {
		return I18n.format(key, format);
	}
	
	@Override
	public boolean i18nContains(String key) {
		return I18n.hasKey(key);
	}
	
	@Override
	public PluralRules getPluralRules() {
		LanguageManager lm = Minecraft.getMinecraft().getLanguageManager();
		Locale jl = lm.getCurrentLanguage().getJavaLocale();
		ULocale ul = ULocale.forLocale(jl);
		return CorrelatedPluralRulesLoader.loader.forLocale(ul, PluralType.CARDINAL);
	}
	
	@Override
	public void weldthrowerTick(EntityPlayer player) {
		Vec3d look = player.getLookVec();
		Vec3d right = look.rotateYaw(-90);
		double dist = 0.5;
		double gap = 1;
		double fuzz = 0.05;
		look.rotateYaw(20);
		for (int i = 0; i < 5; i++) {
			Random rand = player.world.rand;
			ParticleWeldthrower dust = new ParticleWeldthrower(player.world,
					player.posX+(right.x*dist)+(look.x*gap),
					player.posY+(player.getEyeHeight()-0.1)+(right.y*dist)+(look.y*gap),
					player.posZ+(right.z*dist)+(look.z*gap), 1);
			dust.setRBGColorF(0, 0.9725490196078431f-(rand.nextFloat()/5), 0.8235294117647059f-(rand.nextFloat()/5));
			dust.setMotion(look.x+(rand.nextGaussian()*fuzz), look.y+(rand.nextGaussian()*fuzz), look.z+(rand.nextGaussian()*fuzz));
			Minecraft.getMinecraft().effectRenderer.addEffect(dust);
		}
	}
	@Override
	public void weldthrowerHeal(EntityAutomaton ent) {
		for (int i = 0; i < 5; i++) {
			Random rand = ent.world.rand;
			ParticleWeldthrower dust = new ParticleWeldthrower(ent.world,
					ent.posX+(rand.nextGaussian()*0.2),
					ent.posY+(rand.nextGaussian()*0.2),
					ent.posZ+(rand.nextGaussian()*0.2), 1);
			dust.setRBGColorF(0, 0.9725490196078431f-(rand.nextFloat()/5), 0.8235294117647059f-(rand.nextFloat()/5));
			Minecraft.getMinecraft().effectRenderer.addEffect(dust);
		}
	}
	@Override
	public void smokeTick(EntityAutomaton ent) {
		if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().world.getTotalWorldTime() < 10) return;
		Random rand = ent.world.rand;
		for (int i = 0; i < ent.getMaxHealth()-ent.getHealth(); i++) {
			if (rand.nextInt(5) == 0) {
				Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(EnumParticleTypes.SMOKE_NORMAL.getParticleID(),
						ent.posX+(rand.nextGaussian()*0.2),
						ent.posY+(rand.nextGaussian()*0.2),
						ent.posZ+(rand.nextGaussian()*0.2),
						0, 0, 0);
			}
		}
		if (ent.getHealth() < ent.getMaxHealth()/2 && rand.nextInt(10) == 0) {
			Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(EnumParticleTypes.LAVA.getParticleID(),
					ent.posX,
					ent.posY,
					ent.posZ,
					0, 0, 0);
		}
	}
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (e.phase == Phase.START) {
			ticks++;
			if (glitchTicks > -1 && Minecraft.getMinecraft().world == null) {
				glitchTicks = -1;
			}
			if (glitchTicks > -1 && jpegTexture != -1 && !Minecraft.getMinecraft().isGamePaused()) {
				glitchTicks++;
				if (glitchTicks >= 240) {
					glitchTicks = -1;
					Minecraft.getMinecraft().getSoundHandler().stopSounds();
					if (Minecraft.getMinecraft().player != null && !Minecraft.getMinecraft().player.isDead) {
						Minecraft.getMinecraft().displayGuiScreen(new GuiFakeReboot());
					}
				}
				if (glitchJpeg != null && corruptionFuture == null) {
					corruptionFuture = jpegCorruptor.submit(jpegCorruptionTask);
				}
			}
			
			if (Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().world.provider.getDimension() == CConfig.limboDimId) {
				
			}
		}
	}
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent e) {
		GlStateManager.pushMatrix();
		EntityPlayer p = Minecraft.getMinecraft().player;
		double interpX = p.lastTickPosX + ((p.posX - p.lastTickPosX) * e.getPartialTicks());
		double interpY = p.lastTickPosY + ((p.posY - p.lastTickPosY) * e.getPartialTicks());
		double interpZ = p.lastTickPosZ + ((p.posZ - p.lastTickPosZ) * e.getPartialTicks());
		GlStateManager.translate(-interpX, -interpY, -interpZ);
		for (IRenderHandler irh : shapes) {
			irh.render(e.getPartialTicks(), Minecraft.getMinecraft().world, Minecraft.getMinecraft());
		}
		GlStateManager.popMatrix();
	}
	@SubscribeEvent
	public void onRenderLiving(RenderLivingEvent.Pre<EntityLivingBase> e) {
		if (TextFormatting.getTextWithoutFormattingCodes(e.getEntity().getName()).equals("unascribed")) {
			if (e.getEntity() instanceof EntityPlayer) {
				EntityPlayer ep = (EntityPlayer)e.getEntity();
				if (!ep.isWearing(EnumPlayerModelParts.CAPE)) return;
			}
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		}
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onRenderTick(RenderTickEvent e) {
		if (e.phase == Phase.START) {
			ticks = ((int)ticks)+e.renderTickTime;
		} else if (e.phase == Phase.END) {
			if (Minecraft.getMinecraft().currentScreen == null || Minecraft.getMinecraft().currentScreen instanceof GuiGlitchedMainMenu) {
				drawJpegGlitch();
			}
		}
	}
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onRenderOverlay(RenderGameOverlayEvent.Pre e) {
		if (textGlitch) {
			if (e.getType() == ElementType.ALL) {
				drawTextGlitch(e.getResolution());
			} else if (e.getType() == ElementType.HOTBAR || e.getType() == ElementType.HEALTH ||
					e.getType() == ElementType.FOOD || e.getType() == ElementType.EXPERIENCE) {
				e.setCanceled(true);
			}
		}
	}
	private final ImmutableList<Integer> EGA_COLORS = ImmutableList.of(
				0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA,
				0xFFAA0000, 0xFFAA00AA, 0xFFAA5500, 0xFFAAAAAA,
				0xFF555555, 0xFF5555FF, 0xFF55FF55, 0xFF55FFFF,
				0xFFFF5555, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF
			);
	private Random textGlitchRandom = new Random(0);
	private void drawTextGlitch(ScaledResolution res) {
		int cols = 80;
		int rows = 24;
		textGlitchRandom.setSeed(((int)ticks)/160);
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
		GlStateManager.scale(res.getScaledWidth_double()/((cols+1)*4.5), res.getScaledHeight_double()/((rows+1)*8), 1);
		double midX = cols/2D;
		double midY = rows/2D;
		for (int x = 0; x <= cols; x++) {
			for (int y = 0; y <= rows; y++) {
				int fg = EGA_COLORS.get(textGlitchRandom.nextInt(EGA_COLORS.size()));
				int bg = EGA_COLORS.get(textGlitchRandom.nextInt(EGA_COLORS.size()));
				boolean blink = (textGlitchRandom.nextInt(8) == 0);
				char chr = IBMFontRenderer.CP437.charAt(textGlitchRandom.nextInt(IBMFontRenderer.CP437.length()));
				
				double xDist = (x-midX)/3.5;
				double yDist = y-midY;
				double dist = Math.abs((xDist * xDist) + (yDist * yDist));
				
				int pct = (int)(Math.log10(dist/32.1)*100);
				
				if (Minecraft.getMinecraft().gameSettings.particleSetting == 1) {
					pct /= 2;
				} else if (Minecraft.getMinecraft().gameSettings.particleSetting == 2) {
					pct /= 8;
				}
				
				if (textGlitchRandom.nextInt(100) > pct) continue;
				
				GlStateManager.pushMatrix();
					GlStateManager.translate(x*4.5, y*8, 0);
					GlStateManager.pushMatrix();
						GlStateManager.scale(0.5f, 0.5f, 1);
						Gui.drawRect(0, 0, 9, 16, bg);
					GlStateManager.popMatrix();
					if (!blink || ((int)ticks)%20 < 10) {
						IBMFontRenderer.drawString(0, 0, Character.toString(chr), fg);
					}
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
		GlStateManager.disableAlpha();
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Pre e) {
		if (!(e.getGui() instanceof GuiGlitchedMainMenu)) drawJpegGlitch();
	}
	@SubscribeEvent
	public void onSoundSetup(SoundSetupEvent e) throws SoundSystemException {
		SoundSystemConfig.setCodec("xm", CodecIBXM.class);
		SoundSystemConfig.setCodec("s3m", CodecIBXM.class);
		SoundSystemConfig.setCodec("mod", CodecIBXM.class);
	}
	@SubscribeEvent
	public void onSoundPlay(PlaySoundEvent e) {
		if (glitchTicks >= 0 || Minecraft.getMinecraft().currentScreen instanceof GuiFakeReboot || Minecraft.getMinecraft().currentScreen instanceof GuiGlitchedMainMenu) {
			if (e.getSound() != null && !(e.getSound().getSoundLocation().getResourceDomain().equals("correlated"))
					&& !(e.getSound().getSoundLocation().getResourcePath().contains("glitch"))) {
				e.setResultSound(null);
			}
		}
	}
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent e) {
		if (glitchTicks >= 0 && e.getGui() != null && !(e.getGui() instanceof GuiIngameMenu)) {
			e.setCanceled(true);
		} else if (Minecraft.getMinecraft().world != null
				&& !Minecraft.getMinecraft().world.getWorldInfo().isHardcoreModeEnabled()
				&& e.getGui() instanceof GuiGameOver
				&& !(e.getGui() instanceof GuiAbortRetryFail)) {
			if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.dimension == CConfig.limboDimId) {
				e.setGui(new GuiAbortRetryFail(ReflectionHelper.getPrivateValue(GuiGameOver.class, (GuiGameOver)e.getGui(), "field_184871_f", "causeOfDeath", "f")));
			}
		}
	}
	private void drawJpegGlitch() {
		if (glitchTicks == 0) {
			if (jpegTexture != -1) {
				TextureUtil.deleteTexture(jpegTexture);
				jpegTexture = -1;
			}
			jpegTexture = TextureUtil.glGenTextures();
			BufferedImage raw = ScreenShotHelper.createScreenshot(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, Minecraft.getMinecraft().getFramebuffer());
			Image scaled = raw.getScaledInstance(854, 480, Image.SCALE_AREA_AVERAGING);
			BufferedImage screenshot = new BufferedImage(854, 480, BufferedImage.TYPE_3BYTE_BGR);
			Graphics g = screenshot.createGraphics();
			g.drawImage(scaled, 0, 0, null);
			g.dispose();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				if (corruptionFuture != null) {
					corruptionFuture.cancel(true);
					corruptionFuture = null;
				}
				ImageIO.write(screenshot, "JPEG", baos);
				glitchJpeg = BitSet.valueOf(baos.toByteArray());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (glitchTicks == -1) {
			if (jpegTexture != -1) {
				if (corruptionFuture != null) {
					corruptionFuture.cancel(true);
					corruptionFuture = null;
				}
				TextureUtil.deleteTexture(jpegTexture);
				jpegTexture = -1;
			}
		} else if (glitchTicks > 0) {
			if (corruptionFuture != null && corruptionFuture.isDone()) {
				try {
					TextureUtil.uploadTextureImage(jpegTexture, corruptionFuture.get());
					corruptionFuture = null;
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder vb = tess.getBuffer();
			GlStateManager.color(1, 1, 1);
			GlStateManager.disableDepth();
			GlStateManager.bindTexture(jpegTexture);
			ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
			vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			vb.pos(0, res.getScaledHeight(), 0).tex(0, 1).endVertex();
			vb.pos(res.getScaledWidth(), res.getScaledHeight(), 0).tex(1, 1).endVertex();
			vb.pos(res.getScaledWidth(), 0, 0).tex(1, 0).endVertex();
			vb.pos(0, 0, 0).tex(0, 0).endVertex();
			tess.draw();
			if (seed != null) {
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR,
						SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				IBMFontRenderer.drawString((res.getScaledWidth()-IBMFontRenderer.measure(seed))/2, 2, seed, -1);
				GlStateManager.disableBlend();
			}
			GlStateManager.enableDepth();
			if (Keyboard.isKeyDown(Keyboard.KEY_GRAVE)) {
				glitchTicks = 240;
			}
		}
	}
	
	
	@SubscribeEvent
	public void onStitch(TextureStitchEvent.Pre e) {
		e.getMap().registerSprite(new ResourceLocation("correlated", "blocks/accessory/wireless_endpoint_error"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "blocks/accessory/wireless_endpoint_linked"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "blocks/accessory/terminal_error_glow"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "blocks/controller/controller_power_light"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "blocks/controller/controller_memory_light"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "blocks/accessory/optical_linked"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "blocks/accessory/optical_error"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "items/tool/handheld_terminal_glow"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "items/tool/handheld_terminal_glow_error"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "items/tool/doc_tablet_glow"));
		e.getMap().registerSprite(new ResourceLocation("correlated", "items/keycard/glow"));
	}
	@SubscribeEvent
	public void onSoundLoad(SoundLoadEvent e) {
		for (String snd : CRecords.RECORDS) {
			String file = snd.substring(0, snd.indexOf('.'));
			String ext = snd.substring(snd.indexOf('.')+1);
			Sound danslarue = new Sound("correlated:"+file, 1, 1, 1, Type.FILE, true) {
				@Override
				public ResourceLocation getSoundAsOggLocation() {
					return new ResourceLocation(getSoundLocation().getResourceDomain(), "sounds/music/" + getSoundLocation().getResourcePath() + "." + ext);
				}
			};
			SoundList li = new SoundList(Lists.newArrayList(danslarue), false, null);
			Method m = ReflectionHelper.findMethod(SoundHandler.class, "loadSoundResource", "func_147693_a", ResourceLocation.class, SoundList.class);
			try {
				m.invoke(e.getManager().sndHandler, new ResourceLocation("correlated", file), li);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderHand(RenderSpecificHandEvent e) {
		ItemStack is = e.getItemStack();
		if (is != null) {
			Item item = is.getItem();
			if (item != CItems.HANDHELD_TERMINAL
					&& item != CItems.DRIVE
					&& item != CItems.MEMORY
					&& item != CItems.KEYCARD) return;
			boolean terminalError = false;
			boolean terminalUnlinked = false;
			if (is.getItem() == CItems.HANDHELD_TERMINAL) {
				if (is.getItemDamage() == 1) {
					terminalError = true;
				} else if (is.getItemDamage() == 2) {
					terminalUnlinked = true;
				}
				is = is.copy();
				is.setItemDamage(4);
			}
			Minecraft mc = Minecraft.getMinecraft();
			
			AbstractClientPlayer p = mc.player;
			EnumHand hand = e.getHand();
			ItemRenderer ir = mc.getItemRenderer();
			
			boolean isMain = (hand == EnumHand.MAIN_HAND);
			EnumHandSide handSide = isMain ? p.getPrimaryHand() : p.getPrimaryHand().opposite();
			
			float prevEquippedProgress;
			float equippedProgress;
			
			if (isMain) {
				prevEquippedProgress = ir.prevEquippedProgressMainHand;
				equippedProgress = ir.equippedProgressMainHand;
			} else {
				prevEquippedProgress = ir.prevEquippedProgressOffHand;
				equippedProgress = ir.equippedProgressOffHand;
			}
			
			float partialTicks = e.getPartialTicks();
			
			float swingProgress = p.getSwingProgress(partialTicks);
			EnumHand swingingHand = MoreObjects.firstNonNull(p.swingingHand, EnumHand.MAIN_HAND);
			
			float interpPitch = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * partialTicks;
			float interpYaw = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * partialTicks;
			
			float swing = swingingHand == hand ? swingProgress : 0.0F;
			float equip = 1.0F - (prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTicks);
			
			ir.rotateArroundXAndY(interpPitch, interpYaw);
			ir.setLightmap();
			ir.rotateArm(partialTicks);
			GlStateManager.enableRescaleNormal();

			ir.renderItemInFirstPerson(p, partialTicks, interpPitch, hand, swing, is, equip);
			
			TransformType transform = (handSide == EnumHandSide.RIGHT ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND);
			
			IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(is, mc.world, p);
			
			GlStateManager.pushMatrix();
				float f = -0.4F * MathHelper.sin(MathHelper.sqrt(swing) * (float) Math.PI);
				float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(swing) * ((float) Math.PI * 2F));
				float f2 = -0.2F * MathHelper.sin(swing * (float) Math.PI);
				int i = isMain ? 1 : -1;
				GlStateManager.translate(i * f, f1, f2);
				ir.transformSideFirstPerson(handSide, equip);
				ir.transformFirstPerson(handSide, swing);
				ForgeHooksClient.handleCameraTransforms(model, transform, handSide == EnumHandSide.LEFT);
				GlStateManager.disableCull();
				GlStateManager.translate(-0.5f, 0.5f, 0.03225f);
				GlStateManager.scale(1, -1, 1);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
				GL11.glDisable(GL11.GL_LIGHTING);
				GlStateManager.enableBlend();
				GlStateManager.disableAlpha();
				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				if (item == CItems.HANDHELD_TERMINAL) {
					if (!terminalUnlinked) {
						drawSprite(mc.getTextureMapBlocks().getAtlasSprite("correlated:items/tool/handheld_terminal_glow"+(terminalError ? "_error" : "")));
					}
				} else if (item == CItems.DRIVE) {
					TextureAtlasSprite fullness = mc.getTextureMapBlocks().getAtlasSprite("correlated:items/drive/fullness_light");
					TextureAtlasSprite tier = mc.getTextureMapBlocks().getAtlasSprite("correlated:items/drive/tier_light");
					TextureAtlasSprite partition = mc.getTextureMapBlocks().getAtlasSprite("correlated:items/drive/partition_light");
					TextureAtlasSprite priority_left = mc.getTextureMapBlocks().getAtlasSprite("correlated:items/drive/priority_light_left");
					TextureAtlasSprite priority_mid = mc.getTextureMapBlocks().getAtlasSprite("correlated:items/drive/priority_light_middle");
					TextureAtlasSprite priority_right = mc.getTextureMapBlocks().getAtlasSprite("correlated:items/drive/priority_light_right");
					
					int uncolored;
					if (is.getItemDamage() == 4) {
						uncolored = getColorValues(ColorType.OTHER).getColor("voiddrive_unlit_light");
					} else {
						uncolored = getColorValues(ColorType.OTHER).getColor("drive_unlit_light");
					}
					
					int priorityLeftColor = mc.getItemColors().getColorFromItemstack(is, 4);
					int priorityMidColor = mc.getItemColors().getColorFromItemstack(is, 5);
					int priorityRightColor = mc.getItemColors().getColorFromItemstack(is, 6);
					
					color(mc.getItemColors().getColorFromItemstack(is, 1));
					drawSprite(fullness);
					
					color(mc.getItemColors().getColorFromItemstack(is, 2));
					drawSprite(tier);
					
					color(mc.getItemColors().getColorFromItemstack(is, 3));
					drawSprite(partition);
					
					if (priorityLeftColor != uncolored) {
						color(priorityLeftColor);
						drawSprite(priority_left);
					}
					if (priorityMidColor != uncolored) {
						color(priorityMidColor);
						drawSprite(priority_mid);
					}
					if (priorityRightColor != uncolored) {
						color(priorityRightColor);
						drawSprite(priority_right);
					}
				} else if (item == CItems.MEMORY) {
					TextureAtlasSprite tier = mc.getTextureMapBlocks().getAtlasSprite("correlated:items/accessory/ram_tier_light");
					color(mc.getItemColors().getColorFromItemstack(is, 1));
					drawSprite(tier);
				} else if (item == CItems.KEYCARD) {
					drawSprite(mc.getTextureMapBlocks().getAtlasSprite("correlated:items/keycard/glow"));
				}
				GL11.glEnable(GL11.GL_LIGHTING);
				GlStateManager.enableLighting();
				GlStateManager.disableBlend();
				GlStateManager.enableAlpha();
				ir.setLightmap();
				GlStateManager.enableCull();
			GlStateManager.popMatrix();
			
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			e.setCanceled(true);
		}
	}
	private void color(int packed) {
		GlStateManager.color(((packed >> 16) & 0xFF)/255f, ((packed >> 8) & 0xFF)/255f, (packed&0xFF)/255f);
	}
	public void drawSprite(TextureAtlasSprite tas) {
		float minU = tas.getMinU();
		float minV = tas.getMinV();
		float maxU = tas.getMaxU();
		float maxV = tas.getMaxV();
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();
		vb.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX);
		vb.pos(0, 0, 0).tex(minU, minV).endVertex();
		vb.pos(1, 0, 0).tex(maxU, minV).endVertex();
		vb.pos(0, 1, 0).tex(minU, maxV).endVertex();
		vb.pos(1, 1, 0).tex(maxU, maxV).endVertex();
		tess.draw();
	}

	@SubscribeEvent
	public void onRenderTickForDebugOutput(RenderTickEvent e) {
		if (e.phase == Phase.START && (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
			if (!checkedDebugSupport) {
				if (!GLContext.getCapabilities().GL_KHR_debug) {
					CLog.info("Your driver doesn't support KHR_debug_output...");
					checkedDebugSupport = true;
					return;
				} else {
					CLog.info("KHR_debug_output is supported. Enabling OpenGL debug output!");
				}
				checkedDebugSupport = true;

				GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT);
				GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
				KHRDebug.glDebugMessageCallback(new KHRDebugCallback(new KHRDebugCallback.Handler() {
					
					@Override
					public void handleMessage(int source, int type, int id, int severity, String message) {
						String identityStr;
						switch (type) {
							case GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
								identityStr = "Deprecated Behavior";
								break;
							case GL43.GL_DEBUG_TYPE_ERROR:
								identityStr = "Error";
								break;
							case GL43.GL_DEBUG_TYPE_MARKER:
								identityStr = "Marker";
								break;
							case GL43.GL_DEBUG_TYPE_OTHER:
								identityStr = "Other";
								break;
							case GL43.GL_DEBUG_TYPE_PERFORMANCE:
								identityStr = "Performance";
								break;
							case GL43.GL_DEBUG_TYPE_POP_GROUP:
								identityStr = "Pop Group";
								break;
							case GL43.GL_DEBUG_TYPE_PORTABILITY:
								identityStr = "Portability";
								break;
							case GL43.GL_DEBUG_TYPE_PUSH_GROUP:
								identityStr = "Push Group";
								break;
							case GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
								identityStr = "Undefined Behavior";
								break;
							default:
								identityStr = "Unknown";
								break;
						}
						switch (source) {
							case GL43.GL_DEBUG_SOURCE_API:
								identityStr += " | API";
								break;
							case GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM:
								identityStr += " | Window System";
								break;
							case GL43.GL_DEBUG_SOURCE_SHADER_COMPILER:
								identityStr += " | Shader Compiler";
								break;
							case GL43.GL_DEBUG_SOURCE_THIRD_PARTY:
								identityStr += " | Third Party";
								break;
							case GL43.GL_DEBUG_SOURCE_APPLICATION:
								identityStr += " | Application";
								break;
							case GL43.GL_DEBUG_SOURCE_OTHER:
								identityStr += " | Other";
								break;
							default:
								identityStr += " | Unknown";
								break;
						}
						String idHex = "0x"+Integer.toHexString(id).toUpperCase(Locale.ROOT);
						if (severity == GL43.GL_DEBUG_SEVERITY_NOTIFICATION) {
							LogManager.getLogger("OpenGL").info("["+identityStr+"] "+idHex+" Notification: "+message);
						} else if (severity == GL43.GL_DEBUG_SEVERITY_LOW) {
							LogManager.getLogger("OpenGL").info("["+identityStr+"] "+idHex+" "+message);
						} else if (severity == GL43.GL_DEBUG_SEVERITY_MEDIUM) {
							LogManager.getLogger("OpenGL").warn("["+identityStr+"] "+idHex+" "+message);
						} else if (severity == GL43.GL_DEBUG_SEVERITY_HIGH) {
							LogManager.getLogger("OpenGL").error("["+identityStr+"] "+idHex+" "+message, new OpenGLException().fillInStackTrace());
						}
					}
				}));
				checkedDebugSupport = true;
			}
		}
	}
}

