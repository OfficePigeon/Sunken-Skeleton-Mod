package fun.wich.client;

import fun.wich.SunkenSkeletonEntity;
import fun.wich.SunkenSkeletonVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SunkenSkeletonEntityRenderer extends BipedEntityRenderer<SunkenSkeletonEntity, SunkenSkeletonEntityRenderState, SunkenSkeletonEntityModel> {
	public SunkenSkeletonEntityRenderer(EntityRendererFactory.Context context) {
		this(context, EntityModelLayers.SKELETON_EQUIPMENT, new SunkenSkeletonEntityModel(context.getPart(SunkenSkeletonsClient.SUNKEN_SKELETON)));
	}
	public SunkenSkeletonEntityRenderer(EntityRendererFactory.Context context, EquipmentModelData<EntityModelLayer> equipmentModelData, SunkenSkeletonEntityModel skeletonEntityModel) {
		super(context, skeletonEntityModel, 0.5F);
		this.addFeature(new ArmorFeatureRenderer<>(this, EquipmentModelData.mapToEntityModel(equipmentModelData, context.getEntityModels(), SkeletonEntityModel::new), context.getEquipmentRenderer()));
	}
	@Override
	public Identifier getTexture(SunkenSkeletonEntityRenderState skeletonEntityRenderState) {
		return skeletonEntityRenderState.variant.texture;
	}
	@Override
	public SunkenSkeletonEntityRenderState createRenderState() { return new SunkenSkeletonEntityRenderState(); }
	@Override
	public void updateRenderState(SunkenSkeletonEntity entity, SunkenSkeletonEntityRenderState skeletonEntityRenderState, float f) {
		//Abstract Skeleton Entity
		super.updateRenderState(entity, skeletonEntityRenderState, f);
		skeletonEntityRenderState.attacking = entity.isAttacking();
		skeletonEntityRenderState.shaking = entity.isShaking();
		skeletonEntityRenderState.holdingBow = entity.getMainHandStack().isOf(Items.BOW);
		//Sunken Skeleton Entity
		skeletonEntityRenderState.variant = SunkenSkeletonVariant.get(entity);
		ItemStack stack = entity.getEquippedStack(EquipmentSlot.HEAD);
		if (stack.isIn(ItemTags.HEAD_ARMOR)) skeletonEntityRenderState.showFans = false;
		else if (stack.getItem() instanceof BlockItem) skeletonEntityRenderState.showFans = false;
		else skeletonEntityRenderState.showFans = true;
	}
	@Override
	protected boolean isShaking(SunkenSkeletonEntityRenderState state) { return state.shaking; }
	@Override
	protected BipedEntityModel.ArmPose getArmPose(SunkenSkeletonEntity abstractSkeletonEntity, Arm arm) {
		return abstractSkeletonEntity.getMainArm() == arm && abstractSkeletonEntity.isAttacking() && abstractSkeletonEntity.getMainHandStack().isOf(Items.BOW)
				? BipedEntityModel.ArmPose.BOW_AND_ARROW
				: BipedEntityModel.ArmPose.EMPTY;
	}
	@Override
	public void render(SunkenSkeletonEntityRenderState state, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
		for (ModelPart part : this.model.fans) part.visible = state.showFans;
		super.render(state, matrixStack, orderedRenderCommandQueue, cameraRenderState);
	}
}