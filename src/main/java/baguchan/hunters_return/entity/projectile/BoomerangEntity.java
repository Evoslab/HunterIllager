package baguchan.hunters_return.entity.projectile;

import baguchan.hunters_return.init.HunterDamageSource;
import baguchan.hunters_return.init.HunterEnchantments;
import baguchan.hunters_return.init.HunterEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;

public class BoomerangEntity extends Projectile {
	private static final EntityDataAccessor<Byte> LOYALTY_LEVEL = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.BYTE);

	private static final EntityDataAccessor<Byte> PIERCING_LEVEL = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> BOUNCE_LEVEL = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> TOUCH_GROUND = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<ItemStack> BOOMERANG = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.ITEM_STACK);


	private int totalHits;

	private int flyTick;
	public boolean inGround;
	protected int inGroundTime;

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
		this.entityData.set(LOYALTY_LEVEL, (byte) EnchantmentHelper.getLoyalty(boomerang));
		this.entityData.set(PIERCING_LEVEL, (byte) EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, boomerang));
		this.entityData.set(BOUNCE_LEVEL, (byte) EnchantmentHelper.getItemEnchantmentLevel(HunterEnchantments.BOUNCE.get(), boomerang));
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
		int loyaltyLevel = (this.entityData.get(LOYALTY_LEVEL)).byteValue();
		int piercingLevel = (this.entityData.get(PIERCING_LEVEL)).byteValue();
		Entity shooter = getOwner();
		if (result.getEntity() != shooter) {

			if (!isReturning() || loyaltyLevel <= 0) {
				int sharpness = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, getBoomerang());
				int damage = (int) ((3.0D * Math.sqrt(getDeltaMovement().length()) + Math.min(1, sharpness) + Math.max(0, sharpness - 1) * 0.5D) + 0.5F * piercingLevel);

				if (this.isOnFire()) {
					result.getEntity().setSecondsOnFire(5);
				}

				if (damage > 0) {
					result.getEntity().hurt(this.boomerangAttack(shooter), damage);
				}
				if (shooter instanceof LivingEntity) {
					getBoomerang().hurtAndBreak(1, (LivingEntity) shooter, p_222182_1_ -> {
					});
				}

				double speed = getSpeed();
				if (piercingLevel < 1 && this.totalHits >= this.getBounceLevel() || this.totalHits >= piercingLevel + this.getBounceLevel() && speed > 0.4000000059604645D) {
					returnToOwner = true;

				} else if (piercingLevel < 1 && this.totalHits < this.getBounceLevel() || this.totalHits < piercingLevel + this.getBounceLevel() && speed <= 0.4000000059604645D) {
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
			if (getOwner() != null && shouldReturnToThrower() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.LOYALTY, getBoomerang()) > 0) {
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
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		BlockPos pos = result.getBlockPos();
		BlockState state = this.level().getBlockState(pos);
		SoundType soundType = state.getSoundType(this.level(), pos, this);

		int returnLevel = this.entityData.get(LOYALTY_LEVEL).byteValue();
		Entity entity = getOwner();
		Vec3 movement = this.getDeltaMovement();
		if (!isReturning() || returnLevel > 0 && isReturning()) {

			if (movement.length() < 0.2F && movement.y <= 0) {
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
					Vec3 vec31 = vec3.normalize().scale((double) 0.05F);
					this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
				}
			} else {
				Vec3i direction = result.getDirection().getNormal();
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
		if (this.flyTick >= 10 && entityIn == getOwner()) {
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

	public void lerpTo(double p_36728_, double p_36729_, double p_36730_, float p_36731_, float p_36732_, int p_36733_, boolean p_36734_) {
		this.setPos(p_36728_, p_36729_, p_36730_);
		this.setRot(p_36731_, p_36732_);
	}

	public void lerpMotion(double p_36786_, double p_36787_, double p_36788_) {
		super.lerpMotion(p_36786_, p_36787_, p_36788_);
		this.inGroundTime = 0;
	}

	@Override
	public void tick() {
		super.tick();
		int returningLevel = (this.entityData.get(LOYALTY_LEVEL));
		boolean flag = false;
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
			if (hitresult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
				this.onHit(hitresult);
			}




			Entity entity = getOwner();
			if (!isReturning()) {
				if (this.flyTick >= 30 && entity != null) {
					this.level().playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0F, 1.0F);
					setReturning(true);
				}
			}
			if (entity != null && !shouldReturnToThrower() && isReturning()) {
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


			if (flag) {
				this.setYRot((float) (Mth.atan2(-d5, -d1) * 180.0F / (float) Math.PI));
			} else {
				this.setYRot((float) (Mth.atan2(d5, d1) * 180.0F / (float) Math.PI));
			}

			this.setXRot((float) (Mth.atan2(d6, d4) * 180.0F / (float) Math.PI));
			this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
			this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
			float f = 1.0F;
			if (this.isInWater()) {
				for (int j = 0; j < 4; j++) {
					float f1 = 0.25F;
					this.level().addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25, d2 - d6 * 0.25, d3 - d1 * 0.25, d5, d6, d1);
				}

				f = this.getWaterInertia();
			}

			if (!flag) {
				this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y - this.getGravity(), this.getDeltaMovement().z);
			}

			this.setDeltaMovement(this.getDeltaMovement().scale((double) f));


			this.setPos(d7, d2, d3);
			this.checkInsideBlocks();

			if (!this.level().isClientSide()) {
				if (returningLevel > 0) {
					List<ItemEntity> list = this.level().getEntities(EntityTypeTest.forClass(ItemEntity.class), this.getBoundingBox().inflate(0.1F), Entity::isAlive);

					if (this.getPassengers().isEmpty()) {
						if (list != null && !list.isEmpty()) {
							list.get(0).startRiding(this);
						}
					}
				}
			}
			this.flyTick++;
		}

		if (this.shouldDropToThrower()) {
			this.drop(this.getX(), this.getY(), this.getZ());
		}
	}

	private float getWaterInertia() {
		return 0.6F;
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
		this.setDeltaMovement(vec3.multiply((double) (this.random.nextFloat() * 0.2F), (double) (this.random.nextFloat() * 0.2F), (double) (this.random.nextFloat() * 0.2F)));
		this.inGroundTime = 0;
	}

	protected float getGravity() {
		if (getLoyaltyLevel() > 0 && !this.isReturning()) {
			if (this.isInWater()) {
				return 0.01F;
			}

			return 0.0F;
		}

		return this.getLoyaltyLevel() <= 0 && this.isTouchGround() ? 0.05F : 0.0F;
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
	protected void defineSynchedData() {
		this.entityData.define(LOYALTY_LEVEL, (byte) 0);
		this.entityData.define(PIERCING_LEVEL, (byte) 0);
		this.entityData.define(BOUNCE_LEVEL, (byte) 0);
		this.entityData.define(RETURNING, false);
		this.entityData.define(TOUCH_GROUND, true);
		this.entityData.define(BOOMERANG, ItemStack.EMPTY);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.put("boomerang", getBoomerang().save(new CompoundTag()));
		nbt.putInt("totalHits", this.totalHits);
		nbt.putInt("InGroundTime", this.inGroundTime);
		nbt.putInt("FlyTick", this.flyTick);
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
		setBoomerang(ItemStack.of(nbt.getCompound("boomerang")));
		this.totalHits = nbt.getInt("totalHits");
		this.inGroundTime = nbt.getInt("InGroundTime");
		if (nbt.contains("inBlockState", 10)) {
			this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), nbt.getCompound("inBlockState"));
		}

		this.setInGround(nbt.getBoolean("inGround"));
		this.flyTick = nbt.getInt("FlyTick");
		setReturning(nbt.getBoolean("returning"));
		this.entityData.set(LOYALTY_LEVEL, (byte) EnchantmentHelper.getLoyalty(getBoomerang()));
		this.entityData.set(PIERCING_LEVEL, (byte) EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, getBoomerang()));
		this.entityData.set(BOUNCE_LEVEL, (byte) EnchantmentHelper.getItemEnchantmentLevel(HunterEnchantments.BOUNCE.get(), getBoomerang()));
		this.setTouchGround(nbt.getBoolean("TouchGround"));
	}

	private int getLoyaltyLevel() {
		return (this.entityData.get(LOYALTY_LEVEL)).byteValue();
	}

	public int getInGroundTime() {
		return inGroundTime;
	}

	private int getBounceLevel() {
		int loyaltyLevel = (this.entityData.get(LOYALTY_LEVEL)).byteValue();
		int bounceLevel = (this.entityData.get(BOUNCE_LEVEL)).byteValue();
		if (loyaltyLevel > 0)
			return bounceLevel * 2;
		return bounceLevel;
	}

	public boolean isReturning() {
		return ((Boolean) this.entityData.get(RETURNING)).booleanValue();
	}

	public ItemStack getBoomerang() {
		return (ItemStack) this.entityData.get(BOOMERANG);
	}

	public double getSpeed() {
		return Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().y * getDeltaMovement().y + getDeltaMovement().z * getDeltaMovement().z);
	}

	public double getVelocity() {
		return Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
	}

	public int getPiercingLevel() {
		return (this.entityData.get(PIERCING_LEVEL)).byteValue();
	}

	public void setReturning(boolean returning) {
		this.entityData.set(RETURNING, Boolean.valueOf(returning));
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
