package baguchi.hunters_return.client.model;


import baguchi.bagus_lib.client.layer.IArmor;
import baguchi.hunters_return.client.render.state.HunterRenderState;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class OldHunterModel<T extends HunterRenderState> extends HunterModel<T> implements ArmedModel, HeadedModel, IArmor {

    public OldHunterModel(ModelPart p_170688_) {
        super(p_170688_);

		/*getHat().addBox("hood_fixed", -4.0F, -10.0F, -4.0F, 8, 10, 8, scaleFactor + 0.45F, 32, 0);
		this.cape = new ModelPart((Model) this, 0, 0);
		this.cape.setTexSize(textureWidthIn, textureHeightIn);
		this.cape.setPos(0.0F, 0.5F, 3.0F);
		this.cape.texOffs(0, 64).addBox(-4.5F, 0.0F, -0.5F, 9.0F, 11.0F, 1.0F, 0.1F + scaleFactor);
		this._body.addChild(this.cape);
		this.quiver = new ModelPart((Model) this);
		this.quiver.setTexSize(textureWidthIn, textureHeightIn);
		this.quiver.setPos(3.0F, 0.0F, 2.5F);
		this.quiver.texOffs(20, 64).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 13.0F, 3.0F, -0.5F + scaleFactor);
		this.quiver.texOffs(36, 64).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 13.0F, 3.0F, scaleFactor);
		this.cape.addChild(this.quiver);
		this.capeLower = new ModelPart((Model) this, 0, 0);
		this.capeLower.setTexSize(textureWidthIn, textureHeightIn);
		this.capeLower.setPos(0.0F, 11.0F, 0.0F);
		this.capeLower.texOffs(0, 76).addBox(-4.5F, 0.0F, -0.5F, 9.0F, 8.0F, 1.0F, 0.1F + scaleFactor);
		this.cape.addChild(this.capeLower);*/
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition everything = partdefinition.addOrReplaceChild("everything", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition left_leg = everything.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -12.0F, 0.0F));

        PartDefinition right_leg = everything.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -12.0F, 0.0F));

        PartDefinition body = everything.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, -12.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(1, 38).addBox(-4.0F, -11.75F, -2.5F, 8.0F, 14.0F, 5.0F, new CubeDeformation(0.75F)), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition cape = body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 64).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 76).mirror().addBox(-4.5F, 11.0F, 0.0F, 9.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -12.0F, 3.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition quiver = cape.addOrReplaceChild("quiver", CubeListBuilder.create().texOffs(20, 64).addBox(-2.5F, 0.0F, 1.0F, 5.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -3.0F));

        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 46).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, -10.0F, 0.0F));

        PartDefinition leftHand = left_arm.addOrReplaceChild("leftHand", CubeListBuilder.create(), PartPose.offset(1.0F, 9.5F, 0.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 46).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, -10.0F, 0.0F));

        PartDefinition rightHand = right_arm.addOrReplaceChild("rightHand", CubeListBuilder.create(), PartPose.offset(-1.0F, 11.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }
}