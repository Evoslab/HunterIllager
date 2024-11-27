package baguchi.hunters_return.client;

import baguchi.hunters_return.item.data.QuiverContents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientQuiverTooltip implements ClientTooltipComponent {
    private static final ResourceLocation PROGRESSBAR_BORDER_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_border");
    private static final ResourceLocation PROGRESSBAR_FILL_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
    private static final ResourceLocation PROGRESSBAR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/bundle_progressbar_full");
    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_highlight_front");
    private static final ResourceLocation SLOT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot_background");
    private static final int SLOT_MARGIN = 4;
    private static final int SLOT_SIZE = 24;
    private static final int GRID_WIDTH = 96;
    private static final int PROGRESSBAR_HEIGHT = 13;
    private static final int PROGRESSBAR_WIDTH = 96;
    private static final int PROGRESSBAR_BORDER = 1;
    private static final int PROGRESSBAR_FILL_MAX = 94;
    private static final int PROGRESSBAR_MARGIN_Y = 4;
    private static final Component BUNDLE_FULL_TEXT = Component.translatable("item.minecraft.bundle.full");
    private static final Component BUNDLE_EMPTY_TEXT = Component.translatable("item.minecraft.bundle.empty");
    private static final Component BUNDLE_EMPTY_DESCRIPTION = Component.translatable("item.minecraft.bundle.empty.description");
    private final QuiverContents contents;

    public ClientQuiverTooltip(QuiverContents p_331917_) {
        this.contents = p_331917_;
    }

    @Override
    public int getHeight(Font p_364523_) {
        return this.contents.isEmpty() ? getEmptyBundleBackgroundHeight(p_364523_) : this.backgroundHeight();
    }

    @Override
    public int getWidth(Font p_169901_) {
        return 96;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    private static int getEmptyBundleBackgroundHeight(Font p_361305_) {
        return getEmptyBundleDescriptionTextHeight(p_361305_) + 13 + 8;
    }

    private int backgroundHeight() {
        return this.itemGridHeight() + 13 + 8;
    }

    private int itemGridHeight() {
        return this.gridSizeY() * 24;
    }

    private int getContentXOffset(int p_368639_) {
        return (p_368639_ - 96) / 2;
    }

    private int gridSizeY() {
        return Mth.positiveCeilDiv(this.slotCount(), 4);
    }

    private int slotCount() {
        return Math.min(12, this.contents.size());
    }

    @Override
    public void renderImage(Font p_194042_, int p_194043_, int p_194044_, int p_368730_, int p_368543_, GuiGraphics p_282522_) {
        if (this.contents.isEmpty()) {
            this.renderEmptyBundleTooltip(p_194042_, p_194043_, p_194044_, p_368730_, p_368543_, p_282522_);
        } else {
            this.renderBundleWithItemsTooltip(p_194042_, p_194043_, p_194044_, p_368730_, p_368543_, p_282522_);
        }
    }

    private void renderEmptyBundleTooltip(Font p_365081_, int p_364144_, int p_364357_, int p_368704_, int p_368751_, GuiGraphics p_365036_) {
        drawEmptyBundleDescriptionText(p_364144_ + this.getContentXOffset(p_368704_), p_364357_, p_365081_, p_365036_);
        this.drawProgressbar(
                p_364144_ + this.getContentXOffset(p_368704_), p_364357_ + getEmptyBundleDescriptionTextHeight(p_365081_) + 4, p_365081_, p_365036_
        );
    }

    private void renderBundleWithItemsTooltip(Font p_364080_, int p_360498_, int p_363327_, int p_368677_, int p_368508_, GuiGraphics p_360526_) {
        boolean flag = this.contents.size() > 12;
        List<ItemStack> list = this.getShownItems(this.contents.getNumberOfItemsToShow());
        int i = p_360498_ + this.getContentXOffset(p_368677_) + 96;
        int j = p_363327_ + this.gridSizeY() * 24;
        int k = 1;

        for (int l = 1; l <= this.gridSizeY(); l++) {
            for (int i1 = 1; i1 <= 4; i1++) {
                int j1 = i - i1 * 24;
                int k1 = j - l * 24;
                if (shouldRenderSurplusText(flag, i1, l)) {
                    renderCount(j1, k1, this.getAmountOfHiddenItems(list), p_364080_, p_360526_);
                } else if (shouldRenderItemSlot(list, k)) {
                    this.renderSlot(k, j1, k1, list, k, p_364080_, p_360526_);
                    k++;
                }
            }
        }

        this.drawSelectedItemTooltip(p_364080_, p_360526_, p_360498_, p_363327_, p_368677_);
        this.drawProgressbar(p_360498_ + this.getContentXOffset(p_368677_), p_363327_ + this.itemGridHeight() + 4, p_364080_, p_360526_);
    }

    private List<ItemStack> getShownItems(int p_364960_) {
        int i = Math.min(this.contents.size(), p_364960_);
        return this.contents.itemCopyStream().toList().subList(0, i);
    }

    private static boolean shouldRenderSurplusText(boolean p_361034_, int p_363348_, int p_360653_) {
        return p_361034_ && p_363348_ * p_360653_ == 1;
    }

    private static boolean shouldRenderItemSlot(List<ItemStack> p_362150_, int p_364466_) {
        return p_362150_.size() >= p_364466_;
    }

    private int getAmountOfHiddenItems(List<ItemStack> p_362700_) {
        return this.contents.itemCopyStream().skip((long) p_362700_.size()).mapToInt(ItemStack::getCount).sum();
    }

    private void renderSlot(int p_283180_, int p_282972_, int p_282547_, List<ItemStack> p_361523_, int p_360587_, Font p_281863_, GuiGraphics p_283625_) {
        int i = p_361523_.size() - p_283180_;
        boolean flag = i == this.contents.getSelectedItem();
        ItemStack itemstack = p_361523_.get(i);
        if (flag) {
            p_283625_.blitSprite(RenderType::guiTextured, SLOT_HIGHLIGHT_BACK_SPRITE, p_282972_, p_282547_, 24, 24);
        } else {
            p_283625_.blitSprite(RenderType::guiTextured, SLOT_BACKGROUND_SPRITE, p_282972_, p_282547_, 24, 24);
        }

        p_283625_.renderItem(itemstack, p_282972_ + 4, p_282547_ + 4, p_360587_);
        p_283625_.renderItemDecorations(p_281863_, itemstack, p_282972_ + 4, p_282547_ + 4);
        if (flag) {
            p_283625_.blitSprite(RenderType::guiTexturedOverlay, SLOT_HIGHLIGHT_FRONT_SPRITE, p_282972_, p_282547_, 24, 24);
        }
    }

    private static void renderCount(int p_363359_, int p_364432_, int p_364090_, Font p_363903_, GuiGraphics p_363709_) {
        p_363709_.drawCenteredString(p_363903_, "+" + p_364090_, p_363359_ + 12, p_364432_ + 10, 16777215);
    }

    private void drawSelectedItemTooltip(Font p_360616_, GuiGraphics p_364594_, int p_362065_, int p_363779_, int p_368494_) {
        if (this.contents.hasSelectedItem()) {
            ItemStack itemstack = this.contents.getItemUnsafe(this.contents.getSelectedItem());
            Component component = itemstack.getStyledHoverName();
            int i = p_360616_.width(component.getVisualOrderText());
            int j = p_362065_ + p_368494_ / 2 - 12;
            p_364594_.renderTooltip(p_360616_, component, j - i / 2, p_363779_ - 15, itemstack.get(DataComponents.TOOLTIP_STYLE));
        }
    }

    private void drawProgressbar(int p_362365_, int p_364597_, Font p_363606_, GuiGraphics p_362696_) {
        p_362696_.blitSprite(RenderType::guiTextured, this.getProgressBarTexture(), p_362365_ + 1, p_364597_, this.getProgressBarFill(), 13);
        p_362696_.blitSprite(RenderType::guiTextured, PROGRESSBAR_BORDER_SPRITE, p_362365_, p_364597_, 96, 13);
        Component component = this.getProgressBarFillText();
        if (component != null) {
            p_362696_.drawCenteredString(p_363606_, component, p_362365_ + 48, p_364597_ + 3, 16777215);
        }
    }

    private static void drawEmptyBundleDescriptionText(int p_363213_, int p_362527_, Font p_361041_, GuiGraphics p_360386_) {
        p_360386_.drawWordWrap(p_361041_, BUNDLE_EMPTY_DESCRIPTION, p_363213_, p_362527_, 96, 11184810);
    }

    private static int getEmptyBundleDescriptionTextHeight(Font p_363613_) {
        return p_363613_.split(BUNDLE_EMPTY_DESCRIPTION, 96).size() * 9;
    }

    private int getProgressBarFill() {
        return Mth.clamp(Mth.mulAndTruncate(this.contents.weight(), 94), 0, 94);
    }

    private ResourceLocation getProgressBarTexture() {
        return this.contents.weight().compareTo(Fraction.ONE) >= 0 ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
    }

    @Nullable
    private Component getProgressBarFillText() {
        if (this.contents.isEmpty()) {
            return BUNDLE_EMPTY_TEXT;
        } else {
            return this.contents.weight().compareTo(Fraction.ONE) >= 0 ? BUNDLE_FULL_TEXT : null;
        }
    }
}
