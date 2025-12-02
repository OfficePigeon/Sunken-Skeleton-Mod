package fun.wich;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.event.GameEvent;

public class SunkenSkeletonEntity extends SkeletonEntity implements Shearable {
	private static final TrackedData<Boolean> SHEARED = DataTracker.registerData(SunkenSkeletonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
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
	public boolean isSheared() { return this.dataTracker.get(SHEARED); }
	public void setSheared(boolean sheared) { this.dataTracker.set(SHEARED, sheared); }
	public int getVariant() { return this.dataTracker.get(VARIANT); }
	public void setVariant(int variant) { this.dataTracker.set(VARIANT, variant); }
	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(SHEARED, false);
		builder.add(VARIANT, 0);
	}
	@Override
	public void writeCustomDataToNbt(NbtCompound view) {
		super.writeCustomDataToNbt(view);
		view.putBoolean("sheared", this.isSheared());
		view.putInt("Variant", this.getVariant());
	}
	@Override
	public void readCustomDataFromNbt(NbtCompound view) {
		super.readCustomDataFromNbt(view);
		this.setSheared(view.contains("sheared") && view.getBoolean("sheared"));
		this.setVariant(view.contains("Variant") ? view.getInt("Variant") : 0);
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
	public static boolean canSpawn(EntityType<SunkenSkeletonEntity> ignoredType, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
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
	@SuppressWarnings("deprecation")
	public static boolean isValidSpawnDepth(WorldAccess world, BlockPos pos) { return pos.getY() < world.getSeaLevel() - 5; }
	@Override public boolean isPushedByFluids() { return !this.isSwimming(); }
	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (itemStack.isOf(Items.SHEARS) && this.isShearable()) {
			this.sheared(SoundCategory.PLAYERS);
			this.emitGameEvent(GameEvent.SHEAR, player);
			if (!this.getWorld().isClient) itemStack.damage(1, player, getSlotForHand(hand));
			return ActionResult.SUCCESS;
		}
		else return super.interactMob(player, hand);
	}
	@Override
	public void sheared(SoundCategory shearedSoundCategory) {
		World world = this.getEntityWorld();
		world.playSoundFromEntity(null, this, SunkenSkeletonMod.ENTITY_SUNKEN_SKELETON_SHEAR, shearedSoundCategory, 1, 1);
		if (world instanceof ServerWorld serverWorld) {
			LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(SunkenSkeletonMod.SUNKEN_SKELETON_SHEARING);
			LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverWorld)
					.add(LootContextParameters.ORIGIN, this.getPos())
					.add(LootContextParameters.THIS_ENTITY, this)
					.build(LootContextTypes.SHEARING);
			for (ItemStack itemStack : lootTable.generateLoot(lootContextParameterSet)) {
				this.dropStack(itemStack, this.getHeight());
			}
		}
		this.setSheared(true);
	}
	@Override public boolean isShearable() { return !this.isSheared() && this.isAlive(); }
}
