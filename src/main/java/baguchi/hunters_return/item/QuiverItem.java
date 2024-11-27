package baguchi.hunters_return.item;

import baguchi.hunters_return.init.HunterDataComponents;
import baguchi.hunters_return.item.data.QuiverContents;
import baguchi.hunters_return.item.data.QuiverTooltips;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;

import java.util.Optional;

public class QuiverItem extends Item {
    private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
    private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);

    public QuiverItem(Properties p_40512_) {
        super(p_40512_);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack p_150733_, Slot p_150734_, ClickAction p_150735_, Player p_150736_) {
        QuiverContents bundlecontents = p_150733_.get(HunterDataComponents.QUIVER_CONTENTS);
        if (bundlecontents == null || p_150733_.getCount() != 1) {
            return false;
        } else {
            ItemStack itemstack = p_150734_.getItem();
            QuiverContents.Mutable bundlecontents$mutable = new QuiverContents.Mutable(bundlecontents);
            if (p_150735_ == ClickAction.PRIMARY && !itemstack.isEmpty()) {
                if (bundlecontents$mutable.tryTransfer(p_150734_, p_150736_) > 0) {
                    playInsertSound(p_150736_);
                } else {
                    playInsertFailSound(p_150736_);
                }

                p_150733_.set(HunterDataComponents.QUIVER_CONTENTS, bundlecontents$mutable.toImmutable());
                this.broadcastChangesOnContainerMenu(p_150736_);
                return true;
            } else if (p_150735_ == ClickAction.SECONDARY && itemstack.isEmpty()) {
                ItemStack itemstack1 = bundlecontents$mutable.removeOne();
                if (itemstack1 != null) {
                    ItemStack itemstack2 = p_150734_.safeInsert(itemstack1);
                    if (itemstack2.getCount() > 0) {
                        bundlecontents$mutable.tryInsert(itemstack2);
                    } else {
                        playRemoveOneSound(p_150736_);
                    }
                }

                p_150733_.set(HunterDataComponents.QUIVER_CONTENTS, bundlecontents$mutable.toImmutable());
                this.broadcastChangesOnContainerMenu(p_150736_);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack p_150742_, ItemStack p_150743_, Slot p_150744_, ClickAction p_150745_, Player p_150746_, SlotAccess p_150747_
    ) {
        if (p_150742_.getCount() != 1) return false;
        if (p_150745_ == ClickAction.PRIMARY && p_150743_.isEmpty()) {
            toggleSelectedItem(p_150742_, -1);
            return false;
        } else {
            QuiverContents bundlecontents = p_150742_.get(HunterDataComponents.QUIVER_CONTENTS);
            if (bundlecontents == null) {
                return false;
            } else {
                QuiverContents.Mutable bundlecontents$mutable = new QuiverContents.Mutable(bundlecontents);
                if (p_150745_ == ClickAction.PRIMARY && !p_150743_.isEmpty() && (p_150743_.is(ItemTags.ARROWS)) || p_150743_.is(Items.FIREWORK_ROCKET)) {
                    if (p_150744_.allowModification(p_150746_) && bundlecontents$mutable.tryInsert(p_150743_) > 0) {
                        playInsertSound(p_150746_);
                    } else {
                        playInsertFailSound(p_150746_);
                    }

                    p_150742_.set(HunterDataComponents.QUIVER_CONTENTS, bundlecontents$mutable.toImmutable());
                    this.broadcastChangesOnContainerMenu(p_150746_);
                    return true;
                } else if (p_150745_ == ClickAction.SECONDARY && p_150743_.isEmpty()) {
                    if (p_150744_.allowModification(p_150746_)) {
                        ItemStack itemstack = bundlecontents$mutable.removeOne();
                        if (itemstack != null) {
                            playRemoveOneSound(p_150746_);
                            p_150747_.set(itemstack);
                        }
                    }

                    p_150742_.set(HunterDataComponents.QUIVER_CONTENTS, bundlecontents$mutable.toImmutable());
                    this.broadcastChangesOnContainerMenu(p_150746_);
                    return true;
                } else {
                    toggleSelectedItem(p_150742_, -1);
                    return false;
                }
            }
        }
    }

    @Override
    public InteractionResult use(Level p_150760_, Player p_150761_, InteractionHand p_150762_) {
        if (p_150760_.isClientSide) {
            return InteractionResult.CONSUME;
        } else {
            p_150761_.startUsingItem(p_150762_);
            return InteractionResult.SUCCESS_SERVER;
        }
    }

    private void dropContent(Level p_380196_, Player p_371609_, ItemStack p_371542_) {
        if (this.dropContent(p_371542_, p_371609_)) {
            playDropContentsSound(p_380196_, p_371609_);
            p_371609_.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack p_150769_) {
        QuiverContents bundlecontents = p_150769_.getOrDefault(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);
        return bundlecontents.weight().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(ItemStack p_150771_) {
        QuiverContents bundlecontents = p_150771_.getOrDefault(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);
        return Math.min(1 + Mth.mulAndTruncate(bundlecontents.weight(), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack p_150773_) {
        QuiverContents bundlecontents = p_150773_.getOrDefault(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);
        return bundlecontents.weight().compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
    }

    public static void toggleSelectedItem(ItemStack p_363912_, int p_363836_) {
        QuiverContents bundlecontents = p_363912_.get(HunterDataComponents.QUIVER_CONTENTS);
        if (bundlecontents != null) {
            QuiverContents.Mutable bundlecontents$mutable = new QuiverContents.Mutable(bundlecontents);
            bundlecontents$mutable.toggleSelectedItem(p_363836_);
            p_363912_.set(HunterDataComponents.QUIVER_CONTENTS, bundlecontents$mutable.toImmutable());
        }
    }

    public static boolean hasSelectedItem(ItemStack p_362328_) {
        QuiverContents bundlecontents = p_362328_.getOrDefault(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);
        return bundlecontents.getSelectedItem() != -1;
    }

    public static int getSelectedItem(ItemStack p_362900_) {
        QuiverContents bundlecontents = p_362900_.getOrDefault(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);
        return bundlecontents.getSelectedItem();
    }

    public static ItemStack getSelectedItemStack(ItemStack p_361689_) {
        QuiverContents bundlecontents = p_361689_.getOrDefault(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);
        return bundlecontents.getItemUnsafe(bundlecontents.getSelectedItem());
    }

    public static int getNumberOfItemsToShow(ItemStack p_361742_) {
        QuiverContents bundlecontents = p_361742_.getOrDefault(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);
        return bundlecontents.getNumberOfItemsToShow();
    }

    private boolean dropContent(ItemStack p_371437_, Player p_371809_) {
        QuiverContents bundlecontents = p_371437_.get(HunterDataComponents.QUIVER_CONTENTS);
        if (bundlecontents != null && !bundlecontents.isEmpty()) {
            Optional<ItemStack> optional = removeOneItemFromQuiver(p_371437_, p_371809_, bundlecontents);
            if (optional.isPresent()) {
                p_371809_.drop(optional.get(), true);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static Optional<ItemStack> removeOneItemFromQuiver(ItemStack p_371385_, Player p_371941_, QuiverContents p_371197_) {
        QuiverContents.Mutable bundlecontents$mutable = new QuiverContents.Mutable(p_371197_);
        ItemStack itemstack = bundlecontents$mutable.removeOne();
        if (itemstack != null) {
            playRemoveOneSound(p_371941_);
            p_371385_.set(HunterDataComponents.QUIVER_CONTENTS, bundlecontents$mutable.toImmutable());
            return Optional.of(itemstack);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<ItemStack> removeOneItemFromQuiverWithoutPlayer(ItemStack p_371385_, QuiverContents p_371197_) {
        QuiverContents.Mutable bundlecontents$mutable = new QuiverContents.Mutable(p_371197_);
        ItemStack itemstack = bundlecontents$mutable.removeOne();
        if (itemstack != null) {
            p_371385_.set(HunterDataComponents.QUIVER_CONTENTS, bundlecontents$mutable.toImmutable());
            return Optional.of(itemstack);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void onUseTick(Level p_371661_, LivingEntity p_371559_, ItemStack p_371431_, int p_371432_) {
        if (!p_371661_.isClientSide && p_371559_ instanceof Player player) {
            int i = this.getUseDuration(p_371431_, p_371559_);
            boolean flag = p_371432_ == i;
            if (flag || p_371432_ < i - 10 && p_371432_ % 2 == 0) {
                this.dropContent(p_371661_, player, p_371431_);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack p_371683_, LivingEntity p_371530_) {
        return 200;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack p_150775_) {
        return !p_150775_.has(DataComponents.HIDE_TOOLTIP) && !p_150775_.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)
                ? Optional.ofNullable(p_150775_.get(HunterDataComponents.QUIVER_CONTENTS)).map(QuiverTooltips::new)
                : Optional.empty();
    }

    @Override
    public void onDestroyed(ItemEntity p_150728_) {
        QuiverContents bundlecontents = p_150728_.getItem().get(HunterDataComponents.QUIVER_CONTENTS);
        if (bundlecontents != null) {
            p_150728_.getItem().set(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);
            ItemUtils.onContainerDestroyed(p_150728_, bundlecontents.itemsCopy());
        }
    }

    private static void playRemoveOneSound(Entity p_186343_) {
        p_186343_.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + p_186343_.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity p_186352_) {
        p_186352_.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + p_186352_.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(Entity p_364546_) {
        p_364546_.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

    private static void playDropContentsSound(Level p_380028_, Entity p_186354_) {
        p_380028_.playSound(
                null,
                p_186354_.blockPosition(),
                SoundEvents.BUNDLE_DROP_CONTENTS,
                SoundSource.PLAYERS,
                0.8F,
                0.8F + p_186354_.level().getRandom().nextFloat() * 0.4F
        );
    }

    private void broadcastChangesOnContainerMenu(Player p_376384_) {
        AbstractContainerMenu abstractcontainermenu = p_376384_.containerMenu;
        if (abstractcontainermenu != null) {
            abstractcontainermenu.slotsChanged(p_376384_.getInventory());
        }
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }
}

