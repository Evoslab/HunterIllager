package baguchi.hunters_return.item.data;

import baguchi.hunters_return.init.HunterDataComponents;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class QuiverContents implements TooltipComponent {
    public static final QuiverContents EMPTY = new QuiverContents(List.of());
    public static final Codec<QuiverContents> CODEC = ItemStack.CODEC
            .listOf()
            .flatXmap(QuiverContents::checkAndCreate, p_381696_ -> DataResult.success(p_381696_.items));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuiverContents> STREAM_CODEC = ItemStack.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(QuiverContents::new, p_331551_ -> p_331551_.items);
    private static final Fraction BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction(1, 16);
    private static final int NO_STACK_INDEX = -1;
    public static final int NO_SELECTED_ITEM_INDEX = -1;
    final List<ItemStack> items;
    final Fraction weight;
    final int selectedItem;

    QuiverContents(List<ItemStack> p_331277_, Fraction p_339622_, int p_361490_) {
        this.items = p_331277_;
        this.weight = p_339622_;
        this.selectedItem = p_361490_;
    }

    private static DataResult<QuiverContents> checkAndCreate(List<ItemStack> p_381706_) {
        try {
            Fraction fraction = computeContentWeight(p_381706_);
            return DataResult.success(new QuiverContents(p_381706_, fraction, -1));
        } catch (ArithmeticException arithmeticexception) {
            return DataResult.error(() -> "Excessive total bundle weight");
        }
    }

    public QuiverContents(List<ItemStack> p_331417_) {
        this(p_331417_, computeContentWeight(p_331417_), -1);
    }

    private static Fraction computeContentWeight(List<ItemStack> p_331148_) {
        Fraction fraction = Fraction.ZERO;

        for (ItemStack itemstack : p_331148_) {
            fraction = fraction.add(getWeight(itemstack).multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
        }

        return fraction;
    }

    static Fraction getWeight(ItemStack p_332084_) {
        QuiverContents bundlecontents = p_332084_.get(HunterDataComponents.QUIVER_CONTENTS);
        if (bundlecontents != null) {
            return BUNDLE_IN_BUNDLE_WEIGHT.add(bundlecontents.weight());
        } else {
            List<BeehiveBlockEntity.Occupant> list = p_332084_.getOrDefault(DataComponents.BEES, List.of());
            return !list.isEmpty() ? Fraction.ONE : Fraction.getFraction(1, p_332084_.getMaxStackSize() * 2);
        }
    }

    public static boolean canItemBeInBundle(ItemStack p_362945_) {
        return !p_362945_.isEmpty() && p_362945_.getItem().canFitInsideContainerItems();
    }

    public int getNumberOfItemsToShow() {
        int i = this.size();
        int j = i > 12 ? 11 : 12;
        int k = i % 4;
        int l = k == 0 ? 0 : 4 - k;
        return Math.min(i, j - l);
    }

    public ItemStack getItemUnsafe(int p_330802_) {
        return this.items.get(p_330802_);
    }

    public Stream<ItemStack> itemCopyStream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Iterable<ItemStack> items() {
        return this.items;
    }

    public Iterable<ItemStack> itemsCopy() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public int size() {
        return this.items.size();
    }

    public Fraction weight() {
        return this.weight;
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int getSelectedItem() {
        return this.selectedItem;
    }

    public boolean hasSelectedItem() {
        return this.selectedItem != -1;
    }

    @Override
    public boolean equals(Object p_331898_) {
        if (this == p_331898_) {
            return true;
        } else {
            return !(p_331898_ instanceof QuiverContents bundlecontents)
                    ? false
                    : this.weight.equals(bundlecontents.weight) && ItemStack.listMatches(this.items, bundlecontents.items);
        }
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    @Override
    public String toString() {
        return "BundleContents" + this.items;
    }

    public static class Mutable {
        private final List<ItemStack> items;
        private Fraction weight;
        private int selectedItem;

        public Mutable(QuiverContents p_332039_) {
            this.items = new ArrayList<>(p_332039_.items);
            this.weight = p_332039_.weight;
            this.selectedItem = p_332039_.selectedItem;
        }

        public QuiverContents.Mutable clearItems() {
            this.items.clear();
            this.weight = Fraction.ZERO;
            this.selectedItem = -1;
            return this;
        }

        private int findStackIndex(ItemStack p_331941_) {
            if (!p_331941_.isStackable()) {
                return -1;
            } else {
                for (int i = 0; i < this.items.size(); i++) {
                    if (ItemStack.isSameItemSameComponents(this.items.get(i), p_331941_)) {
                        return i;
                    }
                }

                return -1;
            }
        }

        private int getMaxAmountToAdd(ItemStack p_330527_) {
            Fraction fraction = Fraction.ONE.subtract(this.weight);
            return Math.max(fraction.divideBy(QuiverContents.getWeight(p_330527_)).intValue(), 0);
        }

        public int tryInsert(ItemStack p_331789_) {
            if (!QuiverContents.canItemBeInBundle(p_331789_)) {
                return 0;
            } else {
                int i = Math.min(p_331789_.getCount(), this.getMaxAmountToAdd(p_331789_));
                if (i == 0) {
                    return 0;
                } else {
                    this.weight = this.weight.add(QuiverContents.getWeight(p_331789_).multiplyBy(Fraction.getFraction(i, 1)));
                    int j = this.findStackIndex(p_331789_);
                    if (j != -1) {
                        ItemStack itemstack = this.items.remove(j);
                        ItemStack itemstack1 = itemstack.copyWithCount(itemstack.getCount() + i);
                        p_331789_.shrink(i);
                        this.items.add(0, itemstack1);
                    } else {
                        this.items.add(0, p_331789_.split(i));
                    }

                    return i;
                }
            }
        }

        public int tryTransfer(Slot p_330834_, Player p_331924_) {
            ItemStack itemstack = p_330834_.getItem();
            int i = this.getMaxAmountToAdd(itemstack);
            return QuiverContents.canItemBeInBundle(itemstack) ? this.tryInsert(p_330834_.safeTake(itemstack.getCount(), i, p_331924_)) : 0;
        }

        public void toggleSelectedItem(int p_360571_) {
            this.selectedItem = this.selectedItem != p_360571_ && p_360571_ < this.items.size() ? p_360571_ : -1;
        }

        @Nullable
        public ItemStack removeOne() {
            if (this.items.isEmpty()) {
                return null;
            } else {
                int i = this.selectedItem != -1 && this.selectedItem < this.items.size() ? this.selectedItem : 0;
                ItemStack itemstack = this.items.remove(i).copy();
                this.weight = this.weight.subtract(QuiverContents.getWeight(itemstack).multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
                this.toggleSelectedItem(-1);
                return itemstack;
            }
        }

        public Fraction weight() {
            return this.weight;
        }

        public QuiverContents toImmutable() {
            return new QuiverContents(List.copyOf(this.items), this.weight, this.selectedItem);
        }
    }
}
