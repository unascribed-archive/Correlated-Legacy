package com.unascribed.correlatedpotentialistics.item;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.unascribed.correlatedpotentialistics.CoPo;
import com.unascribed.correlatedpotentialistics.client.ClientProxy;
import com.unascribed.correlatedpotentialistics.helper.ItemStacks;
import com.unascribed.correlatedpotentialistics.helper.Numbers;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDrive extends Item implements IItemColor {
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
		/*BLACKLIST, TODO*/ NONE, WHITELIST;
		public final String lowerName = name().toLowerCase(Locale.ROOT);
	}

	private final int[] tierColors = {
			0xFF1744, // Red A400
			0xFF9100, // Orange A400
			0x76FF03, // Light Green A400
			0x1DE9B6, // Teal A400
			0xD500F9, // Purple A400
	};
	private final int[] tierSizes = {
			1024 * 8,
			4096 * 8,
			16384 * 8,
			65536 * 8,
			-1
	};
	private final int[] tierAllocSizes = {
			8 * 8,
			32 * 8,
			128 * 8,
			512 * 8,
			0
	};

	public ItemDrive() {
		setMaxStackSize(1);
	}

	public int getFullnessColor(ItemStack stack) {
		int r;
		int g;
		int b;
		boolean dirty = stack.hasTagCompound() && stack.getTagCompound().getBoolean("Dirty") && itemRand.nextBoolean();
		if (dirty && itemRand.nextInt(20) == 0) {
			stack.getTagCompound().removeTag("Dirty");
		}
		if (stack.getItemDamage() == 4) {
			if (dirty) return 0xFF00FF;
			float sin = (MathHelper.sin(ClientProxy.ticks / 20f) + 2.5f) / 5f;
			r = ((int) (sin * 192f)) & 0xFF;
			g = 0;
			b = ((int) (sin * 255f)) & 0xFF;
			return r << 16 | g << 8 | b;
		} else {
			float usedTypes = getTypesUsed(stack)/(float)getMaxTypes(stack);
			float usedBits = getBitsUsed(stack)/(float)getMaxBits(stack);
			float both = (usedTypes+usedBits)/2;
			float hue = (1/3f)*(1-both);
			return Color.HSBtoRGB(hue, 1, dirty ? 1 : 0.65f);
		}
	}

	public int getTierColor(ItemStack stack) {
		return tierColors[stack.getItemDamage() % tierColors.length];
	}

	public int getBaseColor(ItemStack stack) {
		return stack.getItemDamage() == 4 ? 0x554455 : 0xFFFFFF;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("fallthrough")
	public int getColorFromItemstack(ItemStack stack, int tintIndex) {
		if (tintIndex == 1) {
			return getFullnessColor(stack);
		} else if (tintIndex == 2) {
			return getTierColor(stack);
		} else if (tintIndex == 3) {
			switch (getPartitioningMode(stack)) {
				case NONE:
					return 0x00FFAA;
				case WHITELIST:
					return 0xFFFFFF;
			}
		} else if (tintIndex >= 4 && tintIndex <= 6) {
			int uncolored;
			if (stack.getItemDamage() == 4) {
				uncolored = 0;
			} else {
				uncolored = 0x555555;
			}

			int left = uncolored;
			int middle = uncolored;
			int right = uncolored;
			switch (getPriority(stack)) {
				case HIGHEST:
					right = 0xFF0000;
				case HIGHER:
					middle = 0xFF0000;
				case HIGH:
					left = 0xFF0000;
					break;
				case LOWEST:
					left = 0x00FF00;
				case LOWER:
					middle = 0x00FF00;
				case LOW:
					right = 0x00FF00;
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
		return getBaseColor(stack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.translateToLocalFormatted("tooltip.correlatedpotentialistics.rf_usage", getRFConsumptionRate(stack)));
		if (stack.getItemDamage() == 4) {
			int i = 0;
			while (I18n.canTranslate("tooltip.correlatedpotentialistics.void_drive." + i)) {
				tooltip.add(I18n.translateToLocalFormatted("tooltip.correlatedpotentialistics.void_drive." + i));
				i++;
			}
		} else {
			int typesUsed = getTypesUsed(stack);
			int typesMax = getMaxTypes(stack);
			int bytesUsed = getBitsUsed(stack) / 8;
			int bytesMax = getMaxBits(stack) / 8;

			int typesPercent = (int) (((double) typesUsed / (double) typesMax) * 100);
			int bytesPercent = (int) (((double) bytesUsed / (double) bytesMax) * 100);

			tooltip.add(I18n.translateToLocalFormatted("tooltip.correlatedpotentialistics.types_used", typesUsed, typesMax, typesPercent));
			tooltip.add(I18n.translateToLocalFormatted("tooltip.correlatedpotentialistics.bytes_used", Numbers.humanReadableBytes(bytesUsed), Numbers.humanReadableBytes(bytesMax), bytesPercent));
		}
	}

	public int getRFConsumptionRate(ItemStack stack) {
		if (stack.getItemDamage() == 4) {
			return 4;
		}
		int dmg = stack.getItemDamage() + 1;
		return ((int) Math.pow(2, dmg))/2;
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + stack.getItemDamage();
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < tierColors.length; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	public int getMaxTypes(ItemStack stack) {
		return 64;
	}

	public int getMaxBits(ItemStack stack) {
		return tierSizes[stack.getItemDamage() % tierSizes.length];
	}

	public int getTypeAllocationBits(ItemStack stack) {
		return tierAllocSizes[stack.getItemDamage() % tierSizes.length];
	}

	public Priority getPriority(ItemStack stack) {
		return ItemStacks.getEnum(stack, "Priority", Priority.class)
				.or(Priority.DEFAULT);
	}

	public void setPriority(ItemStack stack, Priority priority) {
		ItemStacks.ensureHasTag(stack).getTagCompound().setString("Priority", priority.name());
	}

	public PartitioningMode getPartitioningMode(ItemStack stack) {
		return ItemStacks.getEnum(stack, "PartitioningMode", PartitioningMode.class)
				.or(PartitioningMode.NONE);
	}

	public void setPartitioningMode(ItemStack stack, PartitioningMode mode) {
		ItemStacks.ensureHasTag(stack).getTagCompound().setString("PartitioningMode", mode.name());
	}

	public void markDirty(ItemStack stack) {
		ItemStacks.ensureHasTag(stack).getTagCompound().setBoolean("Dirty", true);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.openGui(CoPo.inst, 1, worldIn, playerIn.inventory.currentItem, 0, 0);
		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}

	// all this code should probably be refactored into some sort of general
	// "NetworkContents" class at some point

	public int getTypesUsed(ItemStack stack) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Data", NBT.TAG_LIST)) return 0;
		return ItemStacks.getCompoundList(stack, "Data").tagCount();
	}

	public int getBitsUsed(ItemStack stack) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Data", NBT.TAG_LIST)) return 0;
		NBTTagList list = ItemStacks.getCompoundList(stack, "Data");
		int used = 0;
		for (int i = 0; i < list.tagCount(); i++) {
			used += getTypeAllocationBits(stack);
			used += list.getCompoundTagAt(i).getInteger("Count");
		}
		return used;
	}

	public int getBitsFree(ItemStack stack) {
		if (getMaxBits(stack) == -1) return Integer.MAX_VALUE;
		return getMaxBits(stack) - getBitsUsed(stack);
	}

	protected NBTTagCompound createPrototype(ItemStack item) {
		if (item == null)
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

	public int getBitsFreeFor(ItemStack drive, ItemStack item) {
		if (getMaxBits(drive) == -1) return Integer.MAX_VALUE;
		NBTTagCompound data = findDataForPrototype(drive, createPrototype(item));
		if (data != null) {
			return getBitsFree(drive);
		} else if (getPartitioningMode(drive) == PartitioningMode.NONE && getTypesUsed(drive) < getMaxTypes(drive)) {
			return Math.max(0, getBitsFree(drive) - getTypeAllocationBits(drive));
		}
		return 0;
	}

	/**
	 * Insert as many items as possible from the given stack into a drive.
	 * <p>
	 * The stackSize of the passed stack will be affected. Return value is for
	 * convenience, and will be null if all items are taken.
	 *
	 * @param drive
	 *            The drive to affect
	 * @param item
	 *            The item to add
	 * @return The item passed in
	 */
	public ItemStack addItem(ItemStack drive, ItemStack item) {
		if (getMaxBits(drive) == -1) {
			if (getPartitioningMode(drive) == PartitioningMode.NONE || findDataIndexForPrototype(drive, createPrototype(item)) != -1) {
				item.stackSize = 0;
				return null;
			} else {
				return item;
			}
		}
		int bitsFree = getBitsFreeFor(drive, item);
		int amountTaken = Math.min(item.stackSize, bitsFree);
		int current = getAmountStored(drive, item);
		if (amountTaken > 0) {
			setAmountStored(drive, item, current+amountTaken);
			item.stackSize -= amountTaken;
			markDirty(drive);
		}
		if (item.stackSize <= 0) {
			return null;
		} else {
			return item;
		}
	}

	/**
	 * Take as many items as possible, up to the passed limit, from a drive into
	 * the given stack.
	 * <p>
	 * The stackSize of the passed stack will be affected. Return value is for
	 * convenience.
	 *
	 * @param drive
	 *            The drive to affect
	 * @param item
	 *            The item to affect
	 * @param amountWanted
	 *            The maximum amount to extract
	 */
	public ItemStack removeItems(ItemStack drive, ItemStack stack, int amountWanted) {
		if (getMaxBits(drive) == -1) return null;
		int stored = getAmountStored(drive, stack);
		int amountGiven = Math.min(amountWanted, stored);
		if (amountGiven > 0) {
			setAmountStored(drive, stack, stored-amountGiven);
			stack.stackSize += amountGiven;
			markDirty(drive);
		}
		return stack;
	}

	public int getAmountStored(ItemStack drive, ItemStack item) {
		if (getMaxBits(drive) == -1) return 0;
		NBTTagCompound data = findDataForPrototype(drive, createPrototype(item));
		if (data == null) return 0;
		return data.getInteger("Count");
	}

	public void setAmountStored(ItemStack drive, ItemStack item, int amount) {
		if (getMaxBits(drive) == -1) return;
		NBTTagCompound prototype = createPrototype(item);
		NBTTagList list = ItemStacks.getCompoundList(drive, "Data");
		int index = findDataIndexForPrototype(drive, prototype);
		if (index == -1) {
			allocateType(drive, item, item.stackSize);
		} else {
			if (amount <= 0 && getPartitioningMode(drive) == PartitioningMode.NONE) {
				deallocateType(drive, item);
			} else {
				list.getCompoundTagAt(index).setInteger("Count", amount);
			}
		}
		markDirty(drive);
	}

	/**
	 * Creates a list of "prototype" (stack size zero) itemstacks based on the
	 * stored data in the given drive.
	 * <p>
	 * As these are newly created, it is safe to just modify the stack size and
	 * use them without copying.
	 */
	public List<ItemStack> getPrototypes(ItemStack drive) {
		NBTTagList list = ItemStacks.getCompoundList(drive, "Data");
		List<ItemStack> rtrn = Lists.newArrayList();
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			ItemStack is = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Prototype"));
			if (is == null) {
				list.removeTag(i);
				continue;
			}
			rtrn.add(is);
		}
		return rtrn;
	}

	/**
	 * Creates a list of itemstacks based on the stored data in the given drive.
	 * <p>
	 * Unlike the getPrototypes method, these itemstacks will have stack sizes
	 * matching the amount of items in the drive. These can be potentially
	 * insane values that are <i>far</i> outside the item's max stack size
	 * range. Always use splitStack before passing to another method. Also
	 * unlike getPrototypes, this method will skip items that are partitioned
	 * but have none stored.
	 * <p>
	 * As these are newly created, it is safe to just modify the stack
	 * size and use them without copying.
	 */
	public List<ItemStack> getTypes(ItemStack drive) {
		if (getMaxBits(drive) == -1) return Lists.newArrayList();
		NBTTagList list = ItemStacks.getCompoundList(drive, "Data");
		List<ItemStack> rtrn = Lists.newArrayList();
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			int count = tag.getInteger("Count");
			if (count > 0) {
				ItemStack is = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Prototype"));
				if (is == null) {
					list.removeTag(i);
					continue;
				}
				is.stackSize = count;
				rtrn.add(is);
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
		NBTTagCompound prototype = createPrototype(item);
		int idx = findDataIndexForPrototype(drive, prototype);
		if (idx == -1) {
			NBTTagCompound data = new NBTTagCompound();
			data.setTag("Prototype", prototype);
			data.setInteger("Count", count);
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

}
