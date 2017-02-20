package com.elytradev.correlated.item;

import java.util.List;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.entity.EntityThrownItem;
import com.elytradev.correlated.helper.Numbers;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public class ItemMisc extends Item {
	public static final String[] items = {
			"processor",
			"ceramic_drive_platter",
			"metallic_drive_platter",
			"luminous_pearl",
			"lumtorch",
			"weldthrower_fuel",
			"unstable_pearl",
			"unfinished_organic_circuit",
			"data_core",
			"logo"
		};

	public ItemMisc() {
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, (src, stack) -> {
			if (stack.getMetadata() == 3) {
				src.getWorld().playSound((EntityPlayer) null, src.getX(), src.getY(), src.getZ(), SoundEvents.ENTITY_ENDERPEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
				EnumFacing facing = src.getBlockState().getValue(BlockDispenser.FACING);
				EntityThrownItem e = new EntityThrownItem(src.getWorld(), src.getX()+facing.getFrontOffsetX(), src.getY()+facing.getFrontOffsetY(), src.getZ()+facing.getFrontOffsetZ());
				ItemStack copy = stack.splitStack(1);
				e.setStack(copy);
				e.setThrowableHeading(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ(), 0.5f, 1.0f);
				src.getWorld().spawnEntity(e);
				return stack;
			} else {
				return BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Items.APPLE).dispense(src, stack);
			}
		});
	}
	
	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + getItemName(stack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		int i = 0;
		while (I18n.canTranslate("tooltip.correlated.misc." + getItemName(stack) + "." + i)) {
			tooltip.add(I18n.translateToLocal("tooltip.correlated.misc." + getItemName(stack) + "." + i));
			i++;
		}
		if (stack.getMetadata() == 8) {
			int bytesUsed = (Correlated.drive.getKilobitsUsed(stack) / 8)*1024;

			tooltip.add(I18n.translateToLocalFormatted("tooltip.correlated.bytes_contained", Numbers.humanReadableBytes(bytesUsed)));
		}
	}

	private String getItemName(ItemStack stack) {
		return stack.getMetadata() < 0 || stack.getMetadata() >= items.length ? "missingno." : items[stack.getMetadata()];
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < items.length; i++) {
			if (i == 9) continue;
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (!(playerIn instanceof FakePlayer) && playerIn.dimension != Correlated.limboDimId && (itemStackIn.getMetadata() == 3 || itemStackIn.getMetadata() == 6 || itemStackIn.getMetadata() == 8)) {
			if (!playerIn.capabilities.isCreativeMode) {
				itemStackIn.setCount(itemStackIn.getCount()-1);
			}

			worldIn.playSound((EntityPlayer) null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

			if (!worldIn.isRemote) {
				EntityThrownItem e = new EntityThrownItem(worldIn, playerIn);
				ItemStack copy = itemStackIn.copy();
				copy.setCount(1);
				e.setStack(copy);
				e.setHeadingFromThrower(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 0.9F, 1.0F);
				worldIn.spawnEntity(e);
			}

			playerIn.addStat(StatList.getObjectUseStats(this));
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
		} else if (itemStackIn.getMetadata() == 7) {
			if (playerIn.canEat(false)) {
				playerIn.setActiveHand(hand);
				return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
			} else {
				return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
			}
		}
		return super.onItemRightClick(worldIn, playerIn, hand);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String raw = I18n.translateToLocal(this.getUnlocalizedName(stack) + ".name");
		if (stack.getMetadata() != 6) return raw;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < raw.length(); i++) {
			if (Math.random() < 0.2) {
				builder.append("\u00A7k");
				builder.append(raw.charAt(i));
				builder.append("\u00A7r");
			} else {
				builder.append(raw.charAt(i));
			}
		}
		return builder.toString();
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		stack.setCount(stack.getCount()-1);

		if (entityLiving instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer) entityLiving;
			entityplayer.getFoodStats().addStats(2, 4);
			worldIn.playSound((EntityPlayer) null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
			entityplayer.addStat(StatList.getObjectUseStats(this));
		}

		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 48;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return stack.getMetadata() == 7 ? EnumAction.EAT : EnumAction.NONE;
	}
}
