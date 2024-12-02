package baguchan.hunters_return.entity.ai;

import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class MiniCrossBowAttackGoal<T extends net.minecraft.world.entity.Mob & CrossbowAttackMob> extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T mob;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public <M extends Monster & CrossbowAttackMob> MiniCrossBowAttackGoal(M p_25814_, double p_25815_, float p_25816_) {
        this((T) p_25814_, p_25815_, p_25816_);
    }

    public MiniCrossBowAttackGoal(T p_25814_, double p_25815_, float p_25816_) {
        this.mob = p_25814_;
        this.speedModifier = p_25815_;
        this.attackRadiusSqr = p_25816_ * p_25816_;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.isHolding(is -> is.getItem() instanceof CrossbowItem);
    }

    @Override
    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
            CrossbowItem.setCharged(this.mob.getUseItem(), false);
        }
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        InteractionHand hand2 = ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem) == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null) {
            boolean flag = this.mob.getSensing().hasLineOfSight(livingentity);
            boolean flag1 = this.seeTime > 0;
            if (flag != flag1) {
                this.seeTime = 0;
            }

            if (flag) {
                this.seeTime++;
            } else {
                this.seeTime--;
            }

            double d0 = this.mob.distanceToSqr(livingentity);
            boolean flag2 = (d0 > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
            if (flag2) {
                this.updatePathDelay--;
                if (this.updatePathDelay <= 0) {
                    this.mob.getNavigation().moveTo(livingentity, this.canRun() ? this.speedModifier : this.speedModifier * 0.75);
                    this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
                }
            } else {
                this.updatePathDelay = 0;
                this.mob.getNavigation().stop();
            }

            this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            if (this.crossbowState == CrossbowState.UNCHARGED) {
                if (!flag2) {
                    this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                    this.crossbowState = CrossbowState.CHARGING;
                    this.mob.setChargingCrossbow(true);
                }
            } else if (this.crossbowState == CrossbowState.CHARGING) {
                if (!this.mob.isUsingItem()) {
                    this.crossbowState = CrossbowState.UNCHARGED;
                }

                int i = this.mob.getTicksUsingItem();
                ItemStack itemstack = this.mob.getUseItem();
                if (i >= itemstack.getUseDuration()) {
                    this.mob.releaseUsingItem();
                    this.crossbowState = CrossbowState.CHARGED;
                    this.attackDelay = 12 + this.mob.getRandom().nextInt(4);
                    this.mob.setChargingCrossbow(false);
                }
            } else if (this.crossbowState == CrossbowState.CHARGED) {
                this.attackDelay--;
                if (this.attackDelay == 0) {
                    this.crossbowState = CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && flag) {
                this.mob.performCrossbowAttack(this.mob, 1.2F);
                if (this.mob.getItemInHand(hand2).getItem() instanceof CrossbowItem miniCrossBowItem) {
                    this.attackDelay = 8;
                    this.crossbowState = CrossbowState.READY_TO_ATTACK_SECOND;
                } else {
                    this.crossbowState = CrossbowState.UNCHARGED;
                }
            } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK_SECOND && flag) {
                this.attackDelay--;
                if (this.attackDelay == 0) {
                    this.performSecondCrossbowAttack(this.mob, hand2, 1.2F);
                    this.crossbowState = CrossbowState.UNCHARGED;
                }
            }
        }

    }

    protected void performSecondCrossbowAttack(LivingEntity p_32337_, InteractionHand hand, float p_32338_) {
        ItemStack itemstack = p_32337_.getItemInHand(hand);
        if (itemstack.getItem() instanceof CrossbowItem crossbowitem) {
            crossbowitem.performShooting(
                    p_32337_.level(), p_32337_, hand, itemstack, p_32338_, (float) (14 - p_32337_.level().getDifficulty().getId() * 4)
            );
        }

        this.mob.onCrossbowAttackPerformed();
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }

    static enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK,
        READY_TO_ATTACK_SECOND;
    }
}
