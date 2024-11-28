package baguchi.hunters_return;

import baguchi.hunters_return.init.HunterDataComponents;
import baguchi.hunters_return.init.HunterItems;
import baguchi.hunters_return.item.QuiverItem;
import baguchi.hunters_return.item.data.QuiverContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;

@EventBusSubscriber(modid = HuntersReturn.MODID)
public class HunterEvents {
    @SubscribeEvent
    public static void arrowEvent(LivingGetProjectileEvent event) {
        ItemStack stack = event.getProjectileItemStack();
        if (stack.is(HunterItems.QUIVER.get())) {
            QuiverContents quiverContents = stack.getOrDefault(HunterDataComponents.QUIVER_CONTENTS, QuiverContents.EMPTY);

            if (!quiverContents.isEmpty()) {
                event.setProjectileItemStack(QuiverItem.removeOneNonStackItemFromQuiverWithoutPlayer(stack, quiverContents).get());
            }
        }
    }
}
