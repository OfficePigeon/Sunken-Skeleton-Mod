package fun.wich;

import fun.wich.mixin.LootTablesMixin;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

import java.util.function.Function;

public class SunkenSkeletonMod implements ModInitializer {
	public static final String MOD_ID = "wich";

	public static final SoundEvent ENTITY_SUNKEN_SKELETON_AMBIENT = register("entity.sunken_skeleton.ambient");
	public static final SoundEvent ENTITY_SUNKEN_SKELETON_DEATH = register("entity.sunken_skeleton.death");
	public static final SoundEvent ENTITY_SUNKEN_SKELETON_HURT = register("entity.sunken_skeleton.hurt");
	public static final SoundEvent ENTITY_SUNKEN_SKELETON_STEP = register("entity.sunken_skeleton.step");
	public static final SoundEvent ENTITY_SUNKEN_SKELETON_SHEAR = register("entity.sunken_skeleton.shear");
	public static final SoundEvent ENTITY_SKELETON_CONVERTED_TO_SUNKEN_SKELETON = register("entity.skeleton.converted_to_sunken_skeleton");
	public static final SoundEvent ENTITY_PARROT_IMITATE_SUNKEN_SKELETON = register("entity.parrot.imitate.sunken_skeleton");

	private static SoundEvent register(String path) {
		Identifier id = Identifier.of(MOD_ID, path);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static final TagKey<Biome> TAG_SPAWNS_SUNKEN_SKELETONS = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "spawns_sunken_skeletons"));
	public static final TagKey<Biome> TAG_SKELETONS_CAN_BECOME_SUNKEN = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "skeletons_can_become_sunken"));

	public static final EntityType<SunkenSkeletonEntity> SUNKEN_SKELETON = register(
			"sunken_skeleton",
			EntityType.Builder.create(SunkenSkeletonEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.6F, 1.99F)
					.eyeHeight(1.74F)
					.vehicleAttachment(-0.7F)
					.maxTrackingRange(8)
					.notAllowedInPeaceful()
	);
	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> type) {
		RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, name));
		EntityType<T> entityType = type.build(key);
		Registry.register(Registries.ENTITY_TYPE, key, entityType);
		return entityType;
	}

	public static final Item SUNKEN_SKELETON_SPAWN_EGG = register("sunken_skeleton_spawn_egg", SpawnEggItem::new, new Item.Settings().spawnEgg(SUNKEN_SKELETON));
	public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
		Item item = itemFactory.apply(settings.registryKey(key));
		Registry.register(Registries.ITEM, key, item);
		return item;
	}
	public static final RegistryKey<LootTable> SUNKEN_SKELETON_SHEARING = LootTablesMixin.registerLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(MOD_ID, "shearing/sunken_skeleton")));

	@Override
	public void onInitialize() {
		//Attributes
		FabricDefaultAttributeRegistry.register(SUNKEN_SKELETON, SunkenSkeletonEntity.createAbstractSkeletonAttributes());
		//Spawning
		SpawnRestriction.register(SUNKEN_SKELETON, SpawnLocationTypes.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SunkenSkeletonEntity::canSpawn);
		BiomeModifications.addSpawn(BiomeSelectors.tag(TAG_SPAWNS_SUNKEN_SKELETONS),
				SpawnGroup.MONSTER, SUNKEN_SKELETON, 55, 1, 4);
		//Items
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(itemGroup -> itemGroup.add(SUNKEN_SKELETON_SPAWN_EGG));
	}
}