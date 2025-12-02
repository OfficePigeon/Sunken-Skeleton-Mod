package fun.wich.client;

import fun.wich.SunkenSkeletonEntity;
import fun.wich.SunkenSkeletonVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.*;
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
		super(context, new SunkenSkeletonEntityModel(context.getPart(SunkenSkeletonsClient.SUNKEN_SKELETON)), 0.5F);
		this.addFeature(new ArmorFeatureRenderer<>(this, EquipmentModelData.mapToEntityModel(EntityModelLayers.SKELETON_EQUIPMENT, context.getEntityModels(), SkeletonEntityModel::new), context.getEquipmentRenderer()));
	}
	@Override
	public Identifier getTexture(SunkenSkeletonEntityRenderState skeletonEntityRenderState) {
		return skeletonEntityRenderState.variant.texture;
	}
	@Override
	public SunkenSkeletonEntityRenderState createRenderState() { return new SunkenSkeletonEntityRenderState(); }
	@Override
	public void updateRenderState(SunkenSkeletonEntity entity, SunkenSkeletonEntityRenderState state, float f) {
		//Abstract Skeleton Entity
		super.updateRenderState(entity, state, f);
		state.attacking = entity.isAttacking();
		state.shaking = entity.isShaking();
		state.holdingBow = entity.getMainHandStack().isOf(Items.BOW);
		//Sunken Skeleton Entity
		state.variant = SunkenSkeletonVariant.get(entity);
		ItemStack stack = entity.getEquippedStack(EquipmentSlot.HEAD);
		if (entity.isSheared()) state.showFans = false;
		else if (stack.isIn(ItemTags.HEAD_ARMOR)) state.showFans = false;
		else state.showFans = !(stack.getItem() instanceof BlockItem);
	}
	@Override
	protected boolean isShaking(SunkenSkeletonEntityRenderState state) { return state.shaking; }
	@Override
	protected BipedEntityModel.ArmPose getArmPose(SunkenSkeletonEntity state, Arm arm) {
		return state.getMainArm() == arm && state.isAttacking() && state.getMainHandStack().isOf(Items.BOW) ? BipedEntityModel.ArmPose.BOW_AND_ARROW : BipedEntityModel.ArmPose.EMPTY;
	}
}