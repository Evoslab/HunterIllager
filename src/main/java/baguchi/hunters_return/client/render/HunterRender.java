package baguchi.hunters_return.client.render;

import baguchi.bagus_lib.client.layer.CustomArmorLayer;
import baguchi.hunters_return.HunterConfig;
import baguchi.hunters_return.client.ModModelLayers;
import baguchi.hunters_return.client.model.HunterModel;
import baguchi.hunters_return.client.model.OldHunterModel;
import baguchi.hunters_return.client.render.state.HunterRenderState;
import baguchi.hunters_return.entity.Hunter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HunterRender extends MobRenderer<Hunter, HunterRenderState, HunterModel<HunterRenderState>> {
	private static final ResourceLocation ILLAGER = ResourceLocation.fromNamespaceAndPath(baguchi.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter.png");
	private static final ResourceLocation ILLAGER_SLEEP = ResourceLocation.fromNamespaceAndPath(baguchi.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter_sleep.png");
	private static final ResourceLocation ILLAGER_COLD = ResourceLocation.fromNamespaceAndPath(baguchi.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter_cold.png");
	private static final ResourceLocation ILLAGER_COLD_SLEEP = ResourceLocation.fromNamespaceAndPath(baguchi.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter_cold_sleep.png");
	private static final ResourceLocation ILLAGER_OLD = ResourceLocation.fromNamespaceAndPath(baguchi.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter_old.png");
	private static final ResourceLocation ILLAGER_COLD_OLD = ResourceLocation.fromNamespaceAndPath(baguchi.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter_cold_old.png");

	private final HunterModel<HunterRenderState> old;
	private final HunterModel<HunterRenderState> normal = this.getModel();

	public HunterRender(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn, new HunterModel<>(renderManagerIn.bakeLayer(ModModelLayers.HUNTER)), 0.5F);
		this.addLayer(new CustomArmorLayer<>(this, renderManagerIn));
		this.addLayer(new ItemInHandLayer<>(this, Minecraft.getInstance().getItemRenderer()));
		this.old = new OldHunterModel(renderManagerIn.bakeLayer(ModModelLayers.HUNTER_OLD));
	}

	@Override
	public void render(HunterRenderState p_361886_, PoseStack p_115311_, MultiBufferSource p_115312_, int p_115313_) {
		super.render(p_361886_, p_115311_, p_115312_, p_115313_);
		if (HunterConfig.CLIENT.oldModel.get()) {
			this.model = this.old;
		} else {
			this.model = this.normal;
		}
	}

	@Override
	public HunterRenderState createRenderState() {
		return new HunterRenderState();
	}

	@Override
	public void extractRenderState(Hunter hunter, HunterRenderState hunterState, float p_361157_) {
		super.extractRenderState(hunter, hunterState, p_361157_);

		hunterState.isRiding = hunter.isPassenger();
		hunterState.mainArm = hunter.getMainArm();
		hunterState.armPose = hunter.getArmPose();
		hunterState.maxCrossbowChargeDuration = hunterState.armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE
				? hunter.getUseItem().getUseDuration(hunter)
				: 0;
		hunterState.ticksUsingItem = hunter.getTicksUsingItem();
		hunterState.attackAnim = hunter.getAttackAnim(p_361157_);
		hunterState.isAggressive = hunter.isAggressive();

		hunterState.attackAnimationState.copyFrom(hunter.attackAnimationState);
		hunterState.chargeAnimationState.copyFrom(hunter.chargeAnimationState);
		hunterState.shootAnimationState.copyFrom(hunter.shootAnimationState);
		hunterState.thrownAnimationState.copyFrom(hunter.thrownAnimationState);
		hunterState.hunterType = hunter.getHunterType();
		hunterState.sleep = hunter.isSleeping();
	}

	@Override
	public ResourceLocation getTextureLocation(HunterRenderState p_110775_1_) {
		if (HunterConfig.CLIENT.oldModel.get()) {
			if (p_110775_1_.hunterType == Hunter.HunterType.COLD) {
				return ILLAGER_COLD_OLD;
			} else {
				return ILLAGER_OLD;
			}
		}
		if (p_110775_1_.hunterType == Hunter.HunterType.COLD) {
			if (p_110775_1_.sleep) {
				return ILLAGER_COLD_SLEEP;
			}
			return ILLAGER_COLD;
		}
		if (p_110775_1_.sleep) {
			return ILLAGER_SLEEP;
		}
		return ILLAGER;
	}
}
