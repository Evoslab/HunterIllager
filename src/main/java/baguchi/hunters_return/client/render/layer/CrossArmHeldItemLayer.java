package baguchi.hunters_return.client.render.layer;

import baguchi.hunters_return.client.render.state.HunterRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class CrossArmHeldItemLayer<T extends HunterRenderState, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public CrossArmHeldItemLayer(RenderLayerParent<T, M> p_116686_) {
		super(p_116686_);
	}

	@Override
	public void render(PoseStack p_116699_, MultiBufferSource p_117350_, int p_117351_, T p_116702_, float p_117353_, float p_117354_) {
		if (!p_116702_.isAggressive) {
			p_116699_.pushPose();
			p_116699_.translate(0.0D, (double) 0.4F, (double) -0.4F);
			p_116699_.mulPose(Axis.XP.rotationDegrees(180.0F));
			p_116702_.leftHandItem.render(p_116699_, p_117350_, p_117351_, OverlayTexture.NO_OVERLAY);

			p_116699_.popPose();
		}
	}
}