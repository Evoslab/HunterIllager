package baguchi.hunters_return.entity.projectile;

import baguchi.hunters_return.init.HunterDamageSource;
import baguchi.hunters_return.init.HunterEnchantments;
import baguchi.hunters_return.init.HunterEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class BoomerangEntity extends Projectile {
	private static final EntityDataAccessor<Integer> RETURN_LEVEL = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> BOUNCE_LEVEL = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> TOUCH_GROUND = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<ItemStack> BOOMERANG = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.ITEM_STACK);

	private static final int RETURN_TICK = 30;

	private int totalHits;

	private int flyTick;
	public boolean inGround;
	protected int inGroundTime;
	private double baseDamage = 3.0;
	@Nullable
	private BlockState lastState;

	public BoomerangEntity(EntityType<? extends BoomerangEntity> entityEntityType, Level world) {
		super(entityEntityType, world);
	}

	public BoomerangEntity(EntityType<? extends BoomerangEntity> type, Level world, LivingEntity shootingEntity, ItemStack boomerang) {
		super(type, world);
		this.setPos(shootingEntity.getX(), shootingEntity.getEyeY() - 0.1F, shootingEntity.getZ());
		setOwner(shootingEntity);
		setBoomerang(boomerang);
		Registry<Enchantment> enchantments = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		this.entityData.set(RETURN_LEVEL, EnchantmentHelper.getTagEnchantmentLevel(enchantments.getOrThrow(HunterEnchantments.RETURN), boomerang));
		this.entityData.set(BOUNCE_LEVEL, EnchantmentHelper.getTagEnchantmentLevel(enchantments.getOrThrow(HunterEnchantments.BOUNCE), boomerang));
		this.totalHits = 0;
		this.setTouchGround(false);
	}

	public BoomerangEntity(Level world, LivingEntity entity, ItemStack boomerang) {
		this(HunterEntityRegistry.BOOMERANG.get(), world, entity, boomerang);
	}

	public DamageSource boomerangAttack(@Nullable Entity p_270857_) {
		return this.damageSources().source(HunterDamageSource.BOOMERANG, this, p_270857_);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		boolean returnToOwner = false;
		int loyaltyLevel = this.getReturnLevel();
		Entity shooter = getOwner();
		if (result.getEntity() != shooter) {

			if (!isReturning() || loyaltyLevel <= 0) {
				int damage = (int) ((baseDamage * Math.sqrt(getDeltaMovement().length())));

				float realDamage = damage;
				if (this.isOnFire()) {
                    result.getEntity().setRemainingFireTicks(5);
				}
				DamageSource damageSource = this.boomerangAttack(shooter);

				if (this.level() instanceof ServerLevel serverlevel) {
					realDamage = EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), this.getOwner(), damageSource, realDamage);
				}

				if (realDamage > 0) {
					if (this.level() instanceof ServerLevel serverLevel) {
						if (result.getEntity().hurtServer(serverLevel, damageSource, realDamage)) {
						if (result.getEntity() instanceof LivingEntity livingEntity) {
							this.doKnockback(livingEntity, damageSource);
							EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, livingEntity, damageSource, this.getWeaponItem());

						}

					}
				}
				}
				double speed = getSpeed();
				if (this.totalHits >= this.getBounceLevel() || this.totalHits >= this.getBounceLevel() && speed > 0.4000000059604645D) {
					returnToOwner = true;

				} else if (this.totalHits < this.getBounceLevel() || this.totalHits < this.getBounceLevel() && speed <= 0.4000000059604645D) {
					Vec3 motion = getDeltaMovement();
					double motionX = motion.x;
					double motionY = motion.y;
					double motionZ = motion.z;
					motionX = -motionX;
					motionZ = -motionZ;
					setDeltaMovement(motionX, motionY, motionZ);
					this.setDeltaMovement(this.getDeltaMovement().scale(0.91F + this.getBounceLevel() * 0.01F));
				}
				this.totalHits++;
			}

		}
		if (returnToOwner && !isReturning())
			if (getOwner() != null && shouldReturnToThrower() && loyaltyLevel > 0) {
				this.level().playSound(null, shooter.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
				Vec3 motion = getDeltaMovement();
				double motionX = motion.x;
				double motionY = motion.y;
				double motionZ = motion.z;
				motionX = -motionX;
				motionZ = -motionZ;
				setDeltaMovement(motionX, motionY, motionZ);
				if (loyaltyLevel > 0 && !isReturning() &&
						shooter != null) {
					this.level().playSound(null, shooter.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
					setReturning(true);
				}
			} else {
				Vec3 motion = getDeltaMovement();
				double motionX = motion.x;
				double motionY = motion.y;
				double motionZ = motion.z;
				motionX = -motionX;
				motionZ = -motionZ;
				setDeltaMovement(motionX, motionY, motionZ);
				if (loyaltyLevel > 0 && !isReturning() &&
						shooter != null) {
					this.level().playSound(null, shooter.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
					setReturning(true);
				}
			}
	}

	@Override
	public ItemStack getWeaponItem() {
		return this.getBoomerang();
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		BlockPos pos = result.getBlockPos();
		BlockState state = this.level().getBlockState(pos);
		SoundType soundType = state.getSoundType(this.level(), pos, this);
		ItemStack itemstack = this.getWeaponItem();
		Level var5 = this.level();
		int returnLevel = this.getReturnLevel();
		Entity entity = getOwner();
		Vec3 movement = this.getDeltaMovement();
		if (!isReturning() || returnLevel > 0 && isReturning()) {
			if (var5 instanceof ServerLevel serverlevel) {
				if (itemstack != null) {
					this.hitBlockEnchantmentEffects(serverlevel, result, itemstack);
				}
			}

			if (movement.length() < 0.35F && movement.y <= 0) {
				if (returnLevel > 0) {
					if (!isReturning() &&
							entity != null) {
						this.level().playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
						setReturning(true);
					}
				} else {
					setReturning(false);
					this.inGround = true;
					this.lastState = this.level().getBlockState(result.getBlockPos());
					Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
					this.setDeltaMovement(vec3);
					Vec3 vec31 = vec3.normalize().scale(0.05F);
					this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
				}
			} else {
				Vec3i direction = result.getDirection().getUnitVec3i();
				switch (result.getDirection()) {
					case UP, SOUTH, EAST -> direction = direction.multiply(-1);
					default -> {
					}
				}
				direction = new Vec3i(direction.getX() == 0 ? 1 : direction.getX(), direction.getY() == 0 ? 1 : direction.getY(), direction.getZ() == 0 ? 1 : direction.getZ());
				this.setDeltaMovement(movement.multiply(new Vec3(direction.getX(), direction.getY(), direction.getZ())).scale(0.91F + this.getBounceLevel() * 0.01F));
				this.playSound(SoundEvents.WOOD_STEP, 0.5F, 1.0F);
				if (!isReturning()) {
					this.level().playSound(null, getX(), getY(), getZ(), soundType.getHitSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch());
				}
				if (returnLevel <= 0) {
					this.setTouchGround(true);
					this.setReturning(false);
				}
			}
		}
	}

	protected void doKnockback(LivingEntity p_346111_, DamageSource p_346412_) {
		float var10000;
		label18:
		{
			if (this.getBoomerang() != null) {
				Level var6 = this.level();
				if (var6 instanceof ServerLevel serverlevel) {
					var10000 = EnchantmentHelper.modifyKnockback(serverlevel, this.getBoomerang(), p_346111_, p_346412_, 0.0F);
					break label18;
				}
			}

			var10000 = 0.0F;
		}

		double d0 = var10000;
		if (d0 > 0.0) {
			double d1 = Math.max(0.0, 1.0 - p_346111_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
			Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(d0 * 0.6 * d1);
			if (vec3.lengthSqr() > 0.0) {
				p_346111_.push(vec3.x, 0.1, vec3.z);
			}
		}

	}

	protected void hitBlockEnchantmentEffects(ServerLevel p_345462_, BlockHitResult p_345204_, ItemStack p_345083_) {
		Vec3 vec3 = p_345204_.getBlockPos().clampLocationWithin(p_345204_.getLocation());
		Entity var6 = this.getOwner();
		LivingEntity var10002;
		if (var6 instanceof LivingEntity livingentity) {
			var10002 = livingentity;
		} else {
			var10002 = null;
		}

		EnchantmentHelper.onHitBlock(p_345462_, p_345083_, var10002, this, null, vec3, p_345462_.getBlockState(p_345204_.getBlockPos()), (p_348569_) -> {
		});
	}


	public int getFlyTick() {
		return flyTick;
	}

	private boolean shouldReturnToThrower() {
		Entity entity = getOwner();
		if (entity != null && entity.isAlive())
			return (!entity.isSpectator());
		return false;
	}

	private boolean shouldDropToThrower() {
		Entity entity = getOwner();
		if (entity != null && entity.isAlive())
			return !entity.isSpectator() && !(entity instanceof Player) && (this.distanceToSqr(entity) < 3);
		return false;
	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);
		if ((this.flyTick >= 10 || this.inGround) && entityIn == getOwner()) {
			if (!this.level().isClientSide) {
				if (!entityIn.isCreative() && this.tryPickup(entityIn) || entityIn.isCreative()) {
					this.playSound(SoundEvents.ITEM_PICKUP);
					entityIn.take(this, 1);
					this.discard();
				}
			}
		}
	}

	protected boolean tryPickup(Player p_150121_) {
		return p_150121_.getInventory().add(this.getBoomerang());
	}

	public void drop(double x, double y, double z) {
		if (!this.level().isClientSide()) {
			if (!(getOwner() instanceof Player) || (getOwner() instanceof Player && !((Player) getOwner()).isCreative())) {
				this.level().addFreshEntity(new ItemEntity(this.level(), x, y, z, getBoomerang().split(1)));
				this.discard();
			} else {
				this.discard();
			}
		}
	}

	public void move(MoverType p_36749_, Vec3 p_36750_) {
		super.move(p_36749_, p_36750_);
		if (p_36749_ != MoverType.SELF && this.shouldFall()) {
			this.startFalling();
		}

	}

	public void shoot(double p_36775_, double p_36776_, double p_36777_, float p_36778_, float p_36779_) {
		super.shoot(p_36775_, p_36776_, p_36777_, p_36778_, p_36779_);
		this.inGroundTime = 0;
	}

	public void lerpMotion(double p_36786_, double p_36787_, double p_36788_) {
		super.lerpMotion(p_36786_, p_36787_, p_36788_);
		this.inGroundTime = 0;
	}

	@Override
	public void tick() {
		super.tick();
		int returningLevel = (this.entityData.get(RETURN_LEVEL));
		boolean flag = false;
		boolean flag2 = returningLevel > 0 || !this.isTouchGround();
		Vec3 vec3 = this.getDeltaMovement();
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			double d0 = vec3.horizontalDistance();
			this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * 180.0F / (float) Math.PI));
			this.setXRot((float) (Mth.atan2(vec3.y, d0) * 180.0F / (float) Math.PI));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}

		BlockPos blockpos = this.blockPosition();
		BlockState blockstate = this.level().getBlockState(blockpos);
		if (!blockstate.isAir() && !flag && returningLevel <= 0) {
			VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
			if (!voxelshape.isEmpty()) {
				Vec3 vec31 = this.position();

				for (AABB aabb : voxelshape.toAabbs()) {
					if (aabb.move(blockpos).contains(vec31)) {
						this.inGround = true;
						break;
					}
				}
			}
		}


		if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType))) {
			this.clearFire();
		}

		if (this.inGround && !flag) {
			if (this.lastState != blockstate && this.shouldFall()) {
				this.startFalling();
			} else if (!this.level().isClientSide) {
				this.tickDespawn();
			}

			this.inGroundTime++;
		} else {
			this.inGroundTime = 0;
			HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
			if (hitresult.getType() != HitResult.Type.MISS && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hitresult)) {
				this.hitTargetOrDeflectSelf(hitresult);
			}




			Entity entity = getOwner();
			if (!isReturning()) {
				if (this.flyTick >= RETURN_TICK && entity != null && !this.isTouchGround()) {
					if (returningLevel > 0) {
						this.level().playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
					}
					setReturning(true);
				}
			}
			if (returningLevel > 0 && entity != null && !shouldReturnToThrower() && isReturning()) {
				drop(getX(), getY(), getZ());
			} else if (entity != null && isReturning()) {
				this.noPhysics = true;
				Vec3 vec3d3 = new Vec3(entity.getX() - getX(), entity.getEyeY() - getY(), entity.getZ() - getZ());
				double d0 = 0.05D * (returningLevel + 1);
				this.setDeltaMovement(getDeltaMovement().scale(0.95D).add(vec3d3.normalize().scale(d0)));
			}


			vec3 = this.getDeltaMovement();
			double d5 = vec3.x;
			double d6 = vec3.y;
			double d1 = vec3.z;

			double d7 = this.getX() + d5;
			double d2 = this.getY() + d6;
			double d3 = this.getZ() + d1;
			double d4 = vec3.horizontalDistance();


			if (flag2) {
				this.setYRot((float) (Mth.atan2(-d5, -d1) * 180.0F / (float) Math.PI));
			} else {
				this.setYRot((float) (Mth.atan2(d5, d1) * 180.0F / (float) Math.PI));
			}

			this.setXRot((float) (Mth.atan2(d6, d4) * 180.0F / (float) Math.PI));
			this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
			this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
			float f = returningLevel > 0 ? 1.0F : 0.99F;
			if (this.isInWater()) {
				for (int j = 0; j < 4; j++) {
					float f1 = 0.25F;
					this.level().addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25, d2 - d6 * 0.25, d3 - d1 * 0.25, d5, d6, d1);
				}

				f = this.getWaterInertia();
			}

			if (!flag2) {
				this.applyGravity();
			}

			this.setDeltaMovement(this.getDeltaMovement().scale(f));


			this.setPos(d7, d2, d3);
			if (!this.level().isClientSide()) {

				List<ItemEntity> list = this.level().getEntities(EntityTypeTest.forClass(ItemEntity.class), this.getBoundingBox().inflate(0.1F), Entity::isAlive);

				if (this.getPassengers().isEmpty()) {
					if (list != null && !list.isEmpty()) {
						list.get(0).startRiding(this);
					}
				}
			}
			this.flyTick++;
		}

		if (this.shouldDropToThrower()) {
			this.drop(this.getX(), this.getY(), this.getZ());
		}
	}

	protected float getWaterInertia() {
		return 0.6F;
	}

	@Nullable
	protected EntityHitResult findHitEntity(Vec3 p_36758_, Vec3 p_36759_) {
		return ProjectileUtil.getEntityHitResult(
				this.level(), this, p_36758_, p_36759_, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity
		);
	}


	protected void tickDespawn() {
		++this.inGroundTime;
		if (this.inGroundTime >= 1200) {
			this.discard();
		}

	}

	private boolean shouldFall() {
		return this.isInGround() && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
	}

	private void startFalling() {
		this.setInGround(false);
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(vec3.multiply(this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F));
		this.inGroundTime = 0;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double p_70112_1_) {
		double d0 = this.getBoundingBox().getSize() * 5.0F;
		if (Double.isNaN(d0)) {
			d0 = 1.0D;
		}

		d0 = d0 * 64.0D;
		return p_70112_1_ < d0 * d0;
	}

	@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(RETURN_LEVEL, 0);
		builder.define(BOUNCE_LEVEL, 0);
        builder.define(RETURNING, false);
		builder.define(TOUCH_GROUND, true);
        builder.define(BOOMERANG, ItemStack.EMPTY);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
        nbt.put("boomerang", getBoomerang().save(this.registryAccess(), new CompoundTag()));
		nbt.putInt("totalHits", this.totalHits);
		nbt.putInt("InGroundTime", this.inGroundTime);
		nbt.putInt("FlyTick", this.flyTick);

		nbt.putInt("ReturnLevel", this.getReturnLevel());
		nbt.putInt("BounceLevel", this.getBounceLevel());
		nbt.putBoolean("returning", isReturning());
		if (this.lastState != null) {
			nbt.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
		}

		nbt.putBoolean("inGround", this.isInGround());
		nbt.putBoolean("TouchGround", this.isTouchGround());
	}


	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
        setBoomerang(ItemStack.parse(this.registryAccess(), nbt.getCompound("boomerang")).orElse(this.getBoomerang()));
		this.totalHits = nbt.getInt("totalHits");
		this.inGroundTime = nbt.getInt("InGroundTime");
		if (nbt.contains("inBlockState", 10)) {
			this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), nbt.getCompound("inBlockState"));
		}

		this.setInGround(nbt.getBoolean("inGround"));
		this.flyTick = nbt.getInt("FlyTick");
		setReturning(nbt.getBoolean("returning"));
		this.setReturnLevel(nbt.getInt("ReturnLevel"));
		this.setBounceLevel(nbt.getInt("BounceLevel"));
		this.setTouchGround(nbt.getBoolean("TouchGround"));
	}

	private int getReturnLevel() {
		return (this.entityData.get(RETURN_LEVEL));
	}

	public int getInGroundTime() {
		return inGroundTime;
	}


	public void setReturnLevel(int returning) {
		this.entityData.set(RETURN_LEVEL, returning);
	}

	public void setBounceLevel(int bounce) {
		this.entityData.set(BOUNCE_LEVEL, bounce);
	}

	private int getBounceLevel() {
		int returnLevel = this.getReturnLevel();
		int bounceLevel = this.entityData.get(BOUNCE_LEVEL);
		if (returnLevel > 0)
			return bounceLevel * 2;
		return bounceLevel;
	}

	public boolean isReturning() {
		return this.entityData.get(RETURNING);
	}

	public ItemStack getBoomerang() {
		return this.entityData.get(BOOMERANG);
	}

	public double getSpeed() {
		return Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().y * getDeltaMovement().y + getDeltaMovement().z * getDeltaMovement().z);
	}

	public double getVelocity() {
		return Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
	}

	public void setBaseDamage(double baseDamage) {
		this.baseDamage = baseDamage;
	}

	public void setReturning(boolean returning) {
		this.entityData.set(RETURNING, returning);
	}

	@Override
	protected double getDefaultGravity() {
		return this.getReturnLevel() <= 0 && this.isTouchGround() ? 0.05F : 0.0F;
	}

	public void setInGround(boolean flag) {
		this.inGround = flag;
	}

	public boolean isInGround() {
		return this.inGround;
	}

	public void setBoomerang(ItemStack stack) {
		this.entityData.set(BOOMERANG, stack);
	}

	public void setTouchGround(boolean flag) {
		this.entityData.set(TOUCH_GROUND, flag);
	}

	public boolean isTouchGround() {
		return this.entityData.get(TOUCH_GROUND);
	}
}
