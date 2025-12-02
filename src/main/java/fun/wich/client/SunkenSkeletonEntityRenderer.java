package fun.wich.client;

import fun.wich.SunkenSkeletonEntity;
import fun.wich.SunkenSkeletonVariant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SunkenSkeletonEntityRenderer extends BipedEntityRenderer<SunkenSkeletonEntity, SkeletonEntityModel<SunkenSkeletonEntity>> {
	public SunkenSkeletonEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new SunkenSkeletonEntityModel(context.getPart(SunkenSkeletonsClient.SUNKEN_SKELETON)), 0.5F);
		this.addFeature(
		new ArmorFeatureRenderer<>(
this, new SkeletonEntityModel<>(context.getPart(EntityModelLayers.SKELETON_INNER_ARMOR)), new SkeletonEntityModel<>(context.getPart(EntityModelLayers.SKELETON_OUTER_ARMOR)), context.getModelManager()
		));
	}
	@Override public Identifier getTexture(SunkenSkeletonEntity state) { return SunkenSkeletonVariant.get(state).texture; }
	@Override protected boolean isShaking(SunkenSkeletonEntity state) { return state.isShaking(); }
}