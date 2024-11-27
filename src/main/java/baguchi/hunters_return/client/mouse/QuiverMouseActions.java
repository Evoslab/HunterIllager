package baguchi.hunters_return.client.mouse;

import baguchi.hunters_return.init.HunterItems;
import baguchi.hunters_return.item.QuiverItem;
import baguchi.hunters_return.message.QuiverSelectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector2i;

@OnlyIn(Dist.CLIENT)
public class QuiverMouseActions implements ItemSlotMouseAction {
    private final Minecraft minecraft;
    private final ScrollWheelHandler scrollWheelHandler;

    public QuiverMouseActions(Minecraft p_361400_) {
        this.minecraft = p_361400_;
        this.scrollWheelHandler = new ScrollWheelHandler();
    }

    @Override
    public boolean matches(Slot p_360559_) {
        return p_360559_.getItem().is(HunterItems.QUIVER.get());
    }

    @Override
    public boolean onMouseScrolled(double p_360390_, double p_362650_, int p_363161_, ItemStack p_364763_) {
        int i = QuiverItem.getNumberOfItemsToShow(p_364763_);
        if (i == 0) {
            return false;
        } else {
            Vector2i vector2i = this.scrollWheelHandler.onMouseScroll(p_360390_, p_362650_);
            int j = vector2i.y == 0 ? -vector2i.x : vector2i.y;
            if (j != 0) {
                int k = QuiverItem.getSelectedItem(p_364763_);
                int l = ScrollWheelHandler.getNextScrollWheelSelection((double) j, k, i);
                if (k != l) {
                    this.toggleSelectedQuiverItem(p_364763_, p_363161_, l);
                }
            }

            return true;
        }
    }

    @Override
    public void onStopHovering(Slot p_363289_) {
        this.unselectedQuiverItem(p_363289_.getItem(), p_363289_.index);
    }

    @Override
    public void onSlotClicked(Slot p_372932_, ClickType p_372800_) {
        if (p_372800_ == ClickType.QUICK_MOVE || p_372800_ == ClickType.SWAP) {
            this.unselectedQuiverItem(p_372932_.getItem(), p_372932_.index);
        }
    }

    private void toggleSelectedQuiverItem(ItemStack p_364573_, int p_364078_, int p_365257_) {
        if (this.minecraft.getConnection() != null && p_365257_ < QuiverItem.getNumberOfItemsToShow(p_364573_)) {
            QuiverItem.toggleSelectedItem(p_364573_, p_365257_);
            QuiverSelectPacket message = new QuiverSelectPacket(p_364078_, p_365257_);
            PacketDistributor.sendToServer(message);
        }
    }

    public void unselectedQuiverItem(ItemStack p_365339_, int p_363847_) {
        this.toggleSelectedQuiverItem(p_365339_, p_363847_, -1);
    }
}
