package fun.wich.client;

import fun.wich.SunkenSkeletonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

import java.util.List;

@Environment(value= EnvType.CLIENT)
public class SunkenSkeletonEntityModel extends SkeletonEntityModel<SunkenSkeletonEntity> {
	public ModelPart[] fans;
	public SunkenSkeletonEntityModel(ModelPart modelPart) {
		super(modelPart);
		fans = List.of(
				this.head.getChild("top_fan"),
				this.head.getChild("side_fan"),
				this.head.getChild("wrap")
		).toArray(ModelPart[]::new);
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = getModelData(Dilation.NONE, 0);
		ModelPartData root = modelData.getRoot();
		ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4, -8, -4, 8, 8, 8), ModelTransform.NONE);
		head.addChild("top_fan", ModelPartBuilder.create().uv(24, 0).cuboid(-1, -16, -4, 9, 8, 0), ModelTransform.NONE);
		head.addChild("side_fan", ModelPartBuilder.create().uv(32, 8).cuboid(4, -8, -4, 4, 2, 0), ModelTransform.NONE);
		head.addChild("wrap", ModelPartBuilder.create().uv(28, 12).cuboid(-1, -3, 2, 6, 0, 4), ModelTransform.NONE);
		ModelPartData body = root.getChild(EntityModelPartNames.BODY);
		body.addChild("rib", ModelPartBuilder.create().uv(14, 24).cuboid(-5, 4, -2, 1, 1, 0), ModelTransform.NONE);
		body.addChild("waist", ModelPartBuilder.create().uv(12, 30).cuboid(1, 12, -2, 2, 2, 0), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-1, -2, -1, 2, 12, 2), ModelTransform.pivot(-5, 2, 0));
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1, -2, -1, 2, 12, 2), ModelTransform.pivot(5, 2, 0));
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-1, 0, -1, 2, 12, 2), ModelTransform.pivot(-2, 12, 0));
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-1, 0, -1, 2, 12, 2), ModelTransform.pivot(2, 12, 0));
		return TexturedModelData.of(modelData, 64, 32);
	}
	@Override
	public void animateModel(SunkenSkeletonEntity state, float f, float g, float h) {
		super.animateModel(state, f, g, h);
		boolean showFans;
		ItemStack stack = state.getEquippedStack(EquipmentSlot.HEAD);
		if (state.isSheared()) showFans = false;
		else if (stack.isIn(ItemTags.HEAD_ARMOR)) showFans = false;
		else showFans = !(stack.getItem() instanceof BlockItem);
		for (ModelPart part : this.fans) part.visible = showFans;
	}
}
