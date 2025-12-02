package fun.wich.client;

import fun.wich.SunkenSkeletonMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SunkenSkeletonsClient implements ClientModInitializer {
	public static final EntityModelLayer SUNKEN_SKELETON = new EntityModelLayer(Identifier.of(SunkenSkeletonMod.MOD_ID, "sunken_skeleton"), "main");
	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(SUNKEN_SKELETON, SunkenSkeletonEntityModel::getTexturedModelData);
		EntityRendererRegistry.register(SunkenSkeletonMod.SUNKEN_SKELETON, SunkenSkeletonEntityRenderer::new);
	}
}
