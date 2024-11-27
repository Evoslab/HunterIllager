package baguchi.hunters_return.client;

import baguchi.hunters_return.HuntersReturn;
import baguchi.hunters_return.client.mouse.QuiverMouseActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = HuntersReturn.MODID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void initToolTip(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            abstractContainerScreen.addItemSlotMouseAction(new QuiverMouseActions(Minecraft.getInstance()));
        }

    }
}
