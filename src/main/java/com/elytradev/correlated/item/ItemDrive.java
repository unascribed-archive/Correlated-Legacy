package com.elytradev.correlated.item;

import java.util.List;
import java.util.Locale;

import com.elytradev.correlated.EnergyHelper;
import com.elytradev.correlated.block.BlockDriveBay;
import com.elytradev.correlated.block.BlockMemoryBay;
import com.elytradev.correlated.helper.ItemStacks;
import com.elytradev.correlated.helper.Numbers;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CItems;
import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.init.CStacks;
import com.elytradev.correlated.proxy.ClientProxy;
import com.elytradev.correlated.storage.InsertResult;
import com.elytradev.correlated.storage.NetworkType;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import com.elytradev.correlated.C28n;
import com.elytradev.correlated.ColorType;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDrive extends Item {
	public enum Priority {
		HIGHEST(TextFormatting.RED),
		HIGHER(TextFormatting.DARK_RED),
		HIGH(TextFormatting.GRAY),
		DEFAULT(TextFormatting.GRAY),
		LOW(TextFormatting.GRAY),
		LOWER(TextFormatting.DARK_GREEN),
		LOWEST(TextFormatting.GREEN);
		public final String lowerName = name().toLowerCase(Locale.ROOT);
		public final TextFormatting color;
		private Priority(TextFormatting color) {
			this.color = color;
		}
	}
	public enum PartitioningMode {
		BLACKLIST, NONE, WHITELIST;
		public final String lowerName = name().toLowerCase(Locale.ROOT);
	}

	public static final int[] tierSizes = {
			1024 * 8,
			4096 * 8,
			16384 * 8,
			65536 * 8,
			-1,
			131072 * 8,
			Integer.MAX_VALUE,
			Integer.MAX_VALUE
	};

	public ItemDrive() {
		setMaxStackSize(1);
	}

	public int getFullnessColor(ItemStack stack) {
		boolean dirty = stack.hasTagCompound() && stack.getTagCompound().getBoolean("Dirty") && itemRand.nextBoolean();
		if (dirty && itemRand.nextInt(20) == 0) {
			stack.getTagCompound().removeTag("Dirty");
		}
		int idx;
		if (stack.getItemDamage() == 4) {
			int x = (int)(((MathHelper.sin(ClientProxy.ticks / 20f)+1) / 2f)*255);
			idx = x;
			if (!dirty) {
				idx += 256;
			}
		} else {
			idx = 512;
			float usedBits = getKilobitsUsed(stack)/(float)getMaxKilobits(stack);
			int x = (int)(usedBits*254f);
			idx += x;
			if (!dirty) {
				idx += 256;
			}
		}
		return ColorType.FADE.getColor(idx);
	}

	public int getTierColor(ItemStack stack) {
		if (stack.getMetadata() == 4) return ColorType.TIER.getColor(16);
		int meta = stack.getMetadata();
		if (meta > 4) {
			meta--;
		}
		return ColorType.TIER.getColor(meta);
	}

	public int getBaseColor(ItemStack stack) {
		if (stack.getItemDamage() == 4) return ColorType.OTHER.getColor("voiddrive_base");
		if (stack.getItemDamage() == 6 || stack.getItemDamage() == 7) return ColorType.OTHER.getColor("creativedrive_base");
		return ColorType.OTHER.getColor("drive_base");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(EnergyHelper.formatPotentialUsage(getPotentialConsumptionRate(stack)));
		if (stack.getItemDamage() == 4) {
			C28n.formatList(tooltip, "tooltip.correlated.void_drive");
		} else {
			long bitsUsed = getKilobitsUsed(stack)*1024L;
			long bitsMax = getMaxKilobits(stack)*1024L;

			int bitsPercent = (int) (((double) bitsUsed / (double) bitsMax) * 100);

			String max = Numbers.humanReadableBits(bitsMax);
			tooltip.add(C28n.format("tooltip.correlated.bytes_used", Numbers.humanReadableBits(bitsUsed), max, bitsPercent));
		}
	}

	public int getPotentialConsumptionRate(ItemStack stack) {
		switch (stack.getItemDamage()) {
			case 0:
				return CConfig.drive1MiBPUsage;
			case 1:
				return CConfig.drive4MiBPUsage;
			case 2:
				return CConfig.drive16MiBPUsage;
			case 3:
				return CConfig.drive64MiBPUsage;
			case 4:
				return CConfig.voidDrivePUsage;
			case 5:
				return CConfig.drive128MiBPUsage;
		}
		return 128;
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.getMetadata() == 6) return com.elytradev.correlated.C28n.format("item.correlated.drive.creative.name");
		if (stack.getMetadata() == 7) return com.elytradev.correlated.C28n.format("item.correlated.drive.vending.name");
		if (stack.getMetadata() == 4) return com.elytradev.correlated.C28n.format("item.correlated.drive.void.name");
		return com.elytradev.correlated.C28n.format("item.correlated.drive.normal.name", Numbers.humanReadableBits(getMaxKilobits(stack)*1024));
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			for (int i = 0; i < tierSizes.length; i++) {
				subItems.add(new ItemStack(this, 1, i));
			}
		}
	}

	public int getMaxKilobits(ItemStack stack) {
		if (stack.getItem() == CItems.MISC && stack.getMetadata() == 8) {
			return Integer.MAX_VALUE;
		}
		return tierSizes[stack.getItemDamage() % tierSizes.length];
	}

	public int getTypeAllocationKilobits(ItemStack stack, NBTTagCompound prototype) {
		return 32 + getNBTComplexity(prototype == null ? null : prototype.getTag("tag"));
	}
	
	public static int getNBTComplexity(NBTBase base) {
		if (base == null) return 0;
		int complexity = 0;
		switch (base.getId()) {
			case NBT.TAG_BYTE:
				complexity += 4;
				break;
			case NBT.TAG_SHORT:
				complexity += 8;
				break;
			case NBT.TAG_INT:
				complexity += 16;
				break;
			case NBT.TAG_LONG:
				complexity += 32;
				break;
			case NBT.TAG_BYTE_ARRAY:
				complexity += 8;
				complexity += ((NBTTagByteArray)base).getByteArray().length*4;
				break;
			case NBT.TAG_INT_ARRAY:
				complexity += 8;
				complexity += ((NBTTagIntArray)base).getIntArray().length*16;
				break;
			case NBT.TAG_STRING:
				String str = ((NBTTagString)base).getString();
				complexity += getStringComplexity(str);
				break;
			case NBT.TAG_LIST:
				NBTTagList li = ((NBTTagList)base);
				complexity += 4;
				for (int i = 0; i < li.tagCount(); i++) {
					complexity += getNBTComplexity(li.get(i));
				}
				break;
			case NBT.TAG_COMPOUND:
				NBTTagCompound compound = ((NBTTagCompound)base);
				complexity += 4;
				for (String k : compound.getKeySet()) {
					// Forestry extensively uses NBT in a way most mods don't
					// Don't penalize it for trying to save item IDs
					if (k.startsWith("forestry.")) continue;
					
					complexity += getStringComplexity(k);
					NBTBase tag = compound.getTag(k);
					if ("Count".equals(k) || "Amount".equals(k) && tag instanceof NBTPrimitive) {
						// !!
						complexity += ((NBTPrimitive)tag).getInt();
					}
					complexity += getNBTComplexity(compound.getTag(k));
				}
				break;
		}
		return complexity;
	}

	public static int getStringComplexity(String str) {
		int complexity = 8;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c < 0xFF) {
				complexity += 4;
			} else {
				complexity += 8;
			}
		}
		return complexity;
	}

	public Priority getPriority(ItemStack stack) {
		return ItemStacks.getEnum(stack, "Priority", Priority.class)
				.or(Priority.DEFAULT);
	}

	public void setPriority(ItemStack stack, Priority priority) {
		ItemStacks.ensureHasTag(stack).getTagCompound().setString("Priority", priority.name());
	}

	public PartitioningMode getPartitioningMode(ItemStack stack) {
		if (stack.getItemDamage() == 7) return PartitioningMode.WHITELIST;
		return ItemStacks.getEnum(stack, "PartitioningMode", PartitioningMode.class)
				.or(PartitioningMode.NONE);
	}

	public void setPartitioningMode(ItemStack stack, PartitioningMode mode) {
		if (stack.getItemDamage() == 7) return;
		ItemStacks.ensureHasTag(stack).getTagCompound().setString("PartitioningMode", mode.name());
	}

	public void markDirty(ItemStack stack) {
		ItemStacks.ensureHasTag(stack).getTagCompound().setBoolean("Dirty", true);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (playerIn.isSneaking()) {
			playerIn.playSound(CSoundEvents.DRIVE_DISASSEMBLE, 0.4f, 0.875f+(itemRand.nextFloat()/4));
			NBTTagList ingredients = ItemStacks.getCompoundList(itemStackIn, "Ingredients");
			if (!worldIn.isRemote) {
				for (int i = 0; i < ingredients.tagCount(); i++) {
					ItemStack is = new ItemStack(ingredients.getCompoundTagAt(i));
					if (is.getItem() == CItems.MISC && (is.getMetadata() == 3 || is.getMetadata() == 8)) continue;
					playerIn.entityDropItem(is, 0.5f);
				}
			}
			if (!ItemStacks.getCompoundList(itemStackIn, "Data").hasNoTags()) {
				ItemStack dataCore = CStacks.dataCore();
				dataCore.setTagCompound(itemStackIn.getTagCompound().copy());
				return ActionResult.newResult(EnumActionResult.SUCCESS, dataCore);
			} else {
				return ActionResult.newResult(EnumActionResult.SUCCESS, CStacks.luminousPearl());
			}
		} else {
			Vec3d eyes = new Vec3d(playerIn.posX, playerIn.posY + playerIn.getEyeHeight(), playerIn.posZ);
			Vec3d look = playerIn.getLookVec();
			Vec3d origin = eyes.addVector(look.x * 4, look.y * 4, look.z * 4);
			RayTraceResult rtr = playerIn.world.rayTraceBlocks(eyes, origin, false, false, true);
			if (rtr.typeOfHit == Type.BLOCK) {
				Block b = worldIn.getBlockState(rtr.getBlockPos()).getBlock();
				if (b instanceof BlockDriveBay || b instanceof BlockMemoryBay) {
					return ActionResult.newResult(EnumActionResult.FAIL, itemStackIn);
				}
			}
			if (!worldIn.isRemote) {
				playerIn.sendMessage(new TextComponentTranslation("msg.correlated.no_rightclick_editor"));
			}
			return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
		}
	}

	// all this code should probably be refactored into some sort of general
	// "NetworkContents" class at some point

	public int getKilobitsUsed(ItemStack stack) {
		if (!stack.hasTagCompound()) return 0;
		if (!stack.getTagCompound().hasKey("Data", NBT.TAG_LIST) || ItemStacks.getCompoundList(stack, "Data").hasNoTags()) {
			return stack.getTagCompound().getInteger("UsedBits");
		}
		NBTTagList list = ItemStacks.getCompoundList(stack, "Data");
		int used = 0;
		for (int i = 0; i < list.tagCount(); i++) {
			used += getTypeAllocationKilobits(stack, list.getCompoundTagAt(i).getCompoundTag("Prototype"));
			used += list.getCompoundTagAt(i).getInteger("Count");
		}
		return used;
	}

	public int getKilobitsFree(ItemStack stack) {
		if (getMaxKilobits(stack) == -1) return Integer.MAX_VALUE;
		return getMaxKilobits(stack) - getKilobitsUsed(stack);
	}

	protected NBTTagCompound createPrototype(ItemStack item) {
		if (item.isEmpty())
			return null;
		NBTTagCompound prototype = item.writeToNBT(new NBTTagCompound());
		prototype.removeTag("Count");
		return prototype;
	}

	protected NBTTagCompound findDataForPrototype(ItemStack drive, NBTTagCompound prototype) {
		int index = findDataIndexForPrototype(drive, prototype);
		if (index == -1) return null;
		return ItemStacks.getCompoundList(drive, "Data").getCompoundTagAt(index);
	}
	protected int findDataIndexForPrototype(ItemStack drive, NBTTagCompound prototype) {
		if (prototype == null)
			return -1;
		NBTTagList list = ItemStacks.getCompoundList(drive, "Data");
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			if (tag.getCompoundTag("Prototype").equals(prototype)) {
				return i;
			}
		}
		return -1;
	}

	public int getKilobitsFreeFor(ItemStack drive, ItemStack item) {
		if (getMaxKilobits(drive) == -1) return Integer.MAX_VALUE;
		if (isBlacklisted(drive, item)) return 0;
		NBTTagCompound prototype = createPrototype(item);
		NBTTagCompound data = findDataForPrototype(drive, prototype);
		if (data != null) {
			return getKilobitsFree(drive);
		} else if (getPartitioningMode(drive) != PartitioningMode.WHITELIST) {
			return Math.max(0, getKilobitsFree(drive) - getTypeAllocationKilobits(drive, prototype));
		}
		return 0;
	}

	/**
	 * Insert as many items as possible from the given stack into a drive.
	 * <p>
	 * The stackSize of the passed stack will be affected.
	 *
	 * @param drive The drive to affect
	 * @param item The item to add
	 * @param simulate If true, the stack size will not be affected and the only
	 * 		useful return value will be the result.
	 * @return An InsertResult including information on whether the item was
	 * 		accepted or not, and if not, why
	 */
	public InsertResult addItem(ItemStack drive, ItemStack item, boolean simulate) {
		if (item == null || item.isEmpty()) {
			return InsertResult.success(item);
		}
		if (isBlacklisted(drive, item)) return InsertResult.itemIncompatible(item);
		if (getMaxKilobits(drive) == -1) {
			if (getPartitioningMode(drive) == PartitioningMode.NONE || findDataIndexForPrototype(drive, createPrototype(item)) != -1) {
				if (!simulate) {
					item.setCount(0);
					markDirty(drive);
				}
				return InsertResult.successVoided(item);
			}
		}
		if (getPartitioningMode(drive) == PartitioningMode.WHITELIST && findDataForPrototype(drive, createPrototype(item)) == null) {
			return InsertResult.itemIncompatible(item);
		}
		int bitsFree = getKilobitsFreeFor(drive, item);
		int amountTaken = Math.min(item.getCount(), bitsFree);
		int current = getAmountStored(drive, item);
		if (amountTaken > 0) {
			if (!simulate) {
				setAmountStored(drive, item, current+amountTaken);
				item.setCount(item.getCount()-amountTaken);
				markDirty(drive);
			}
			return InsertResult.success(item);
		}
		return InsertResult.insufficientStorage(item);
	}

	/**
	 * Take as many items as possible, up to the passed limit, from a drive into
	 * a new stack.
	 * <p>
	 * The stackSize of the passed stack will <b>not</b> be affected, thanks to
	 * Mojang and their air stacks.
	 *
	 * @param drive
	 *            The drive to affect
	 * @param prototype
	 *            The item to remove
	 * @param amountWanted
	 *            The maximum amount to extract
	 */
	public ItemStack removeItems(ItemStack drive, ItemStack prototype, int amountWanted) {
		if (getMaxKilobits(drive) == -1) return ItemStack.EMPTY;
		ItemStack stack = prototype.copy();
		stack.setCount(0);
		int stored = getAmountStored(drive, prototype);
		int amountGiven = Math.min(amountWanted, stored);
		if (amountGiven > 0) {
			setAmountStored(drive, prototype, stored-amountGiven);
			stack.setCount(stack.getCount() + amountGiven);
			markDirty(drive);
		}
		return stack;
	}

	public int getAmountStored(ItemStack drive, ItemStack item) {
		if (getMaxKilobits(drive) == -1) return 0;
		NBTTagCompound data = findDataForPrototype(drive, createPrototype(item));
		if (data == null) return 0;
		return data.getInteger("Count");
	}

	public void setAmountStored(ItemStack drive, ItemStack item, int amount) {
		if (getMaxKilobits(drive) == -1) return;
		NBTTagCompound prototype = createPrototype(item);
		NBTTagList list = ItemStacks.getCompoundList(drive, "Data");
		int index = findDataIndexForPrototype(drive, prototype);
		if (index == -1) {
			allocateType(drive, item, item.getCount());
		} else {
			if (amount <= 0 && getPartitioningMode(drive) != PartitioningMode.WHITELIST) {
				deallocateType(drive, item);
			} else {
				list.getCompoundTagAt(index).setInteger("Count", amount);
				list.getCompoundTagAt(index).setLong("LastModified", System.currentTimeMillis());
			}
		}
		markDirty(drive);
	}

	/**
	 * Get all NetworkTypes available or partitioned in this drive. The stacks
	 * within the types returned will all have a count of 1.
	 */
	public List<NetworkType> getPrototypes(ItemStack drive) {
		NBTTagList list = ItemStacks.getCompoundList(drive, "Data");
		List<NetworkType> rtrn = Lists.newArrayList();
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			ItemStack is = new ItemStack(tag.getCompoundTag("Prototype"));
			is.setCount(1);
			rtrn.add(new NetworkType(is, tag.getLong("LastModified")));
		}
		return rtrn;
	}

	/**
	 * Get all NetworkTypes available in this drive. The stacks within the types
	 * returned will match the amount of items in the drive.
	 */
	public List<NetworkType> getTypes(ItemStack drive) {
		if (getMaxKilobits(drive) == -1) return Lists.newArrayList();
		NBTTagList list = ItemStacks.getCompoundList(drive, "Data");
		List<NetworkType> rtrn = Lists.newArrayList();
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			int count = tag.getInteger("Count");
			if (count > 0) {
				ItemStack is = new ItemStack(tag.getCompoundTag("Prototype"));
				if (drive.getItemDamage() == 7) {
					is.setCount(Integer.MAX_VALUE);
					ItemStacks.ensureHasTag(is).getTagCompound().setBoolean("correlated:FromVendingDrive", true);
				} else {
					is.setCount(count);
				}
				rtrn.add(new NetworkType(is, tag.getLong("LastModified")));
			}
		}
		return rtrn;
	}

	/**
	 * Forcefully allocates a type for the given item. It is the
	 * responsibility of the caller to ensure there is space for
	 * the type. Does nothing if there is already an allocation
	 * for this type. Allocation will succeed even if the count
	 * is zero.
	 */
	public void allocateType(ItemStack drive, ItemStack item, int count) {
		if (drive.getItemDamage() == 7) {
			count = 1;
		}
		NBTTagCompound prototype = createPrototype(item);
		int idx = findDataIndexForPrototype(drive, prototype);
		if (idx == -1) {
			NBTTagCompound data = new NBTTagCompound();
			data.setTag("Prototype", prototype);
			data.setInteger("Count", count);
			data.setLong("LastModified", System.currentTimeMillis());
			ItemStacks.getCompoundList(drive, "Data").appendTag(data);
			markDirty(drive);
		}
	}

	/**
	 * Forcefully deallocates a type for the given item. If there
	 * were any items of the given type stored in this drive,
	 * they will be deleted.
	 */
	public void deallocateType(ItemStack drive, ItemStack item) {
		NBTTagCompound prototype = createPrototype(item);
		int idx = findDataIndexForPrototype(drive, prototype);
		if (idx != -1) {
			ItemStacks.getCompoundList(drive, "Data").removeTag(idx);
			markDirty(drive);
		}
	}
	
	
	protected NBTTagCompound findBlacklistForPrototype(ItemStack drive, NBTTagCompound prototype) {
		int index = findBlacklistIndexForPrototype(drive, prototype);
		if (index == -1) return null;
		return ItemStacks.getCompoundList(drive, "Blacklist").getCompoundTagAt(index);
	}
	protected int findBlacklistIndexForPrototype(ItemStack drive, NBTTagCompound prototype) {
		if (prototype == null)
			return -1;
		NBTTagList list = ItemStacks.getCompoundList(drive, "Blacklist");
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			if (tag.getCompoundTag("Prototype").equals(prototype)) {
				return i;
			}
		}
		return -1;
	}
	
	
	public void blacklistType(ItemStack drive, ItemStack item) {
		NBTTagCompound prototype = createPrototype(item);
		int idx = findBlacklistIndexForPrototype(drive, prototype);
		if (idx == -1) {
			NBTTagCompound data = new NBTTagCompound();
			data.setTag("Prototype", prototype);
			ItemStacks.getCompoundList(drive, "Blacklist").appendTag(data);
			markDirty(drive);
		}
	}
	
	public void unblacklistType(ItemStack drive, ItemStack item) {
		NBTTagCompound prototype = createPrototype(item);
		int idx = findBlacklistIndexForPrototype(drive, prototype);
		if (idx != -1) {
			ItemStacks.getCompoundList(drive, "Blacklist").removeTag(idx);
			markDirty(drive);
		}
	}
	
	public boolean isBlacklisted(ItemStack drive, ItemStack item) {
		if (getPartitioningMode(drive) != PartitioningMode.BLACKLIST) return false;
		return findBlacklistIndexForPrototype(drive, createPrototype(item)) != -1;
	}
	
	public List<ItemStack> getBlacklistedTypes(ItemStack drive) {
		if (getPartitioningMode(drive) != PartitioningMode.BLACKLIST) return Lists.newArrayList();
		NBTTagList list = ItemStacks.getCompoundList(drive, "Blacklist");
		List<ItemStack> rtrn = Lists.newArrayList();
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			ItemStack is = new ItemStack(tag.getCompoundTag("Prototype"));
			is.setCount(1);
			rtrn.add(is);
		}
		return rtrn;
	}
	
	
	
	public boolean isCreativeDrive(ItemStack drive) {
		return drive.getItemDamage() == 6 || drive.getItemDamage() == 7;
	}

}
