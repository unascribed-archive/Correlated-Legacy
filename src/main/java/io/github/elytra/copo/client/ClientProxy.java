package io.github.elytra.copo.client;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.Proxy;
import io.github.elytra.copo.client.gui.GuiAbortRetryFail;
import io.github.elytra.copo.client.gui.GuiFakeReboot;
import io.github.elytra.copo.client.gui.GuiGlitchedMainMenu;
import io.github.elytra.copo.client.render.entity.RenderAutomaton;
import io.github.elytra.copo.client.render.entity.RenderThrownItem;
import io.github.elytra.copo.client.render.tile.RenderController;
import io.github.elytra.copo.client.render.tile.RenderDriveBay;
import io.github.elytra.copo.client.render.tile.RenderImporterChest;
import io.github.elytra.copo.client.render.tile.RenderMemoryBay;
import io.github.elytra.copo.client.render.tile.RenderVT;
import io.github.elytra.copo.client.render.tile.RenderWirelessReceiver;
import io.github.elytra.copo.client.render.tile.RenderWirelessTransmitter;
import io.github.elytra.copo.entity.EntityAutomaton;
import io.github.elytra.copo.entity.EntityThrownItem;
import io.github.elytra.copo.item.ItemDrive;
import io.github.elytra.copo.item.ItemKeycard;
import io.github.elytra.copo.item.ItemMemory;
import io.github.elytra.copo.item.ItemMisc;
import io.github.elytra.copo.item.ItemModule;
import io.github.elytra.copo.tile.TileEntityController;
import io.github.elytra.copo.tile.TileEntityDriveBay;
import io.github.elytra.copo.tile.TileEntityImporterChest;
import io.github.elytra.copo.tile.TileEntityMemoryBay;
import io.github.elytra.copo.tile.TileEntityVT;
import io.github.elytra.copo.tile.TileEntityWirelessReceiver;
import io.github.elytra.copo.tile.TileEntityWirelessTransmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.Sound.Type;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
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

public class ClientProxy extends Proxy {
	public static float ticks = 0;
	
	public static int glitchTicks = -1;
	private BitSet glitchJpeg;
	private int jpegTexture = -1;
	private Random rand = new Random();
	
	private Set<String> knownColorTypes = Sets.newHashSet("tier", "fullness", "other", "pci");
	private Map<String, int[]> colors = Maps.newHashMap();
	
	private Future<BufferedImage> corruptionFuture;
	private ExecutorService jpegCorruptor = Executors.newFixedThreadPool(1, (r) -> new Thread(r, "JPEG Corruption Thread"));
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
	
	@SuppressWarnings("deprecation")
	@Override
	public void preInit() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityController.class, new RenderController());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDriveBay.class, new RenderDriveBay());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMemoryBay.class, new RenderMemoryBay());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVT.class, new RenderVT());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWirelessReceiver.class, new RenderWirelessReceiver());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWirelessTransmitter.class, new RenderWirelessTransmitter());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityImporterChest.class, new RenderImporterChest());
		
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CoPo.wireless_endpoint), 0, TileEntityWirelessReceiver.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CoPo.wireless_endpoint), 1, TileEntityWirelessTransmitter.class);
		
		RenderingRegistry.registerEntityRenderingHandler(EntityThrownItem.class, (rm) -> new RenderThrownItem(rm, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityAutomaton.class, RenderAutomaton::new);
		
		MinecraftForge.EVENT_BUS.register(this);

		((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener((rm) -> {
			colors.clear();
			for (String s : knownColorTypes) {
				CoPo.log.info("Loading {} colors", s);
				try {
					IResource res = rm.getResource(new ResourceLocation("correlatedpotentialistics", "textures/misc/"+s+"_colors.png"));
					InputStream in = res.getInputStream();
					BufferedImage img = ImageIO.read(in);
					in.close();
					int[] rgb = new int[img.getWidth()*img.getHeight()];
					img.getRGB(0, 0, img.getWidth(), img.getHeight(), rgb, 0, img.getWidth());
					colors.put(s, rgb);
					CoPo.log.info("Successfully loaded {} colors", s);
				} catch (IOException e) {
					CoPo.log.info("Error while loading {} colors", s);
				}
			}
		});
		
		ModelLoader.setCustomModelResourceLocation(CoPo.floppy, 0, new ModelResourceLocation(new ResourceLocation("correlatedpotentialistics", "floppy_write_enabled"), "inventory"));
		ModelLoader.setCustomModelResourceLocation(CoPo.floppy, 1, new ModelResourceLocation(new ResourceLocation("correlatedpotentialistics", "floppy_write_disabled"), "inventory"));
		
		int idx = 0;
		for (String s : ItemMisc.items) {
			ModelLoader.setCustomModelResourceLocation(CoPo.misc, idx++, new ModelResourceLocation(new ResourceLocation("correlatedpotentialistics", s), "inventory"));
		}
		idx = 0;
		for (String s : ItemKeycard.colors) {
			ModelLoader.setCustomModelResourceLocation(CoPo.keycard, idx++, new ModelResourceLocation(new ResourceLocation("correlatedpotentialistics", "keycard_"+s), "inventory"));
		}
	}
	@Override
	public int getColor(String group, int index) {
		if (index < 0) return -1;
		if (!colors.containsKey(group)) return -1;
		int[] rgb = colors.get(group);
		if (rgb == null || index >= rgb.length) return -1;
		return rgb[index] | 0xFF000000;
	}
	@Override
	public void postInit() {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if (stack == null || !(stack.getItem() instanceof ItemModule)) return -1;
				ItemModule id = (ItemModule)stack.getItem();
				if (tintIndex == 1) {
					return id.getTypeColor(stack);
				}
				return -1;
			}
			
		}, CoPo.module);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if (stack == null || !(stack.getItem() instanceof ItemMemory)) return -1;
				ItemMemory id = (ItemMemory)stack.getItem();
				if (tintIndex == 1) {
					return id.getTierColor(stack);
				}
				return -1;
			}
			
		}, CoPo.memory);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			@SuppressWarnings("fallthrough")
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if (stack == null || !(stack.getItem() instanceof ItemDrive)) return -1;
				ItemDrive id = (ItemDrive)stack.getItem();
				if (tintIndex == 1) {
					return id.getFullnessColor(stack);
				} else if (tintIndex == 2) {
					return id.getTierColor(stack);
				} else if (tintIndex == 3) {
					switch (id.getPartitioningMode(stack)) {
						case NONE:
							return getColor("other", 1);
						case WHITELIST:
							return getColor("other", 0);
						//case BLACKLIST:
						//	return getColor("other", 2);
					}
				} else if (tintIndex >= 4 && tintIndex <= 6) {
					int uncolored;
					if (stack.getItemDamage() == 4) {
						uncolored = getColor("other", 32);
					} else {
						uncolored = getColor("other", 48);
					}

					int red = getColor("other", 16);
					int green = getColor("other", 17);
					
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
			
		}, CoPo.drive);
	}
	@Override
	public void registerItemModel(Item item, int variants) {
		ResourceLocation loc = Item.REGISTRY.getNameForObject(item);
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
	@Override
	public void weldthrowerTick(EntityPlayer player) {
		Vec3d look = player.getLookVec();
		Vec3d right = look.rotateYaw(-90);
		double dist = 0.5;
		double gap = 1;
		double fuzz = 0.05;
		look.rotateYaw(20);
		for (int i = 0; i < 5; i++) {
			Random rand = player.worldObj.rand;
			ParticleWeldthrower dust = new ParticleWeldthrower(player.worldObj,
					player.posX+(right.xCoord*dist)+(look.xCoord*gap),
					player.posY+(player.getEyeHeight()-0.1)+(right.yCoord*dist)+(look.yCoord*gap),
					player.posZ+(right.zCoord*dist)+(look.zCoord*gap), 1);
			dust.setRBGColorF(0, 0.9725490196078431f-(rand.nextFloat()/5), 0.8235294117647059f-(rand.nextFloat()/5));
			dust.setMotion(look.xCoord+(rand.nextGaussian()*fuzz), look.yCoord+(rand.nextGaussian()*fuzz), look.zCoord+(rand.nextGaussian()*fuzz));
			Minecraft.getMinecraft().effectRenderer.addEffect(dust);
		}
	}
	@Override
	public void weldthrowerHeal(EntityAutomaton ent) {
		for (int i = 0; i < 5; i++) {
			Random rand = ent.worldObj.rand;
			ParticleWeldthrower dust = new ParticleWeldthrower(ent.worldObj,
					ent.posX+(rand.nextGaussian()*0.2),
					ent.posY+(rand.nextGaussian()*0.2),
					ent.posZ+(rand.nextGaussian()*0.2), 1);
			dust.setRBGColorF(0, 0.9725490196078431f-(rand.nextFloat()/5), 0.8235294117647059f-(rand.nextFloat()/5));
			Minecraft.getMinecraft().effectRenderer.addEffect(dust);
		}
	}
	@Override
	public void smokeTick(EntityAutomaton ent) {
		if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().theWorld.getTotalWorldTime() < 10) return;
		Random rand = ent.worldObj.rand;
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
			if (glitchTicks > -1 && Minecraft.getMinecraft().theWorld == null) {
				glitchTicks = -1;
			}
			if (glitchTicks > -1 && jpegTexture != -1 && !Minecraft.getMinecraft().isGamePaused()) {
				glitchTicks++;
				if (glitchTicks >= 240) {
					glitchTicks = -1;
					Minecraft.getMinecraft().getSoundHandler().stopSounds();
					if (Minecraft.getMinecraft().thePlayer != null && !Minecraft.getMinecraft().thePlayer.isDead) {
						Minecraft.getMinecraft().displayGuiScreen(new GuiFakeReboot());
					}
				}
				if (glitchJpeg != null && corruptionFuture == null) {
					corruptionFuture = jpegCorruptor.submit(jpegCorruptionTask);
				}
			}
		}
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onRenderTick(RenderTickEvent e) {
		if (e.phase == Phase.START) {
			ticks = ((int)ticks)+e.renderTickTime;
		} else if (e.phase == Phase.END) {
			if (Minecraft.getMinecraft().currentScreen == null || Minecraft.getMinecraft().currentScreen instanceof GuiGlitchedMainMenu) {
				drawGlitch();
			}
		}
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Pre e) {
		if (!(e.getGui() instanceof GuiGlitchedMainMenu)) drawGlitch();
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
			if (e.getSound() != null && !(e.getSound().getSoundLocation().getResourceDomain().equals("correlatedpotentialistics"))
					&& !(e.getSound().getSoundLocation().getResourcePath().contains("glitch"))) {
				e.setResultSound(null);
			}
		}
	}
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent e) {
		if (glitchTicks >= 0 && e.getGui() != null && !(e.getGui() instanceof GuiIngameMenu)) {
			e.setCanceled(true);
		} else if (Minecraft.getMinecraft().theWorld != null
				&& !Minecraft.getMinecraft().theWorld.getWorldInfo().isHardcoreModeEnabled()
				&& e.getGui() instanceof GuiGameOver
				&& !(e.getGui() instanceof GuiAbortRetryFail)) {
			if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.dimension == CoPo.limboDimId) {
				e.setGui(new GuiAbortRetryFail(ReflectionHelper.getPrivateValue(GuiGameOver.class, (GuiGameOver)e.getGui(), "field_184871_f", "causeOfDeath", "f")));
			}
		}
	}
	private void drawGlitch() {
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
			VertexBuffer vb = tess.getBuffer();
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
			GlStateManager.enableDepth();
		}
	}
	
	
	@SubscribeEvent
	public void onStitch(TextureStitchEvent.Pre e) {
		e.getMap().registerSprite(new ResourceLocation("correlatedpotentialistics", "blocks/wireless_endpoint_error"));
		e.getMap().registerSprite(new ResourceLocation("correlatedpotentialistics", "blocks/wireless_endpoint_linked"));
		e.getMap().registerSprite(new ResourceLocation("correlatedpotentialistics", "items/wireless_vt_glow"));
		e.getMap().registerSprite(new ResourceLocation("correlatedpotentialistics", "items/doc_tablet_glow"));
		e.getMap().registerSprite(new ResourceLocation("correlatedpotentialistics", "items/keycard_glow"));
	}
	@SubscribeEvent
	public void onSoundLoad(SoundLoadEvent e) {
		for (String snd : CoPo.records) {
			String file = snd.substring(0, snd.indexOf('.'));
			String ext = snd.substring(snd.indexOf('.')+1);
			Sound danslarue = new Sound("correlatedpotentialistics:"+file, 1, 1, 1, Type.FILE, true) {
				@Override
				public ResourceLocation getSoundAsOggLocation() {
					return new ResourceLocation(getSoundLocation().getResourceDomain(), "sounds/music/" + getSoundLocation().getResourcePath() + "." + ext);
				}
			};
			SoundList li = new SoundList(Lists.newArrayList(danslarue), false, null);
			Method m = ReflectionHelper.findMethod(SoundHandler.class, e.getManager().sndHandler, new String[] {"func_147693_a", "loadSoundResource", "a"}, ResourceLocation.class, SoundList.class);
			try {
				m.invoke(e.getManager().sndHandler, new ResourceLocation("correlatedpotentialistics", file), li);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderHand(RenderSpecificHandEvent e) {
		if (e.getItemStack() != null) {
			Item item = e.getItemStack().getItem();
			if (item != CoPo.wireless_terminal
					&& item != CoPo.drive
					&& item != CoPo.memory
					&& item != CoPo.keycard) return;
			Minecraft mc = Minecraft.getMinecraft();
			
			AbstractClientPlayer p = mc.thePlayer;
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
			EnumHand swingingHand = Objects.firstNonNull(p.swingingHand, EnumHand.MAIN_HAND);
			
			float interpPitch = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * partialTicks;
			float interpYaw = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * partialTicks;
			
			float swing = swingingHand == hand ? swingProgress : 0.0F;
			float equip = 1.0F - (prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTicks);
			
			ir.rotateArroundXAndY(interpPitch, interpYaw);
			ir.setLightmap();
			ir.rotateArm(partialTicks);
			GlStateManager.enableRescaleNormal();

			ir.renderItemInFirstPerson(p, partialTicks, interpPitch, hand, swing, e.getItemStack(), equip);
			
			TransformType transform = (handSide == EnumHandSide.RIGHT ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND);
			
			IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(e.getItemStack(), mc.theWorld, p);
			
			GlStateManager.pushMatrix();
				float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swing) * (float) Math.PI);
				float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swing) * ((float) Math.PI * 2F));
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
				if (item == CoPo.wireless_terminal) {
					drawSprite(mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/wireless_vt_glow"));
				} else if (item == CoPo.drive) {
					TextureAtlasSprite fullness = mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/drive_fullness_light");
					TextureAtlasSprite tier = mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/drive_tier_light");
					TextureAtlasSprite partition = mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/drive_partition_light");
					TextureAtlasSprite priority_left = mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/drive_priority_light_left");
					TextureAtlasSprite priority_mid = mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/drive_priority_light_middle");
					TextureAtlasSprite priority_right = mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/drive_priority_light_right");
					
					int uncolored;
					if (e.getItemStack().getItemDamage() == 4) {
						uncolored = getColor("other", 32);
					} else {
						uncolored = getColor("other", 48);
					}
					
					int priorityLeftColor = mc.getItemColors().getColorFromItemstack(e.getItemStack(), 4);
					int priorityMidColor = mc.getItemColors().getColorFromItemstack(e.getItemStack(), 5);
					int priorityRightColor = mc.getItemColors().getColorFromItemstack(e.getItemStack(), 6);
					
					color(mc.getItemColors().getColorFromItemstack(e.getItemStack(), 1));
					drawSprite(fullness);
					
					color(mc.getItemColors().getColorFromItemstack(e.getItemStack(), 2));
					drawSprite(tier);
					
					color(mc.getItemColors().getColorFromItemstack(e.getItemStack(), 3));
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
				} else if (item == CoPo.memory) {
					TextureAtlasSprite tier = mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/ram_tier_light");
					color(mc.getItemColors().getColorFromItemstack(e.getItemStack(), 1));
					drawSprite(tier);
				} else if (item == CoPo.keycard) {
					drawSprite(mc.getTextureMapBlocks().getAtlasSprite("correlatedpotentialistics:items/keycard_glow"));
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
		VertexBuffer vb = tess.getBuffer();
		vb.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX);
		vb.pos(0, 0, 0).tex(minU, minV).endVertex();
		vb.pos(1, 0, 0).tex(maxU, minV).endVertex();
		vb.pos(0, 1, 0).tex(minU, maxV).endVertex();
		vb.pos(1, 1, 0).tex(maxU, maxV).endVertex();
		tess.draw();
	}
}
