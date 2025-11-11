package fun.wich.client;

import fun.wich.SunkenSkeletonMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ModClient implements ClientModInitializer {
	public static final EntityModelLayer SUNKEN_SKELETON = MakeModelLayer("sunken_skeleton", "main");
	private static EntityModelLayer MakeModelLayer(String id, String name) {
		return new EntityModelLayer(Identifier.of(SunkenSkeletonMod.MOD_ID, id), name);
	}
	private static EquipmentModelData<EntityModelLayer> registerEquipment(String id) {
		return new EquipmentModelData<>(MakeModelLayer(id, "helmet"), MakeModelLayer(id, "chestplate"), MakeModelLayer(id, "leggings"), MakeModelLayer(id, "boots"));
	}
	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(SUNKEN_SKELETON, SunkenSkeletonEntityModel::getTexturedModelData);
		EntityRendererFactories.register(SunkenSkeletonMod.SUNKEN_SKELETON, SunkenSkeletonEntityRenderer::new);
	}
}
