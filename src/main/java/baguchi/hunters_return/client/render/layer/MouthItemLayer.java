package baguchi.hunters_return.client.render.layer;

import baguchi.hunters_return.client.model.HunterModel;
import baguchi.hunters_return.client.render.state.HunterRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class MouthItemLayer<T extends HunterRenderState, M extends HunterModel<T>> extends RenderLayer<T, M> {
    public MouthItemLayer(RenderLayerParent<T, M> p_116686_) {
        super(p_116686_);
    }

    @Override
    public void render(PoseStack p_116699_, MultiBufferSource p_117350_, int p_117351_, T p_116702_, float p_117353_, float p_117354_) {
        if (!p_116702_.mouthItem.isEmpty()) {
            p_116699_.pushPose();

            getParentModel().everything.translateAndRotate(p_116699_);
            getParentModel().body.translateAndRotate(p_116699_);
            getParentModel().head.translateAndRotate(p_116699_);
            p_116699_.translate(0.0D, (double) -(1F / 16), (double) -0.3F);
            p_116699_.mulPose(Axis.XP.rotationDegrees(90.0F));
            p_116702_.mouthItem.render(p_116699_, p_117350_, p_117351_, OverlayTexture.NO_OVERLAY);

            p_116699_.popPose();
        }
    }
}