package baguchan.hunters_return.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;


public class MiniCrossBowItem extends CrossbowItem {

    public MiniCrossBowItem(Properties miniCrossbow) {
        super(miniCrossbow);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_40920_, Player p_40921_, InteractionHand p_40922_) {
        ItemStack itemstack = p_40921_.getItemInHand(p_40922_);
        InteractionHand hand2 = p_40922_ == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack itemstack2 = p_40921_.getItemInHand(hand2);
        if (isCharged(itemstack)) {
            performShooting(p_40920_, p_40921_, p_40922_, itemstack, getShootingPower(itemstack), 1.0F);
            setCharged(itemstack, false);
            return InteractionResultHolder.consume(itemstack);
        } else if (isCharged(itemstack2)) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            return super.use(p_40920_, p_40921_, p_40922_);
        }
    }

    @Override
    public void releaseUsing(ItemStack p_40875_, Level p_40876_, LivingEntity p_40877_, int p_40878_) {
        int i = this.getUseDuration(p_40875_) - p_40878_;
        float f = getPowerForTime(i, p_40875_);
        if (f >= 1.0F && !isCharged(p_40875_) && tryLoadProjectiles(p_40877_, p_40875_)) {
            InteractionHand hand2 = p_40877_.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack itemstack2 = p_40877_.getItemInHand(hand2);

            setCharged(p_40875_, true);
            SoundSource soundsource = p_40877_ instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            p_40876_.playSound((Player) null, p_40877_.getX(), p_40877_.getY(), p_40877_.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundsource, 1.0F, 1.0F / (p_40876_.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
            if (itemstack2.getItem() instanceof MiniCrossBowItem && !isCharged(itemstack2)) {
                tryLoadProjectiles(p_40877_, itemstack2);
                setCharged(itemstack2, true);
            }
        }

    }

    private static boolean tryLoadProjectiles(LivingEntity p_40860_, ItemStack p_40861_) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, p_40861_);
        int j = i == 0 ? 1 : 3;
        boolean flag = p_40860_ instanceof Player && ((Player) p_40860_).getAbilities().instabuild;
        ItemStack itemstack = p_40860_.getProjectile(p_40861_);
        ItemStack itemstack1 = itemstack.copy();

        for (int k = 0; k < j; ++k) {
            if (k > 0) {
                itemstack = itemstack1.copy();
            }

            if (itemstack.isEmpty() && flag) {
                itemstack = new ItemStack(Items.ARROW);
                itemstack1 = itemstack.copy();
            }

            if (!loadProjectile(p_40860_, p_40861_, itemstack, k > 0, flag)) {
                return false;
            }
        }

        return true;
    }

    private static boolean loadProjectile(LivingEntity p_40863_, ItemStack p_40864_, ItemStack p_40865_, boolean p_40866_, boolean p_40867_) {
        if (p_40865_.isEmpty()) {
            return false;
        } else {
            boolean flag = p_40867_ && p_40865_.getItem() instanceof ArrowItem;
            ItemStack itemstack;
            if (!flag && !p_40867_ && !p_40866_) {
                itemstack = p_40865_.split(1);
                if (p_40865_.isEmpty() && p_40863_ instanceof Player) {
                    ((Player) p_40863_).getInventory().removeItem(p_40865_);
                }
            } else {
                itemstack = p_40865_.copy();
            }

            addChargedProjectile(p_40864_, itemstack);
            return true;
        }
    }

    private static void addChargedProjectile(ItemStack p_40929_, ItemStack p_40930_) {
        CompoundTag compoundtag = p_40929_.getOrCreateTag();
        ListTag listtag;
        if (compoundtag.contains("ChargedProjectiles", 9)) {
            listtag = compoundtag.getList("ChargedProjectiles", 10);
        } else {
            listtag = new ListTag();
        }

        CompoundTag compoundtag1 = new CompoundTag();
        p_40930_.save(compoundtag1);
        listtag.add(compoundtag1);
        compoundtag.put("ChargedProjectiles", listtag);
    }

    private static float getShootingPower(ItemStack p_40946_) {
        return containsChargedProjectile(p_40946_, Items.FIREWORK_ROCKET) ? 1.6F * 0.85F : 1.5F;
    }

    @Override
    public int getUseDuration(ItemStack p_40938_) {
        return getChargeDuration(p_40938_) + 3;
    }

    private static float getPowerForTime(int p_40854_, ItemStack p_40855_) {
        float f = (float) p_40854_ / (float) getChargeDuration(p_40855_);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public static int getChargeDuration(ItemStack p_40940_) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, p_40940_);
        return i == 0 ? 13 : Mth.clamp(13 - 5 * i, 1, 13);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 8;
    }
}
