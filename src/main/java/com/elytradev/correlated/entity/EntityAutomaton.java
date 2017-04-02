package com.elytradev.correlated.entity;

import java.util.AbstractList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elytradev.correlated.Correlated;
import com.elytradev.correlated.entity.ai.EntityAIAutomatonAttackRangedBow;
import com.elytradev.correlated.entity.ai.EntityAIAutomatonFollowOwner;
import com.elytradev.correlated.entity.ai.EntityAIAutomatonOwnerHurtByTarget;
import com.elytradev.correlated.entity.ai.EntityAIAutomatonOwnerHurtTarget;
import com.elytradev.correlated.init.CItems;
import com.elytradev.correlated.init.CRecords;
import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.item.ItemDrive;
import com.elytradev.correlated.item.ItemModule;
import com.elytradev.correlated.item.ItemDrive.Priority;
import com.elytradev.correlated.storage.IDigitalStorage;
import com.elytradev.correlated.storage.ITerminal;
import com.elytradev.correlated.storage.InsertResult;
import com.elytradev.correlated.storage.SimpleUserPreferences;
import com.elytradev.correlated.storage.UserPreferences;
import com.elytradev.correlated.storage.InsertResult.Result;
import com.google.common.base.Optional;
import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityAutomaton extends EntityCreature implements IEntityOwnable, ITerminal, IDigitalStorage {
	public enum AutomatonStatus {
		WANDER,
		ATTACK,
		FOLLOW,
		STAY,
		EXEC;
		public static final AutomatonStatus[] VALUES = values();
		public final ResourceLocation texture;
		private AutomatonStatus() {
			texture = new ResourceLocation("correlated", "textures/entity/automaton_status_"+name().toLowerCase(Locale.ROOT)+".png");
		}
	}
	private static final DataParameter<Boolean> ANGRY = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> MUTED = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> FOLLOW_DISTANCE = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.BYTE);
	private static final DataParameter<ItemStack> MODULE1 = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private static final DataParameter<ItemStack> MODULE2 = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private static final DataParameter<ItemStack> MODULE3 = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private static final DataParameter<ItemStack> MODULE4 = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private static final DataParameter<ItemStack> MODULE5 = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private static final DataParameter<ItemStack> MODULE6 = EntityDataManager.createKey(EntityAutomaton.class, DataSerializers.OPTIONAL_ITEM_STACK);
	
	private final TObjectIntMap<UUID> favor = new TObjectIntHashMap<>();
	
	private AutomatonStatus aiStatus;
	
	public EntityAutomaton(World worldIn) {
		super(worldIn);
		setSize(0.5f, 0.5f);
		inventoryArmorDropChances[1] = 1;
		inventoryArmorDropChances[2] = 1;
	}
	
	private void clearAI() {
		clearTaskList(tasks);
		clearTaskList(targetTasks);
	}

	private void clearTaskList(EntityAITasks tasks) {
		for (EntityAITaskEntry eate : tasks.taskEntries.toArray(new EntityAITaskEntry[tasks.taskEntries.size()])) {
			tasks.removeTask(eate.action);
		}
	}
	
	@Override
	protected void initEntityAI() {
		if (getHealth() >= 1) {
			tasks.addTask(0, new EntityAISwimming(this));
			tasks.addTask(6, new EntityAILookIdle(this));
			ItemStack mainhand = getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
			if (mainhand != null && mainhand.getItem() == Items.BOW) {
				tasks.addTask(14, new EntityAIAutomatonAttackRangedBow(this, 0.75, 20, 15));
			} else {
				tasks.addTask(14, new EntityAIAttackMelee(this, 0.75, false));
			}
			aiStatus = getStatus();
			switch (getStatus()) {
				case ATTACK:
					targetTasks.addTask(14, new EntityAIAutomatonOwnerHurtByTarget(this));
					targetTasks.addTask(12, new EntityAIAutomatonOwnerHurtTarget(this));
					tasks.addTask(7, new EntityAIAutomatonFollowOwner(this, 0.75, (getFollowDistance()+1)*6, 128f));
					stepHeight = 1;
					break;
				case EXEC:
					break;
				case FOLLOW:
					tasks.addTask(4, new EntityAIAutomatonFollowOwner(this, 0.75, (getFollowDistance()+1)*4, 32f));
					stepHeight = 1;
					break;
				case WANDER:
					tasks.addTask(4, new EntityAIWander(this, 0.5));
					// fall through
				case STAY:
					stepHeight = 0.6f;
					tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0f));
					tasks.addTask(10, new EntityAIWatchClosest(this, EntityAutomaton.class, 4.0f));
					break;
				default:
					break;
				
			}
		}
	}
	
	public void attackEntityWithRangedAttack(EntityLivingBase target, float p_82196_2_) {
		EntityArrow entityarrow = new EntityTippedArrow(this.world, this);
		if (EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, getItemStackFromSlot(EntityEquipmentSlot.MAINHAND)) <= 0) {
			ItemStack ammo = removeItemsFromNetwork(new ItemStack(Items.ARROW), 1, false);
			if (ammo == null) return;
			entityarrow.pickupStatus = PickupStatus.ALLOWED;
		} else {
			entityarrow.pickupStatus = PickupStatus.CREATIVE_ONLY;
		}
		double d0 = target.posX - this.posX;
		double d1 = target.getEntityBoundingBox().minY + target.height / 3.0F - entityarrow.posY;
		double d2 = target.posZ - this.posZ;
		double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
		entityarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 14 - this.world.getDifficulty().getDifficultyId() * 4);
		int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, this);
		int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
		entityarrow.setDamage(p_82196_2_ * 2.0F + this.rand.nextGaussian() * 0.25D + this.world.getDifficulty().getDifficultyId() * 0.11F);

		if (i > 0) {
			entityarrow.setDamage(entityarrow.getDamage() + i * 0.5D + 0.5D);
		}

		if (j > 0) {
			entityarrow.setKnockbackStrength(j);
		}

		if (EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, this) > 0) {
			entityarrow.setFire(100);
		}

		this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.world.spawnEntity(entityarrow);
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(ANGRY, false);
		dataManager.register(MUTED, false);
		dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
		dataManager.register(STATUS, (byte)0);
		dataManager.register(FOLLOW_DISTANCE, (byte)3);
		dataManager.register(MODULE1, ItemStack.EMPTY);
		dataManager.register(MODULE2, ItemStack.EMPTY);
		dataManager.register(MODULE3, ItemStack.EMPTY);
		dataManager.register(MODULE4, ItemStack.EMPTY);
		dataManager.register(MODULE5, ItemStack.EMPTY);
		dataManager.register(MODULE6, ItemStack.EMPTY);
	}
	
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
		setCanPickUpLoot(rand.nextFloat() < 0.75F * difficulty.getClampedAdditionalDifficulty());
		setHealth((rand.nextFloat()*(getMaxHealth()-4))+2);
		setEquipmentBasedOnDifficulty(difficulty);
		setEnchantmentBasedOnDifficulty(difficulty);
		return super.onInitialSpawn(difficulty, livingdata);
	}
	
	@Override
	protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
		ItemStack res = addItemToNetwork(itemEntity.getEntityItem()).stack;
		if (res.isEmpty()) {
			itemEntity.setDead();
		} else {
			itemEntity.setEntityItemStack(res);
		}
	}
	
	@Override
	protected float getSoundVolume() {
		return 0.25f;
	}
	
	@Override
	public int getTalkInterval() {
		return 160;
	}
	
	@Override
	protected SoundEvent getAmbientSound() {
		return getHealth() >= 1 && !isMuted() ? CSoundEvents.AUTOMATON_IDLE : null;
	}
	
	@Override
	protected SoundEvent getHurtSound() {
		return CSoundEvents.AUTOMATON_HURT;
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}
	
	@Override
	public void onKillCommand() {
		setDead();
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		Correlated.proxy.smokeTick(this);
		if (aiStatus != getStatus() && tasks != null) {
			clearAI();
			initEntityAI();
		}
		boolean isAttacking = (getAttackTarget() != null);
		if (isAngry() != isAttacking) {
			setAngry(isAttacking);
		}
		if (isTamed() && getHealth() < getMaxHealth()/2 && ticksExisted % 160 == 0) {
			adjustFavor(getOwner(), -1);
		}
		if (getAttackTarget() instanceof EntityAutomaton && getAttackTarget().getHealth() < 1) {
			setAttackTarget(null);
		}
		if (isTamed()
				&& getAttackTarget() instanceof IEntityOwnable
				&& getOwnerId().equals(((IEntityOwnable)getAttackTarget()).getOwnerId())) {
			setAttackTarget(null);
		}
		dead = (getHealth() < 1);
		if (posY < -64) {
			setDead();
		}
	}
	
	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		if (rand.nextFloat() < 0.35f * difficulty.getClampedAdditionalDifficulty()) {
			int tier = rand.nextInt(2);

			for (int i = 0; i < 3; i++) {
				if (rand.nextFloat() < 0.095f) {
					tier++;
				}
			}
			
			Item i = getArmorByChance(EntityEquipmentSlot.HEAD, tier);
			if (i != null) {
				ItemStack stack = new ItemStack(i);
				if (i == Items.LEATHER_HELMET) {
					Items.LEATHER_HELMET.setColor(stack, rand.nextInt());
				}
				setItemStackToSlot(EntityEquipmentSlot.HEAD, stack);
			}
		}
		boolean has64k = false;
		List<ItemStack> drives = Lists.newArrayList();
		if (rand.nextInt(8) == 0) {
			ItemStack voidDrive = new ItemStack(CItems.DRIVE, 1, 4);
			CItems.DRIVE.setPriority(voidDrive, Priority.LOW);
			drives.add(voidDrive);
		}
		if (rand.nextInt(16) == 0) {
			drives.add(new ItemStack(CItems.DRIVE, 1, 3));
			has64k = true;
		}
		if (drives.size() < 2 && rand.nextInt(4) == 0) {
			int tier = rand.nextInt(2);
			if (rand.nextFloat() < 0.15f) {
				tier++;
			}
			drives.add(new ItemStack(CItems.DRIVE, 1, tier));
		}
		
		if (drives.size() >= 1) {
			setItemStackToSlot(EntityEquipmentSlot.LEGS, drives.get(0));
		}
		if (drives.size() == 2) {
			setItemStackToSlot(EntityEquipmentSlot.CHEST, drives.get(1));
		}
		
		if (getKilobitsStorageFree() > 0 && rand.nextFloat() < 0.8f * difficulty.getClampedAdditionalDifficulty()) {
			setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
			addItemToNetwork(new ItemStack(Items.ARROW, rand.nextInt(192)+1));
		} else if (rand.nextFloat() < 0.25f * difficulty.getClampedAdditionalDifficulty()) {
			int weapon = rand.nextInt(4);
			switch (weapon) {
				case 0:
					setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
					break;
				case 1:
					setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
					break;
				case 2:
					setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_HOE));
					break;
				case 3:
					int color = rand.nextInt(16);
					setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BANNER, 1, color));
					setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.BANNER, 1, color));
					break;
			}
		}
		
		if (has64k) {
			addItemToNetwork(new ItemStack(Blocks.COBBLESTONE, rand.nextInt(6400)+256));
			addItemToNetwork(new ItemStack(Items.COAL, rand.nextInt(128)));
			addItemToNetwork(new ItemStack(Blocks.IRON_ORE, rand.nextInt(96)));
			addItemToNetwork(new ItemStack(Blocks.GOLD_ORE, rand.nextInt(32)));
			if (rand.nextInt(24) == 0) {
				addItemToNetwork(new ItemStack(Items.DIAMOND, rand.nextInt(12)+2));			 
			}
			if (rand.nextInt(12) == 0) {
				addItemToNetwork(new ItemStack(Items.DYE, rand.nextInt(48)+24, EnumDyeColor.BLUE.getDyeDamage()));
			}
		}
		
		if (getKilobitsStorageFree() > 0) {
			for (int i = 0; i < rand.nextInt(64); i++) {
				switch (rand.nextInt(3)) {
					case 0:
						addItemToNetwork(new ItemStack(CRecords.RECORD_ITEMS.get(rand.nextInt(CRecords.RECORD_ITEMS.size()))));
						break;
					case 1:
						addItemToNetwork(new ItemStack(Items.COOKIE, rand.nextInt(24)+1));
						break;
					case 2:
						addItemToNetwork(new ItemStack(CItems.MISC, rand.nextInt(18)+1, 3));
						break;
				}
			}
		}
	}
	
	public AutomatonStatus getStatus() {
		return AutomatonStatus.VALUES[dataManager.get(STATUS)%AutomatonStatus.VALUES.length];
	}
	
	public void setStatus(AutomatonStatus status) {
		dataManager.set(STATUS, (byte)status.ordinal());
		if (tasks != null) {
			clearAI();
			initEntityAI();
		}
	}
	
	public void setMuted(boolean muted) {
		dataManager.set(MUTED, muted);
	}
	
	public boolean isMuted() {
		return hasModule("speech") || dataManager.get(MUTED);
	}
	
	public boolean hasModule(String module) {
		for (ItemStack is : getModules()) {
			if (is != null && is.getItem() instanceof ItemModule) {
				if (module.equals(((ItemModule)is.getItem()).getType(is))) {
					return true;
				}
			}
		}
		return false;
	}

	public ItemStack getModule(int slot) {
		if (slot < 0 || slot >= 6) throw new IndexOutOfBoundsException(slot+" not within 0-5");
		switch (slot) {
			case 0:
				return dataManager.get(MODULE1);
			case 1:
				return dataManager.get(MODULE2);
			case 2:
				return dataManager.get(MODULE3);
			case 3:
				return dataManager.get(MODULE4);
			case 4:
				return dataManager.get(MODULE5);
			case 5:
				return dataManager.get(MODULE6);
		}
		return null;
	}
	
	public void setModule(int slot, ItemStack stack) {
		if (slot < 0 || slot >= 6) throw new IndexOutOfBoundsException(slot+" not within 0-5");
		switch (slot) {
			case 0:
				dataManager.set(MODULE1, stack);
				break;
			case 1:
				dataManager.set(MODULE2, stack);
				break;
			case 2:
				dataManager.set(MODULE3, stack);
				break;
			case 3:
				dataManager.set(MODULE4, stack);
				break;
			case 4:
				dataManager.set(MODULE5, stack);
				break;
			case 5:
				dataManager.set(MODULE6, stack);
				break;
		}
	}
	
	public Iterable<ItemStack> getModules() {
		return new AbstractList<ItemStack>() {

			@Override
			public ItemStack get(int index) {
				return getModule(index);
			}

			@Override
			public int size() {
				return 6;
			}
			
		};
	}
	
	public void setFollowDistance(int distance) {
		dataManager.set(FOLLOW_DISTANCE, (byte)distance);
		if (tasks != null) {
			clearAI();
			initEntityAI();
		}
	}
	
	public int getFollowDistance() {
		return dataManager.get(FOLLOW_DISTANCE).intValue();
	}
	
	@Override
	public int getMaxFallHeight() {
		return 512;
	}
	
	@Override
	public void setRevengeTarget(EntityLivingBase livingBase) {
		super.setRevengeTarget(livingBase);
		if (!isOwner(livingBase)) {
			setAttackTarget(livingBase);
			for (EntityAutomaton friend : world.getEntitiesWithinAABB(EntityAutomaton.class, getEntityBoundingBox().expandXyz(12))) {
				if (friend == this) continue;
				friend.adjustFavor(livingBase, -1);
			}
		} else {
			adjustFavor(livingBase, -1);
		}
	}
	
	public int adjustFavor(Entity ent, int i) {
		if (ent instanceof EntityPlayer) {
			UUID id = ((EntityPlayer)ent).getGameProfile().getId();
			if (i == 0) return favor.get(id);
			int f = favor.adjustOrPutValue(id, i, i);
			boolean tamed = false;
			if (f >= 16 && getOwnerId() == null) {
				setOwnerId(id);
				setStatus(AutomatonStatus.FOLLOW);
				tamed = true;
			}
			if (world instanceof WorldServer) {
				EnumParticleTypes particle;
				if (tamed) {
					particle = EnumParticleTypes.HEART;
				} else if (i < 0) {
					particle = EnumParticleTypes.VILLAGER_ANGRY;
				} else {
					particle = EnumParticleTypes.VILLAGER_HAPPY;
				}
				((WorldServer)world).spawnParticle(particle, posX, posY, posZ, 4, 0.25f, 0.25f, 0.25f, 0);
			}
			return f;
		}
		return 0;
	}
	
	public int getFavor(Entity ent) {
		return adjustFavor(ent, 0);
	}

	@Override
	public void setHealth(float health) {
		super.setHealth(health);
		if (tasks != null && tasks.taskEntries.isEmpty()) {
			clearAI();
			initEntityAI();
		}
	}
	
	@Override
	public int getTotalArmorValue() {
		return super.getTotalArmorValue() + 6;
	}
	
	@Override
	public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
		super.knockBack(entityIn, strength*2, xRatio, zRatio);
	}
	
	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		isDead = false;
		setHealth(0.1f);
		if (tasks != null) {
			clearAI();
			initEntityAI();
		}
		if (cause instanceof EntityDamageSource) {
			for (EntityAutomaton friend : world.getEntitiesWithinAABB(EntityAutomaton.class, getEntityBoundingBox().expandXyz(12))) {
				friend.adjustFavor(cause.getEntity(), -4);
			}
		}
	}
	
	@Override
	protected void damageEntity(DamageSource cause, float damageAmount) {
		super.damageEntity(cause, damageAmount);
		if (cause instanceof EntityDamageSource) {
			for (EntityAutomaton friend : world.getEntitiesWithinAABB(EntityAutomaton.class, getEntityBoundingBox().expandXyz(12))) {
				friend.adjustFavor(cause.getEntity(), (int)(-(damageAmount*5)));
			}
		}
	}
	
	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));

		if (flag) {
			applyEnchantments(this, entityIn);
		}

		return flag;
	}
	
	
	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == CItems.WELDTHROWER) return EnumActionResult.PASS;
		if (stack.getItem() == CItems.MISC && stack.getMetadata() == 5) {
			if (!world.isRemote) {
				if (!player.capabilities.isCreativeMode) {
					stack.setCount(stack.getCount()-1);;
				}
				adjustFavor(player, 1);
			}
			return EnumActionResult.SUCCESS;
		}
		if (!world.isRemote) {
			if (getHealth() < 1) {
				player.sendMessage(new TextComponentTranslation("msg.correlated.automaton_dead"));
			} else if (!isOwner(player)) {
				if (getOwnerId() == null) {
					player.sendMessage(new TextComponentTranslation("msg.correlated.automaton_untamed"));
				} else if (isAngry()) {
					player.sendMessage(new TextComponentTranslation("msg.correlated.automaton_angry"));
				} else {
					player.sendMessage(new TextComponentTranslation("msg.correlated.automaton_not_yours"));
				}
			} else {
				if (player.isSneaking()) {
					EntityEquipmentSlot slot;
					if (!getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()) {
						slot = EntityEquipmentSlot.CHEST;
					} else if (!getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty()) {
						slot = EntityEquipmentSlot.LEGS;
					} else {
						slot = null;
						player.openGui(Correlated.inst, 4, world, getEntityId(), 0, 0);
					}
					if (slot != null) {
						entityDropItem(getItemStackFromSlot(slot), 0.2f);
						setItemStackToSlot(slot, ItemStack.EMPTY);
					}
				} else if (stack != null && stack.getItem() instanceof ItemDrive) {
					EntityEquipmentSlot slot;
					if (getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty()) {
						slot = EntityEquipmentSlot.LEGS;
					} else if (getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()) {
						slot = EntityEquipmentSlot.CHEST;
					} else {
						slot = null;
						player.openGui(Correlated.inst, 4, world, getEntityId(), 0, 0);
					}
					if (slot != null) {
						setItemStackToSlot(slot, stack.splitStack(1));
					}
				} else {
					player.openGui(Correlated.inst, 4, world, getEntityId(), 0, 0);
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
		super.setItemStackToSlot(slotIn, stack);
		if (slotIn == EntityEquipmentSlot.LEGS || slotIn == EntityEquipmentSlot.CHEST) {
			updateSlotOrder();
		} else {
			playEquipSound(stack);
			if (slotIn == EntityEquipmentSlot.MAINHAND) {
				if (tasks != null) {
					clearAI();
					initEntityAI();
				}
			}
		}
	}
	
	private void updateSlotOrder() {
		ItemStack legs = getItemStackFromSlot(EntityEquipmentSlot.LEGS);
		ItemStack chest = getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		boolean hasLegs = (!legs.isEmpty());
		boolean hasChest = (!chest.isEmpty());
		if (!hasLegs && !hasChest) {
			slots = new EntityEquipmentSlot[0];
		} else if (!hasLegs && hasChest) {
			slots = new EntityEquipmentSlot[] { EntityEquipmentSlot.CHEST };
		} else if (hasLegs && !hasChest) {
			slots = new EntityEquipmentSlot[] { EntityEquipmentSlot.LEGS };
		} else {
			if (getPriority(chest) > getPriority(legs)) {
				slots = new EntityEquipmentSlot[] { EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST };
			} else {
				slots = new EntityEquipmentSlot[] { EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS };
			}
		}
	}

	private int getPriority(ItemStack stack) {
		if (stack.getItem() instanceof ItemDrive) {
			return ((ItemDrive)stack.getItem()).getPriority(stack).ordinal();
		}
		return Priority.DEFAULT.ordinal();
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		if (source.isFireDamage()) return true;
		if (source == DamageSource.FALL) return true;
		return getHealth() < 1 || super.isEntityInvulnerable(source);
	}
	
	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}
	
	@Override
	protected boolean canDropLoot() {
		return false;
	}
	
	public boolean isAngry() {
		return dataManager.get(ANGRY);
	}
	
	public void setAngry(boolean angry) {
		dataManager.set(ANGRY, angry);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);

		if (this.getOwnerId() != null) {
			compound.setUniqueId("Owner", getOwnerId());
		}

		compound.setBoolean("Angry", isAngry());
		compound.setByte("Status", dataManager.get(STATUS));
		compound.setBoolean("Muted", isMuted());
		compound.setByte("FollowDistance", dataManager.get(FOLLOW_DISTANCE));
		
		NBTTagList li = new NBTTagList();
		favor.forEachEntry((id, f) -> {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setUniqueId("Key", id);
			tag.setInteger("Value", f);
			li.appendTag(tag);
			return true;
		});
		compound.setTag("Favor", li);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);

		if (compound.hasUniqueId("Owner")) {
			setOwnerId(compound.getUniqueId("Owner"));
		} else {
			setOwnerId(null);
		}

		setAngry(compound.getBoolean("Angry"));
		dataManager.set(STATUS, compound.getByte("Status"));
		setMuted(compound.getBoolean("Muted"));
		setFollowDistance(compound.getInteger("FollowDistance"));
		
		favor.clear();
		NBTTagList li = compound.getTagList("Favor", NBT.TAG_COMPOUND);
		for (int i = 0; i < li.tagCount(); i++) {
			NBTTagCompound tag = li.getCompoundTagAt(i);
			favor.put(tag.getUniqueId("Key"), tag.getInteger("Value"));
		}
		
		updateSlotOrder();
	}
	
	@Override
	@Nullable
	public UUID getOwnerId() {
		return dataManager.get(OWNER_UNIQUE_ID).orNull();
	}

	public void setOwnerId(@Nullable UUID uuid) {
		dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(uuid));
	}

	@Override
	@Nullable
	public EntityLivingBase getOwner() {
		try {
			UUID uuid = getOwnerId();
			return uuid == null ? null : world.getPlayerEntityByUUID(uuid);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	public boolean isOwner(EntityLivingBase entityIn) {
		return entityIn == getOwner();
	}

	public boolean isTamed() {
		return getOwnerId() != null;
	}
	
	@Override
	public String getName() {
		return "entity.correlated.automaton.name";
	}

	private EntityEquipmentSlot[] slots = {};
	private int changeId = 0;
	
	@Override
	public int getChangeId() {
		return changeId;
	}

	@Override
	public void getTypes(Set<IDigitalStorage> alreadyChecked, List<ItemStack> li) {
		for (EntityEquipmentSlot slot : slots) {
			ItemStack drive = getItemStackFromSlot(slot);
			if (drive.getItem() instanceof ItemDrive) {
				ItemDrive itemDrive = (ItemDrive)drive.getItem();
				li.addAll(itemDrive.getTypes(drive));
			}
		}
	}

	@Override
	public int getKilobitsStorageFree(Set<IDigitalStorage> alreadyChecked) {
		int accum = 0;
		for (EntityEquipmentSlot slot : slots) {
			if (getItemStackFromSlot(slot) != null) {
				ItemStack drive = getItemStackFromSlot(slot);
				if (drive.getItem() instanceof ItemDrive) {
					accum += ((ItemDrive)drive.getItem()).getKilobitsFree(drive);
				}
			}
		}
		return accum;
	}
	
	@Override
	public InsertResult addItemToNetwork(ItemStack stack, Set<IDigitalStorage> alreadyChecked) {
		if (stack.isEmpty()) return InsertResult.success(stack);
		Multiset<Result> results = EnumMultiset.create(Result.class);
		for (EntityEquipmentSlot slot : slots) {
			if (stack.isEmpty()) break;
			ItemStack drive = getItemStackFromSlot(slot);
			if (drive.getItem() instanceof ItemDrive) {
				ItemDrive itemDrive = (ItemDrive)drive.getItem();
				InsertResult ir = itemDrive.addItem(drive, stack, false);
				results.add(ir.result);
				stack = ir.stack;
			}
		}
		changeId++;
		if (!results.contains(Result.SUCCESS) && !results.contains(Result.SUCCESS_VOIDED) && results.size() > 0) {
			Result result = null;
			int num = 0;
			for (Multiset.Entry<Result> en : results.entrySet()) {
				if (en.getCount() > num) {
					result = en.getElement();
					num = en.getCount();
				}
			}
			return new InsertResult(result, stack);
		}
		return stack.isEmpty() ? InsertResult.success(stack) : InsertResult.insufficientStorage(stack);
	}

	@Override
	public ItemStack removeItemsFromNetwork(ItemStack prototype, int amount, boolean b, Set<IDigitalStorage> alreadyChecked) {
		if (prototype.isEmpty()) return ItemStack.EMPTY;
		ItemStack stack = prototype.copy();
		stack.setCount(0);
		for (EntityEquipmentSlot slot : slots) {
			if (getItemStackFromSlot(slot) != null) {
				ItemStack drive = getItemStackFromSlot(slot);
				if (drive.getItem() instanceof ItemDrive) {
					ItemDrive itemDrive = (ItemDrive)drive.getItem();
					int amountWanted = amount-stack.getCount();
					itemDrive.removeItems(drive, prototype, amountWanted);
					if (stack.getCount() >= amount) break;
				}
			}
		}
		changeId++;
		return stack;
	}

	@Override
	public boolean isPowered() {
		return getHealth() >= 1;
	}

	@Override
	public UserPreferences getPreferences(EntityPlayer player) {
		// TODO save preferences
		return new SimpleUserPreferences();
	}

	@Override
	public IDigitalStorage getStorage() {
		return this;
	}

	@Override
	public boolean hasStorage() {
		return true;
	}

	@Override
	public ItemStack getMaintenanceSlotContent() {
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean hasMaintenanceSlot() {
		return false;
	}
	
	@Override
	public void setMaintenanceSlotContent(ItemStack stack) {
		
	}

	@Override
	public boolean canContinueInteracting(EntityPlayer player) {
		return hasStorage() && getStorage().isPowered() && player.getDistanceSqToEntity(this) < 8*8;
	}

	@Override
	public void markUnderlyingStorageDirty() {
		// entities are always dirty
	}
	
	// TODO implement wireless
	
	@Override
	public int getSignalStrength() {
		return -1;
	}
	
	@Override
	public void setAPN(String apn) {
		
	}
	
	@Override
	public String getAPN() {
		return null;
	}

}
