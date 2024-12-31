package baguchan.hunters_return.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;


public class MiniCrossBowItem extends CrossbowItem {
    private static final ChargingSounds DEFAULT_SOUNDS = new ChargingSounds(
            Optional.of(SoundEvents.CROSSBOW_LOADING_START), Optional.of(SoundEvents.CROSSBOW_LOADING_MIDDLE), Optional.of(SoundEvents.CROSSBOW_LOADING_END)
    );

    public MiniCrossBowItem(Properties miniCrossbow) {
        super(miniCrossbow);
    }

    @Override
    public InteractionResultHolder use(Level p_40920_, Player p_40921_, InteractionHand p_40922_) {
        ItemStack itemstack = p_40921_.getItemInHand(p_40922_);
        InteractionHand hand2 = p_40922_ == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack itemstack2 = p_40921_.getItemInHand(hand2);
        ChargedProjectiles chargedprojectiles = itemstack.get(DataComponents.CHARGED_PROJECTILES);
        ChargedProjectiles chargedprojectiles2 = itemstack2.get(DataComponents.CHARGED_PROJECTILES);

        if (chargedprojectiles != null && !chargedprojectiles.isEmpty()) {
            this.performShooting(p_40920_, p_40921_, p_40922_, itemstack, getShootingPower(chargedprojectiles), 1.0F, null);
            return InteractionResultHolder.consume(itemstack);
        } else if (chargedprojectiles2 != null && !chargedprojectiles2.isEmpty()) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            return super.use(p_40920_, p_40921_, p_40922_);
        }
    }

    @Override
    public void releaseUsing(ItemStack p_40875_, Level p_40876_, LivingEntity p_40877_, int p_40878_) {
        int i = this.getUseDuration(p_40875_, p_40877_) - p_40878_;
        float f = getPowerForTime(i, p_40875_, p_40877_);
        InteractionHand hand2 = p_40877_.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack itemstack2 = p_40877_.getItemInHand(hand2);
        if (f >= 1.0F && !isCharged(p_40875_) && tryLoadProjectiles(p_40877_, p_40875_)) {
            CrossbowItem.ChargingSounds crossbowitem$chargingsounds = this.getChargingSounds(p_40875_);
            crossbowitem$chargingsounds.end()
                    .ifPresent(
                            p_381568_ -> p_40876_.playSound(
                                    null,
                                    p_40877_.getX(),
                                    p_40877_.getY(),
                                    p_40877_.getZ(),
                                    p_381568_.value(),
                                    p_40877_.getSoundSource(),
                                    1.0F,
                                    1.0F / (p_40876_.getRandom().nextFloat() * 0.5F + 1.0F) + 0.5F
                            )
                    );
            if (itemstack2.getItem() instanceof MiniCrossBowItem && !isCharged(itemstack2)) {
                tryLoadProjectiles(p_40877_, itemstack2);
            }
        }
    }
    private static boolean tryLoadProjectiles(LivingEntity p_40860_, ItemStack p_40861_) {
        List<ItemStack> list = draw(p_40861_, p_40860_.getProjectile(p_40861_), p_40860_);
        if (!list.isEmpty()) {
            p_40861_.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));
            return true;
        } else {
            return false;
        }
    }

    ChargingSounds getChargingSounds(ItemStack p_345050_) {
        return EnchantmentHelper.pickHighestLevel(p_345050_, EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS).orElse(DEFAULT_SOUNDS);
    }


    @Override
    public boolean useOnRelease(ItemStack p_41464_) {
        return true;
    }

    private static float getShootingPower(ChargedProjectiles p_330249_) {
        return p_330249_.contains(Items.FIREWORK_ROCKET) ? 1.6F * 0.85F : 2.25F;
    }

    @Override
    protected int getDurabilityUse(ItemStack p_331489_) {
        return p_331489_.is(Items.FIREWORK_ROCKET) ? 3 : 1;
    }


    @Override
    public int getUseDuration(ItemStack p_40938_, LivingEntity p_344898_) {
        return getChargeDuration(p_40938_, p_344898_) + 3;
    }

    public static int getChargeDuration(ItemStack p_352255_, LivingEntity p_345687_) {
        float f = EnchantmentHelper.modifyCrossbowChargingTime(p_352255_, p_345687_, 0.65F);
        return Mth.floor(f * 20.0F);
    }


    private static float getPowerForTime(int p_40854_, ItemStack p_40855_, LivingEntity p_344803_) {
        float f = (float) p_40854_ / (float) getChargeDuration(p_40855_, p_344803_);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
    public int getEnchantmentValue() {
        return 2;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 8;
    }
}
