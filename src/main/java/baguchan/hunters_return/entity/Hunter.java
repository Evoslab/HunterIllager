package baguchan.hunters_return.entity;

import bagu_chan.bagus_lib.entity.goal.AnimateAttackGoal;
import baguchan.hunters_return.HunterConfig;
import baguchan.hunters_return.entity.ai.*;
import baguchan.hunters_return.entity.projectile.BoomerangEntity;
import baguchan.hunters_return.init.HunterEnchantments;
import baguchan.hunters_return.init.HunterItems;
import baguchan.hunters_return.init.HunterSounds;
import baguchan.hunters_return.item.MiniCrossBowItem;
import baguchan.hunters_return.utils.HunterConfigUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Hunter extends AbstractIllager implements CrossbowAttackMob, RangedAttackMob {
	private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.BOOLEAN);

	private static final EntityDataAccessor<String> HUNTER_TYPE = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.STRING);
	public static final Predicate<LivingEntity> TARGET_ENTITY_SELECTOR = (p_213616_0_) -> {
		return !p_213616_0_.isBaby() && HunterConfigUtils.isWhitelistedEntity(p_213616_0_.getType());
	};
	private static final Predicate<? super ItemEntity> ALLOWED_ITEMS = (p_213616_0_) -> {
        return HunterConfigUtils.isWhitelistedItem(p_213616_0_.getItem().getItem());
	};

	private final SimpleContainer inventory = new SimpleContainer(5);

	@Nullable
	private BlockPos homeTarget;
	private int cooldown;

	private final int attackAnimationLength = (int) (18);
	private final int shootAnimationLength = 20;
	private final int attackAnimationActionPoint = (int) (0.4 * 20 * 0.75F);
	private int attackAnimationTick;
	private int shootAnimationTick;
	private int thrownAnimationTick;
	public final AnimationState attackAnimationState = new AnimationState();
	public final AnimationState shootAnimationState = new AnimationState();
	public final AnimationState chargeAnimationState = new AnimationState();
	public final AnimationState thrownAnimationState = new AnimationState();

	public Hunter(EntityType<? extends Hunter> p_i48556_1_, Level p_i48556_2_) {
		super(p_i48556_1_, p_i48556_2_);
		((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
		this.moveControl = new DodgeMoveControl(this);
		this.setCanPickUpLoot(true);
	}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_326255_) {
        super.defineSynchedData(p_326255_);
        p_326255_.define(HUNTER_TYPE, HunterType.NORMAL.name());
		p_326255_.define(IS_CHARGING_CROSSBOW, false);
	}

	public void setHunterType(HunterType type) {
		this.entityData.set(HUNTER_TYPE, type.name());
	}

	public HunterType getHunterType() {
		return HunterType.get(this.entityData.get(HUNTER_TYPE));
	}

	@Override
	public boolean canFreeze() {
		if (this.getHunterType() == HunterType.COLD) {
			return false;
		}
		return super.canFreeze();
	}

	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new WakeUpGoal(this));
		this.goalSelector.addGoal(0, new DoSleepingGoal(this));
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(0, new CallAllyGoal(this));
		this.goalSelector.addGoal(0, new DodgeGoal(this, Projectile.class));
		this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
		this.goalSelector.addGoal(2, new AbstractIllager.RaiderOpenDoorGoal(this));
		this.goalSelector.addGoal(3, new Raider.HoldGroundAttackGoal(this, 10.0F));
		this.goalSelector.addGoal(4, new MiniCrossBowAttackGoal<>(this, 1.1D, 9.0F));
		this.goalSelector.addGoal(4, new RangedBowAttackGoal<>(this, 1.1F, 50, 16.0F));
		this.goalSelector.addGoal(4, new BoomeranAttackGoal(this, 50, 16.0F));
		this.goalSelector.addGoal(4, new AnimateAttackGoal(this, 1.15F, attackAnimationActionPoint, attackAnimationLength) {
			@Override
			public boolean canUse() {
				return !mob.isHolding((item) -> item.getItem() instanceof BowItem || item.getItem() instanceof MiniCrossBowItem) && super.canUse();
			}

			@Override
			public boolean canContinueToUse() {
				return !mob.isHolding((item) -> item.getItem() instanceof BowItem || item.getItem() instanceof MiniCrossBowItem) && super.canContinueToUse();
			}
		});
		this.goalSelector.addGoal(5, new SleepOnBedGoal(this, 1.0F, 12));
		this.goalSelector.addGoal(6, new GetFoodGoal<>(this));
		this.goalSelector.addGoal(7, new MoveToGoal(this, 45.0D, 1.2D));
		this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers(AbstractIllager.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Goat.class, true));
		this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, Animal.class, 10, true, false, TARGET_ENTITY_SELECTOR) {
			@Override
			public boolean canUse() {
				return cooldown <= 0 && super.canUse();
			}
		});
		this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
	}

	@Override
	public void baseTick() {
		super.baseTick();
		if (this.level().isClientSide) {
			if (this.attackAnimationTick < this.attackAnimationLength) {
				this.attackAnimationTick++;
				if (this.thrownAnimationState.isStarted()) {
					this.thrownAnimationState.stop();
				}
			}

			if (this.attackAnimationTick >= this.attackAnimationLength) {
				this.attackAnimationState.stop();
			}

			if (this.shootAnimationTick < this.shootAnimationLength) {
				this.shootAnimationTick++;
			}
			if (this.shootAnimationTick >= this.shootAnimationLength) {
				this.shootAnimationState.stop();
			}

			//Thrown animation same as attack
			if (this.thrownAnimationTick < this.attackAnimationLength) {
				this.thrownAnimationTick++;
			}
			if (this.thrownAnimationTick >= this.attackAnimationLength) {
				this.thrownAnimationState.stop();
			}


			if (this.isHolding(is -> is.getItem() instanceof BowItem) && this.isAggressive() && this.shootAnimationTick >= this.shootAnimationLength) {
				if (!this.chargeAnimationState.isStarted()) {
					this.chargeAnimationState.start(this.tickCount);
				}
			} else {
				this.chargeAnimationState.stop();
			}
		}
	}

	public void handleEntityEvent(byte p_219360_) {
		if (p_219360_ == 4) {
			this.attackAnimationTick = 0;
			this.attackAnimationState.start(this.tickCount);
		} else if (p_219360_ == 61) {
			this.shootAnimationTick = 0;
			this.shootAnimationState.start(this.tickCount);
		} else if (p_219360_ == 62) {
			this.thrownAnimationTick = 0;
			this.thrownAnimationState.start(this.tickCount);
		} else {
			super.handleEntityEvent(p_219360_);
		}

	}

	@Override
    public ItemStack eat(Level p_347678_, ItemStack p_347507_, FoodProperties p_347707_) {
        if (p_347707_ != null) {
            this.heal(p_347707_.nutrition());
		}
        return super.eat(p_347678_, p_347507_, p_347707_);
	}

	public void aiStep() {
		if (!this.level().isClientSide && this.isAlive()) {
			ItemStack mainhand = this.getItemInHand(InteractionHand.MAIN_HAND);

			if (!this.isUsingItem() && this.getOffhandItem().isEmpty() && (mainhand.getItem() == Items.BOW && this.getTarget() == null || mainhand.getItem() != Items.BOW)) {
				ItemStack stack = ItemStack.EMPTY;

				if (this.getHealth() < this.getMaxHealth() && this.random.nextFloat() < 0.005F) {
					stack = this.findFood();
				} else if (this.getHealth() >= this.getMaxHealth() && this.random.nextFloat() < 0.01F) {
					stack = this.findBoomerang();
				}

				if (!stack.isEmpty()) {
					this.setItemSlot(EquipmentSlot.OFFHAND, stack);
                    if (stack.getFoodProperties(this) != null) {
						this.startUsingItem(InteractionHand.OFF_HAND);
					}
				}
			}
		}

		super.aiStep();
	}

	private ItemStack findFood() {
		for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
			ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty() && itemstack.getFoodProperties(this) != null && HunterConfigUtils.isWhitelistedItem(itemstack.getItem())) {
				return itemstack.split(1);
			}
		}
		return ItemStack.EMPTY;
	}

	private ItemStack findBoomerang() {
		for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
			ItemStack itemstack = this.inventory.getItem(i);
			if (!itemstack.isEmpty() && itemstack.is(HunterItems.BOOMERANG.get())) {
				return itemstack.split(1);
			}
		}
		return ItemStack.EMPTY;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, (double) 0.3F).add(Attributes.FOLLOW_RANGE, 20.0D).add(Attributes.MAX_HEALTH, 26.0D).add(Attributes.ARMOR, 1.0D).add(Attributes.ATTACK_DAMAGE, 3.0D);
	}

	public void addAdditionalSaveData(CompoundTag p_213281_1_) {
		super.addAdditionalSaveData(p_213281_1_);
		if (this.homeTarget != null) {
			p_213281_1_.put("HomeTarget", NbtUtils.writeBlockPos(this.homeTarget));
		}
		ListTag listnbt = new ListTag();

		for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
			ItemStack itemstack = this.inventory.getItem(i);
			if (!itemstack.isEmpty()) {
                listnbt.add(itemstack.save(this.registryAccess(), new CompoundTag()));
			}
		}

		p_213281_1_.put("Inventory", listnbt);

		p_213281_1_.putInt("HuntingCooldown", this.cooldown);
		p_213281_1_.putString("HunterType", getHunterType().name());
	}

	public void readAdditionalSaveData(CompoundTag p_70037_1_) {
		super.readAdditionalSaveData(p_70037_1_);
		if (p_70037_1_.contains("HomeTarget")) {
            this.homeTarget = NbtUtils.readBlockPos(p_70037_1_, "HomeTarget").orElse(null);
		}
		ListTag listnbt = p_70037_1_.getList("Inventory", 10);

		for (int i = 0; i < listnbt.size(); ++i) {
            Optional<ItemStack> itemstack = ItemStack.parse(this.registryAccess(), listnbt.getCompound(i));
            if (itemstack.isPresent() && !itemstack.get().isEmpty()) {
                this.inventory.addItem(itemstack.orElse(null));
			}
		}

		this.cooldown = p_70037_1_.getInt("HuntingCooldown");
		this.setHunterType(HunterType.get(p_70037_1_.getString("HunterType")));
		this.setCanPickUpLoot(true);
	}

	@Override
    public void applyRaidBuffs(ServerLevel p_348605_, int p_37844_, boolean p_37845_) {
		ItemStack itemstack;
		ItemStack offHandStack = new ItemStack(HunterItems.BOOMERANG.get());

		HolderLookup.RegistryLookup<Enchantment> enchantment = p_348605_.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		Raid raid = this.getCurrentRaid();

		int i = 1;
		if (p_37844_ > raid.getNumGroups(Difficulty.NORMAL)) {
			i = 2;
		}

		if (raid.getRaidOmenLevel() < 2 || p_37844_ <= raid.getNumGroups(Difficulty.NORMAL)) {
			itemstack = this.random.nextBoolean() ? new ItemStack(HunterItems.MINI_CROSSBOW.asItem()) : new ItemStack(Items.STONE_SWORD);
		} else {
			itemstack = this.random.nextBoolean() ? new ItemStack(HunterItems.MINI_CROSSBOW.asItem()) : new ItemStack(Items.IRON_SWORD);
		}

		inventory.addItem(new ItemStack(Items.PORKCHOP, 5));

		boolean flag = this.random.nextFloat() <= raid.getEnchantOdds();
		if (flag) {
			if (itemstack.getItem() == HunterItems.MINI_CROSSBOW.asItem()) {
				if (this.random.nextBoolean()) {
					itemstack.enchant(enchantment.getOrThrow(Enchantments.PIERCING), i);
				} else {
					itemstack.enchant(enchantment.getOrThrow(Enchantments.MULTISHOT), 1);
				}
			} else {
				itemstack.enchant(enchantment.getOrThrow(Enchantments.SHARPNESS), i);
			}

			inventory.addItem(new ItemStack(Items.COOKED_BEEF, 2));


			offHandStack.enchant(enchantment.getOrThrow(HunterEnchantments.CUTTING), i);
		}

		if (this.random.nextFloat() < 0.25F && !itemstack.is(HunterItems.MINI_CROSSBOW)) {
			offHandStack.enchant(enchantment.getOrThrow(HunterEnchantments.RETURN), i);

			this.setItemInHand(InteractionHand.OFF_HAND, offHandStack);
		}
		if (itemstack.is(HunterItems.MINI_CROSSBOW)) {
			this.setItemInHand(InteractionHand.OFF_HAND, HunterItems.MINI_CROSSBOW.toStack());
		}

		if (this.random.nextFloat() < 0.25F) {
			this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		}

		if (this.random.nextFloat() < 0.5F) {
			HolderLookup.RegistryLookup<TrimMaterial> registrylookup1 = this.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
			HolderLookup.RegistryLookup<TrimPattern> registrylookup2 = this.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);

			ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
			stack.set(DataComponents.TRIM, new ArmorTrim(registrylookup1.getOrThrow(TrimMaterials.EMERALD), registrylookup2.getOrThrow(TrimPatterns.SENTRY)));
			this.setItemSlot(EquipmentSlot.CHEST, stack);
			this.setDropChance(EquipmentSlot.CHEST, 0.0F);
		}

		this.setItemInHand(InteractionHand.MAIN_HAND, itemstack);

	}


	@Override
	protected void pickUpItem(ItemEntity p_175445_1_) {
		ItemStack itemstack = p_175445_1_.getItem();
		if (itemstack.getItem() instanceof BannerItem) {
			super.pickUpItem(p_175445_1_);
		} else {
			Item item = itemstack.getItem();
			if (this.wantsFood(itemstack)) {
				this.onItemPickup(p_175445_1_);
				this.take(p_175445_1_, itemstack.getCount());
				ItemStack itemstack1 = this.inventory.addItem(itemstack);
				if (itemstack1.isEmpty()) {
					p_175445_1_.discard();
				} else {
					itemstack.setCount(itemstack1.getCount());
				}
			} else if (item == HunterItems.BOOMERANG.get()) {
				this.onItemPickup(p_175445_1_);
				this.take(p_175445_1_, itemstack.getCount());

				ItemStack itemstack1 = this.inventory.addItem(itemstack);
				if (itemstack1.isEmpty()) {
					p_175445_1_.discard();
				} else {
					itemstack.setCount(itemstack1.getCount());
				}
			}
		}

	}

	private boolean wantsFood(ItemStack p_213672_1_) {
        return p_213672_1_.getFoodProperties(this) != null && HunterConfigUtils.isWhitelistedItem(p_213672_1_.getItem());
	}

    @org.jetbrains.annotations.Nullable
	@Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_37856_, DifficultyInstance p_37857_, MobSpawnType p_37858_, @org.jetbrains.annotations.Nullable SpawnGroupData p_37859_) {
		RandomSource randomsource = p_37856_.getRandom();
        SpawnGroupData ilivingentitydata = super.finalizeSpawn(p_37856_, p_37857_, p_37858_, p_37859_);
		((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
		this.setCanPickUpLoot(true);

			if (!HunterConfig.COMMON.foodInInventoryWhitelist.get().isEmpty()) {
				Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(HunterConfig.COMMON.foodInInventoryWhitelist.get().get(this.random.nextInt(HunterConfig.COMMON.foodInInventoryWhitelist.get().size()))));
				if (item != Items.AIR) {
                    this.inventory.addItem(new ItemStack(item, 3 + this.random.nextInt(3)));
                }
            }

		if (p_37856_.getBiome(this.blockPosition()).value().coldEnoughToSnow(this.blockPosition())) {
			this.setHunterType(HunterType.COLD);
		}
		if (p_37858_ == MobSpawnType.STRUCTURE) {
			this.setHomeTarget(this.blockPosition());
		}
		this.populateDefaultEquipmentSlots(randomsource, p_37857_);


        this.populateDefaultEquipmentEnchantments(p_37856_, randomsource, p_37857_);
		return ilivingentitydata;
	}


	protected void dropEquipment() {
		super.dropEquipment();
		if (this.inventory != null) {
			for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
				ItemStack itemstack = this.inventory.getItem(i);
                if (!itemstack.isEmpty() && !EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
					this.spawnAtLocation(itemstack);
				}
			}
		}
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource p_217055_, DifficultyInstance p_217056_) {
		if (this.getCurrentRaid() == null) {
			if (this.getMainHandItem().isEmpty()) {
				if (this.random.nextFloat() < 0.1F) {
					this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
					this.setItemSlot(EquipmentSlot.OFFHAND, createHorn());
				} else if (this.random.nextFloat() < 0.5F) {
					this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(HunterItems.MINI_CROSSBOW.asItem()));
					this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(HunterItems.MINI_CROSSBOW.asItem()));
				} else {
					this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
					if (this.random.nextBoolean()) {
						ItemStack offHandStack = new ItemStack(HunterItems.BOOMERANG.get());
                        this.setItemInHand(InteractionHand.OFF_HAND, offHandStack);
					}
				}
			}
			if (this.random.nextFloat() < 0.25F) {
				this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
			}
			if (this.random.nextFloat() < 0.25F) {
				this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
			}
		}
	}

	public ItemStack createHorn() {
		Optional<Holder.Reference<Instrument>> holderset = BuiltInRegistries.INSTRUMENT.getHolder(Instruments.CALL_GOAT_HORN);
		if (holderset.isPresent()) {
			return InstrumentItem.create(Items.GOAT_HORN, holderset.get());
		}
		return ItemStack.EMPTY;
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return HunterSounds.HUNTER_ILLAGER_CHEER.get();
	}

	protected SoundEvent getAmbientSound() {
		return HunterSounds.HUNTER_ILLAGER_IDLE.get();
	}

	protected SoundEvent getDeathSound() {
		return HunterSounds.HUNTER_ILLAGER_DEATH.get();
	}

	protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
		return HunterSounds.HUNTER_ILLAGER_HURT.get();
	}


	@OnlyIn(Dist.CLIENT)
	public AbstractIllager.IllagerArmPose getArmPose() {
		if (this.isAggressive()) {
			if (this.isChargingCrossbow()) {
				return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
			} else if (this.isHolding(is -> is.getItem() instanceof net.minecraft.world.item.CrossbowItem)) {
				return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
			}
			return this.isHolding(Items.BOW) || this.isHolding(HunterItems.BOOMERANG.get()) ? AbstractIllager.IllagerArmPose.BOW_AND_ARROW : AbstractIllager.IllagerArmPose.ATTACKING;
		} else {
			return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
		}
	}
	@Override
	public boolean killedEntity(ServerLevel p_216988_, LivingEntity p_216989_) {
		this.playSound(HunterSounds.HUNTER_ILLAGER_LAUGH.get(), this.getSoundVolume(), this.getVoicePitch());
		this.cooldown = 300;
		return super.killedEntity(p_216988_, p_216989_);
	}

	public void setHomeTarget(@Nullable BlockPos p_213726_1_) {
		this.homeTarget = p_213726_1_;
	}

	@Nullable
	private BlockPos getHomeTarget() {
		return this.homeTarget;
	}

	@Override
	public void performRangedAttack(LivingEntity p_32141_, float p_32142_) {
		ItemStack weapon = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem));
		ItemStack itemstack1 = this.getProjectile(weapon);
		AbstractArrow abstractarrow = this.getArrow(itemstack1, p_32142_, weapon);
		if (weapon.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem weaponItem)
			abstractarrow = weaponItem.customArrow(abstractarrow, itemstack1, weapon);
		double d0 = p_32141_.getX() - this.getX();
		double d1 = p_32141_.getY(0.3333333333333333) - abstractarrow.getY();
		double d2 = p_32141_.getZ() - this.getZ();
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		abstractarrow.shoot(d0, d1 + d3 * 0.2F, d2, 1.6F, (float) (14 - this.level().getDifficulty().getId() * 4));
		this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
		this.level().addFreshEntity(abstractarrow);
	}
    protected AbstractArrow getArrow(ItemStack p_32156_, float p_32157_, @Nullable ItemStack p_346155_) {
        return ProjectileUtil.getMobArrow(this, p_32156_, p_32157_, p_346155_);
	}

	@Override
	public boolean canFireProjectileWeapon(ProjectileWeaponItem p_32144_) {
		return p_32144_ == Items.BOW || p_32144_ instanceof CrossbowItem;
	}


	public void performBoomerangAttack(LivingEntity p_82196_1_) {
		BoomerangEntity boomerang = new BoomerangEntity(this.level(), this, this.getOffhandItem().split(1));
		double d0 = p_82196_1_.getX() - this.getX();
		double d1 = p_82196_1_.getY(0.3333333333333333D) - boomerang.getY();
		double d2 = p_82196_1_.getZ() - this.getZ();
		double d3 = (double) Mth.sqrt((float) (d0 * d0 + d2 * d2));
		boomerang.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, (float) (14 - this.level().getDifficulty().getId() * 4));
		this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
		this.level().addFreshEntity(boomerang);
		this.level().broadcastEntityEvent(this, (byte) 62);
	}

	public boolean isChargingCrossbow() {
		return this.entityData.get(IS_CHARGING_CROSSBOW);
	}

	@Override
	public void setChargingCrossbow(boolean p_33302_) {
		this.entityData.set(IS_CHARGING_CROSSBOW, p_33302_);
	}

	@Override
	public void onCrossbowAttackPerformed() {
		this.noActionTime = 0;
	}

	class MoveToGoal extends Goal {
		final Hunter hunter;
		final double stopDistance;
		final double speedModifier;

		MoveToGoal(Hunter p_i50459_2_, double p_i50459_3_, double p_i50459_5_) {
			this.hunter = p_i50459_2_;
			this.stopDistance = p_i50459_3_;
			this.speedModifier = p_i50459_5_;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		public void stop() {
			Hunter.this.navigation.stop();
		}

		public boolean canUse() {
			BlockPos blockpos = this.hunter.getHomeTarget();

			double distance = this.hunter.level().isDay() ? this.stopDistance : this.stopDistance / 1.5F;

			return blockpos != null && this.isTooFarAway(blockpos, distance);
		}

		public void tick() {
			BlockPos blockpos = this.hunter.getHomeTarget();
			if (blockpos != null && Hunter.this.navigation.isDone()) {
				if (this.isTooFarAway(blockpos, 10.0D)) {
					Vec3 vector3d = (new Vec3((double) blockpos.getX() - this.hunter.getX(), (double) blockpos.getY() - this.hunter.getY(), (double) blockpos.getZ() - this.hunter.getZ())).normalize();
					Vec3 vector3d1 = vector3d.scale(10.0D).add(this.hunter.getX(), this.hunter.getY(), this.hunter.getZ());
					Hunter.this.navigation.moveTo(vector3d1.x, vector3d1.y, vector3d1.z, this.speedModifier);
				} else {
					Hunter.this.navigation.moveTo((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ(), this.speedModifier);
				}
			}

		}

		private boolean isTooFarAway(BlockPos p_220846_1_, double p_220846_2_) {
			return !p_220846_1_.closerThan(this.hunter.blockPosition(), p_220846_2_);
		}
	}

	public class GetFoodGoal<T extends Hunter> extends Goal {
		private final T mob;

		public GetFoodGoal(T p_i50572_2_) {
			this.mob = p_i50572_2_;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		public boolean canUse() {
			if (!this.mob.hasActiveRaid()) {

				List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(16.0D, 8.0D, 16.0D), Hunter.ALLOWED_ITEMS);
				if (!list.isEmpty() && this.mob.hasLineOfSight(list.get(0))) {
					return this.mob.getNavigation().moveTo(list.get(0), (double) 1.1F);
				}

				return false;
			} else {
				return false;
			}
		}

		public void tick() {
			if (this.mob.getNavigation().getTargetPos().closerThan(this.mob.blockPosition(), 1.414D)) {
				List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(4.0D, 4.0D, 4.0D), Hunter.ALLOWED_ITEMS);
				if (!list.isEmpty()) {
					this.mob.pickUpItem(list.get(0));
				}
			}

		}
	}

    public enum HunterType {
		NORMAL,
		COLD;

		private HunterType() {

		}

		public static HunterType get(String nameIn) {
			for (HunterType role : values()) {
				if (role.name().equals(nameIn))
					return role;
			}
			return NORMAL;
		}

		public static HunterType create(String name) {
			throw new IllegalStateException("Enum not extended");
		}
	}
}
