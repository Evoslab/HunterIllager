package baguchi.hunters_return.client.render.state;

import baguchi.hunters_return.entity.Hunter;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemDisplayContext;

public class HunterRenderState extends IllagerRenderState {
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState shootAnimationState = new AnimationState();
    public final AnimationState chargeAnimationState = new AnimationState();
    public final AnimationState thrownAnimationState = new AnimationState();
    public Hunter.HunterType hunterType;
    public boolean sleep;
    public final ItemStackRenderState mouthItem = new ItemStackRenderState();

    public static void extractMouthEntityRenderState(Hunter p_387833_, HunterRenderState p_387185_, ItemModelResolver p_386820_) {
        p_386820_.updateForLiving(p_387185_.mouthItem, p_387833_.getMouthItem(), ItemDisplayContext.GROUND, false, p_387833_);
    }
}
