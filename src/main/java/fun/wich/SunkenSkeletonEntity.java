package fun.wich;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class SunkenSkeletonEntity extends SkeletonEntity {
	private static final TrackedData<Integer> VARIANT = DataTracker.registerData(SunkenSkeletonEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public SunkenSkeletonEntity(EntityType<? extends SunkenSkeletonEntity> entityType, World world) {
		super(entityType, world);
	}
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData) {
		entityData = super.initialize(world, difficulty, spawnReason, entityData);
		this.setVariant(this.random.nextInt(SunkenSkeletonVariant.values().length));
		return entityData;
	}
	public int getVariant() { return this.dataTracker.get(VARIANT); }
	public void setVariant(int variant) {
		this.dataTracker.set(VARIANT, variant);
	}
	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(VARIANT, 0);
	}

	@Override
	protected void writeCustomData(WriteView view) {
		super.writeCustomData(view);
		view.putInt("Variant", this.getVariant());
	}

	@Override
	protected void readCustomData(ReadView view) {
		super.readCustomData(view);
		this.setVariant(view.getInt("Variant", 0));
	}

	@Override protected SoundEvent getAmbientSound() { return SunkenSkeletonMod.ENTITY_SUNKEN_SKELETON_AMBIENT; }
	@Override protected SoundEvent getDeathSound() { return SunkenSkeletonMod.ENTITY_SUNKEN_SKELETON_DEATH; }
	@Override protected SoundEvent getHurtSound(DamageSource source) { return SunkenSkeletonMod.ENTITY_SUNKEN_SKELETON_HURT; }
	@Override protected void playStepSound(BlockPos pos, BlockState state) { this.playSound(SunkenSkeletonMod.ENTITY_SUNKEN_SKELETON_STEP, 0.15f, 1.0f); }

	@Override
	protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, ItemStack shotFrom) {
		PersistentProjectileEntity persistentProjectileEntity = super.createArrowProjectile(arrow, damageModifier, shotFrom);
		if (persistentProjectileEntity instanceof WaterDragControllable dragControllable) dragControllable.WaterDragControllable_SetDragInWater(0.99f);
		return persistentProjectileEntity;
	}

	public static boolean canSpawn(EntityType<SunkenSkeletonEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
		if (!world.getFluidState(pos.down()).isIn(FluidTags.WATER) && !SpawnReason.isAnySpawner(spawnReason)) return false;
		RegistryEntry<Biome> registryEntry = world.getBiome(pos);
		boolean bl = world.getDifficulty() != Difficulty.PEACEFUL
				&& (SpawnReason.isTrialSpawner(spawnReason) || isSpawnDark(world, pos, random))
				&& (SpawnReason.isAnySpawner(spawnReason) || world.getFluidState(pos).isIn(FluidTags.WATER));
		if (!bl || !SpawnReason.isAnySpawner(spawnReason) && spawnReason != SpawnReason.REINFORCEMENT) {
			return (registryEntry.matchesKey(BiomeKeys.LUKEWARM_OCEAN) || registryEntry.matchesKey(BiomeKeys.WARM_OCEAN) || registryEntry.matchesKey(BiomeKeys.JUNGLE))
					? random.nextInt(15) == 0 && bl
					: random.nextInt(40) == 0 && isValidSpawnDepth(world, pos) && bl;
		}
		else return true;
	}
	public static boolean isValidSpawnDepth(WorldAccess world, BlockPos pos) { return pos.getY() < world.getSeaLevel() - 5; }

	@Override public boolean isPushedByFluids() { return !this.isSwimming(); }
	@Override public boolean canBreatheInWater() { return true; }
}
