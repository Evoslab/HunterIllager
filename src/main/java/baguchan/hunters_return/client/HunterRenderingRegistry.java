package baguchan.hunters_return.client;

import baguchan.hunters_return.client.model.HunterModel;
import baguchan.hunters_return.client.model.OldHunterModel;
import baguchan.hunters_return.client.render.BoomerangRender;
import baguchan.hunters_return.client.render.HunterRender;
import baguchan.hunters_return.init.HunterEntityRegistry;
import baguchan.hunters_return.init.HunterItems;
import baguchan.hunters_return.item.MiniCrossBowItem;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = baguchan.hunters_return.HuntersReturn.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HunterRenderingRegistry {
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HunterEntityRegistry.HUNTERILLAGER.get(), HunterRender::new);
        event.registerEntityRenderer(HunterEntityRegistry.BOOMERANG.get(), BoomerangRender::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.HUNTER, HunterModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.HUNTER_OLD, OldHunterModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void modelBake(ModelEvent.ModifyBakingResult event) {
        ItemProperties.register(HunterItems.MINI_CROSSBOW.get(), new ResourceLocation("pull"), (ClampedItemPropertyFunction) ((p_174610_, p_174611_, p_174612_, p_174613_) -> {
            if (p_174612_ == null) {
                return 0.0F;
            } else {
                return CrossbowItem.isCharged(p_174610_) ? 0.0F : (float) (p_174610_.getUseDuration() - p_174612_.getUseItemRemainingTicks()) / (float) MiniCrossBowItem.getChargeDuration(p_174610_);
            }
        }));
        ItemProperties.register(HunterItems.MINI_CROSSBOW.get(), new ResourceLocation("pulling"), (ClampedItemPropertyFunction) ((p_174605_, p_174606_, p_174607_, p_174608_) -> p_174607_ != null && p_174607_.isUsingItem() && p_174607_.getUseItem() == p_174605_ && !CrossbowItem.isCharged(p_174605_) ? 1.0F : 0.0F));
        ItemProperties.register(HunterItems.MINI_CROSSBOW.get(), new ResourceLocation("charged"), (ClampedItemPropertyFunction) ((p_275891_, p_275892_, p_275893_, p_275894_) -> CrossbowItem.isCharged(p_275891_) ? 1.0F : 0.0F));
        ItemProperties.register(HunterItems.MINI_CROSSBOW.get(), new ResourceLocation("firework"), (ClampedItemPropertyFunction) ((p_275887_, p_275888_, p_275889_, p_275890_) -> CrossbowItem.isCharged(p_275887_) && CrossbowItem.containsChargedProjectile(p_275887_, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F));
    }
}
