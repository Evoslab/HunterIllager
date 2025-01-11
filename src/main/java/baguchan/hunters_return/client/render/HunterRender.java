package baguchan.hunters_return.client.render;

import bagu_chan.bagus_lib.client.layer.CustomArmorLayer;
import baguchan.hunters_return.HunterConfig;
import baguchan.hunters_return.client.ModModelLayers;
import baguchan.hunters_return.client.model.HunterModel;
import baguchan.hunters_return.client.model.OldHunterModel;
import baguchan.hunters_return.entity.Hunter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HunterRender<T extends Hunter> extends MobRenderer<T, HunterModel<T>> {
	private static final ResourceLocation ILLAGER = new ResourceLocation(baguchan.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter.png");
	private static final ResourceLocation ILLAGER_SLEEP = new ResourceLocation(baguchan.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter_sleep.png");
	private static final ResourceLocation ILLAGER_OLD = new ResourceLocation(baguchan.hunters_return.HuntersReturn.MODID, "textures/entity/hunter/hunter_old.png");

	private final HunterModel<T> old;
	private final HunterModel<T> normal = this.getModel();

	public HunterRender(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn, new HunterModel<>(renderManagerIn.bakeLayer(ModModelLayers.HUNTER)), 0.5F);
		this.addLayer(new CustomArmorLayer<>(this, renderManagerIn));
		this.addLayer(new ItemInHandLayer<>(this, Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer()));
		this.old = new OldHunterModel<>(renderManagerIn.bakeLayer(ModModelLayers.HUNTER_OLD));
	}

	@Override
	public void render(T p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
		super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
		if (HunterConfig.CLIENT.oldModel.get()) {
			this.model = this.old;
		} else {
			this.model = this.normal;
		}
	}

	@Override
	public ResourceLocation getTextureLocation(T p_110775_1_) {
		if (HunterConfig.CLIENT.oldModel.get()) {
			return ILLAGER_OLD;
		}
		if (p_110775_1_.isSleeping()) {
			return ILLAGER_SLEEP;
		}
		return ILLAGER;
	}
}
