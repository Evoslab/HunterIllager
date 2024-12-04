package baguchi.hunters_return.client.model;
// Made with Blockbench 4.5.2
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports

import baguchi.hunters_return.HunterConfig;
import baguchi.hunters_return.client.animation.HunterAnimations;
import baguchi.hunters_return.client.render.state.HunterRenderState;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;

public class HunterModel<T extends HunterRenderState> extends EntityModel<T> implements ArmedModel, HeadedModel, baguchi.bagus_lib.client.layer.IArmor {
    // This layer location should be baked with EntityRendererProvider.Context in the entityId renderer and passed into this model's constructor
	private final ModelPart body;
	private final ModelPart everything;
	private final ModelPart LeftLeg;
	private final ModelPart RightLeg;
	private final ModelPart RightArm;
	private final ModelPart LeftArm;
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart cape;

	public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
	public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;

	public HunterModel(ModelPart root) {
		super(root);
		this.root = root;
		this.everything = root.getChild("everything");
		this.body = this.everything.getChild("body");
		this.cape = this.body.getChild("cape");
		this.LeftLeg = this.everything.getChild("left_leg");
		this.RightLeg = this.everything.getChild("right_leg");
		this.RightArm = this.body.getChild("right_arm");
		this.LeftArm = this.body.getChild("left_arm");
		this.head = this.body.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition everything = partdefinition.addOrReplaceChild("everything", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition left_leg = everything.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(49, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -12.0F, 0.0F));

		PartDefinition right_leg = everything.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(44, 44).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -12.0F, 0.0F));

		PartDefinition body = everything.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0F, -12.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(28, 13).addBox(-4.0F, -11.75F, -2.5F, 8.0F, 14.0F, 5.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition cape = body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(28, 32).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 3.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(65, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leftEye = head.addOrReplaceChild("leftEye", CubeListBuilder.create().texOffs(6, 7).addBox(0.0F, -1.4604F, 0.74F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.5F, -4.75F));

		PartDefinition rightEye = head.addOrReplaceChild("rightEye", CubeListBuilder.create().texOffs(6, 7).addBox(-1.0F, -1.4604F, 0.74F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.5F, -4.75F));

		PartDefinition righteyebrows = head.addOrReplaceChild("righteyebrows", CubeListBuilder.create(), PartPose.offset(-2.5F, -4.9604F, -3.5196F));

		PartDefinition righteyebrows_r1 = righteyebrows.addOrReplaceChild("righteyebrows_r1", CubeListBuilder.create().texOffs(39, 0).addBox(-1.5F, -1.0F, -0.5902F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.0902F, 0.0F, 3.1416F, 0.0F));

		PartDefinition lefteyebrows = head.addOrReplaceChild("lefteyebrows", CubeListBuilder.create(), PartPose.offset(2.5F, -4.9604F, -3.5196F));

		PartDefinition lefteyebrows_r1 = lefteyebrows.addOrReplaceChild("lefteyebrows_r1", CubeListBuilder.create().texOffs(39, 0).mirror().addBox(-3.5F, -1.0F, -0.5902F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -0.0902F, 0.0F, 3.1416F, 0.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 44).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, -10.0F, 0.0F));

		PartDefinition left_arm_r1 = left_arm.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(17, 18).addBox(5.0F, 2.25F, -2.5F, 2.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(48, 32).mirror().addBox(0.0F, -0.75F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition leftHand = left_arm.addOrReplaceChild("leftHand", CubeListBuilder.create(), PartPose.offset(1.0F, 9.5F, 0.0F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, -10.0F, 0.0F));

		PartDefinition right_arm_r1 = right_arm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(19, 0).addBox(-7.0F, 2.25F, -3.0F, 2.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(24, 0).mirror().addBox(-5.0F, -0.75F, -3.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -2.0F, 0.5F, 0.0F, 0.0F, -0.1745F));

		PartDefinition rightHand = right_arm.addOrReplaceChild("rightHand", CubeListBuilder.create(), PartPose.offset(-1.0F, 11.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entityIn) {
		this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		AbstractIllager.IllagerArmPose abstractillager$illagerarmpose = entityIn.armPose;

		super.setupAnim(entityIn);
		this.head.yRot = entityIn.yRot * ((float) Math.PI / 180F);
		this.head.xRot = entityIn.xRot * ((float) Math.PI / 180F);
		if (entityIn.isRiding) {
			this.RightArm.xRot = (-(float) Math.PI / 5F);
			this.RightArm.yRot = 0.0F;
			this.RightArm.zRot = 0.0F;
			this.LeftArm.xRot = (-(float) Math.PI / 5F);
			this.LeftArm.yRot = 0.0F;
			this.LeftArm.zRot = 0.0F;
			this.RightLeg.xRot = -1.4137167F;
			this.RightLeg.yRot = ((float) Math.PI / 10F);
			this.RightLeg.zRot = 0.07853982F;
			this.LeftLeg.xRot = -1.4137167F;
			this.LeftLeg.yRot = (-(float) Math.PI / 10F);
			this.LeftLeg.zRot = -0.07853982F;
		} else {
			if (!HunterConfig.CLIENT.oldAnimation.get()) {
				if (entityIn.chargeAnimationState.isStarted()) {
					if (entityIn.mainArm == HumanoidArm.RIGHT) {
						this.animateWalk(HunterAnimations.HUNTER_RIGHT_WALK_ATTACK, entityIn.walkAnimationPos, entityIn.walkAnimationSpeed, 1, 1.5F);

					} else {
						this.animateWalk(HunterAnimations.HUNTER_LEFT_WALK_ATTACK, entityIn.walkAnimationPos, entityIn.walkAnimationSpeed, 1, 1.5F);
					}
				} else if (!entityIn.sleep) {
					this.animateWalk(HunterAnimations.HUNTER_WALK, entityIn.walkAnimationPos, entityIn.walkAnimationSpeed, 1, 1.5F);
				}
			} else {
				this.RightArm.xRot = Mth.cos(entityIn.walkAnimationPos * 0.6662F + 3.1415927F) * 2.0F * entityIn.walkAnimationSpeed * 0.5F;
				this.RightArm.yRot = 0.0F;
				this.RightArm.zRot = 0.0F;
				this.LeftArm.xRot = Mth.cos(entityIn.walkAnimationPos * 0.6662F) * 2.0F * entityIn.walkAnimationSpeed * 0.5F;
				this.LeftArm.yRot = 0.0F;
				this.LeftArm.zRot = 0.0F;
				this.RightLeg.xRot = Mth.cos(entityIn.walkAnimationPos * 0.6662F) * 1.4F * entityIn.walkAnimationSpeed * 0.5F;
				this.RightLeg.yRot = 0.0F;
				this.RightLeg.zRot = 0.0F;
				this.LeftLeg.xRot = Mth.cos(entityIn.walkAnimationPos * 0.6662F + 3.1415927F) * 1.4F * entityIn.walkAnimationSpeed * 0.5F;
				this.LeftLeg.yRot = 0.0F;
				this.LeftLeg.zRot = 0.0F;
			}
		}

		if (!HunterConfig.CLIENT.oldAnimation.get()) {
			if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
				AnimationUtils.animateCrossbowHold(this.RightArm, this.LeftArm, this.head, true);
			} else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
				AnimationUtils.animateCrossbowCharge(this.RightArm, this.LeftArm, entityIn.ageInTicks, entityIn.maxCrossbowChargeDuration, true);
			} else {
				if (entityIn.mainArm == HumanoidArm.RIGHT) {
					this.animate(entityIn.shootAnimationState, HunterAnimations.HUNTER_RIGHT_SHOT, entityIn.ageInTicks);
					this.animate(entityIn.chargeAnimationState, HunterAnimations.HUNTER_RIGHT_ATTACK_RANGE_CHARGE, entityIn.ageInTicks);
					this.animate(entityIn.attackAnimationState, HunterAnimations.HUNTER_RIGHT_ATTACK_MELEE, entityIn.ageInTicks, 1.5F);
					this.animate(entityIn.thrownAnimationState, HunterAnimations.HUNTER_LEFT_ATTACK_MELEE, entityIn.ageInTicks);

				} else {
					this.animate(entityIn.shootAnimationState, HunterAnimations.HUNTER_LEFT_SHOT, entityIn.ageInTicks);
					this.animate(entityIn.chargeAnimationState, HunterAnimations.HUNTER_LEFT_ATTACK_RANGE_CHARGE, entityIn.ageInTicks);
					this.animate(entityIn.attackAnimationState, HunterAnimations.HUNTER_LEFT_ATTACK_MELEE, entityIn.ageInTicks, 1.5F);
					this.animate(entityIn.thrownAnimationState, HunterAnimations.HUNTER_RIGHT_ATTACK_MELEE, entityIn.ageInTicks);

				}
			}
		} else {
			if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.ATTACKING) {
				if (entityIn.getMainHandItem().isEmpty()) {
					AnimationUtils.animateZombieArms(this.LeftArm, this.RightArm, true, entityIn.attackAnim, entityIn.ageInTicks);
				} else {
					AnimationUtils.swingWeaponDown(this.RightArm, this.LeftArm, entityIn.mainArm, entityIn.attackAnim, entityIn.ageInTicks);
				}
			} else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.SPELLCASTING) {
				this.RightArm.z = 0.0F;
				this.RightArm.x = -5.0F;
				this.LeftArm.z = 0.0F;
				this.LeftArm.x = 5.0F;
				this.RightArm.xRot = Mth.cos(entityIn.ageInTicks * 0.6662F) * 0.25F;
				this.LeftArm.xRot = Mth.cos(entityIn.ageInTicks * 0.6662F) * 0.25F;
				this.RightArm.zRot = 2.3561945F;
				this.LeftArm.zRot = -2.3561945F;
				this.RightArm.yRot = 0.0F;
				this.LeftArm.yRot = 0.0F;
			} else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
				this.RightArm.yRot = -0.1F + this.head.yRot;
				this.RightArm.xRot = -1.5707964F + this.head.xRot;
				this.LeftArm.xRot = -0.9424779F + this.head.xRot;
				this.LeftArm.yRot = this.head.yRot - 0.4F;
				this.LeftArm.zRot = 1.5707964F;
			} else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
				AnimationUtils.animateCrossbowHold(this.RightArm, this.LeftArm, this.head, true);
			} else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
				AnimationUtils.animateCrossbowCharge(this.RightArm, this.LeftArm, entityIn.ageInTicks, entityIn.maxCrossbowChargeDuration, true);
			} else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CELEBRATING) {
				this.RightArm.z = 0.0F;
				this.RightArm.x = -5.0F;
				this.RightArm.xRot = Mth.cos(entityIn.ageInTicks * 0.6662F) * 0.05F;
				this.RightArm.zRot = 2.670354F;
				this.RightArm.yRot = 0.0F;
				this.LeftArm.z = 0.0F;
				this.LeftArm.x = 5.0F;
				this.LeftArm.xRot = Mth.cos(entityIn.ageInTicks * 0.6662F) * 0.05F;
				this.LeftArm.zRot = -2.3561945F;
				this.LeftArm.yRot = 0.0F;
			}
		}
	}

	private ModelPart getArm(HumanoidArm p_102923_) {
		return p_102923_ == HumanoidArm.LEFT ? this.LeftArm : this.RightArm;
	}

	public ModelPart getHead() {
		return this.head;
	}

	public void translateToHand(HumanoidArm p_102925_, PoseStack p_102926_) {
		this.everything.translateAndRotate(p_102926_);
		this.body.translateAndRotate(p_102926_);
		this.getArm(p_102925_).translateAndRotate(p_102926_);
	}

	@Override
	public void translateToHead(ModelPart modelPart, PoseStack poseStack) {
		this.everything.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		modelPart.translateAndRotate(poseStack);
		poseStack.translate(0, -3 / 16F, 0);
	}

	@Override
	public void translateToChest(ModelPart modelPart, PoseStack poseStack) {
		this.everything.translateAndRotate(poseStack);
		modelPart.translateAndRotate(poseStack);
		poseStack.translate(0, -(12F / 16F), 0);
		poseStack.scale(1.05F, 1.05F, 1.05F);
	}

	@Override
	public void translateToLeg(ModelPart modelPart, PoseStack poseStack) {
		this.everything.translateAndRotate(poseStack);
		modelPart.translateAndRotate(poseStack);
		if (this.RightLeg == modelPart) {
			poseStack.translate(2 / 16F, 0F, 0);
		}
		if (this.LeftLeg == modelPart) {
			poseStack.translate(-2 / 16F, 0F, 0);
		}
		poseStack.translate(0, -(12F / 16F), 0);
		poseStack.scale(1.05F, 1.05F, 1.05F);
	}

	@Override
	public void translateToChestPat(ModelPart modelPart, PoseStack poseStack) {
		this.everything.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		modelPart.translateAndRotate(poseStack);
		if (this.RightArm == modelPart) {
			poseStack.translate(4 / 16F, -3 / 16F, 0);
		}
		if (this.LeftArm == modelPart) {
			poseStack.translate(-4 / 16F, -3 / 16F, 0);
		}
		poseStack.scale(1.05F, 1.05F, 1.05F);
	}

	public Iterable<ModelPart> rightHandArmors() {
		return ImmutableList.of(this.RightArm);
	}

	public Iterable<ModelPart> leftHandArmors() {
		return ImmutableList.of(this.LeftArm);
	}

	public Iterable<ModelPart> rightLegPartArmors() {
		return ImmutableList.of(this.RightLeg);
	}

	public Iterable<ModelPart> leftLegPartArmors() {
		return ImmutableList.of(this.LeftLeg);
	}

	public Iterable<ModelPart> bodyPartArmors() {
		return ImmutableList.of(this.body);
	}

	public Iterable<ModelPart> headPartArmors() {
		return ImmutableList.of(this.head);
	}
}