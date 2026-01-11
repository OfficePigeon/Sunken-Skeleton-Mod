package fun.wich;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;

import java.util.EnumSet;

public class SunkenSkeletonEntity extends SkeletonEntity implements Shearable {
	private static final TrackedData<Boolean> SHEARED = DataTracker.registerData(SunkenSkeletonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Integer> VARIANT = DataTracker.registerData(SunkenSkeletonEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected boolean targetingUnderwater;
	protected final SwimNavigation waterNavigation;
	protected final MobNavigation landNavigation;
	public SunkenSkeletonEntity(EntityType<? extends SunkenSkeletonEntity> entityType, World world) {
		super(entityType, world);
		this.moveControl = new DrownedMoveControl(this);
		this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
		this.waterNavigation = new SwimNavigation(this, world);
		this.landNavigation = new MobNavigation(this, world);
	}
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData) {
		entityData = super.initialize(world, difficulty, spawnReason, entityData);
		this.setVariant(this.random.nextInt(SunkenSkeletonVariant.values().length));
		return entityData;
	}
	@Override
	protected void initGoals() {
		//Skeleton
		this.goalSelector.add(2, new AvoidSunlightGoal(this));
		this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0));
		this.goalSelector.add(3, new FleeEntityGoal<>(this, WolfEntity.class, 6, 1, 1.2));
		//Drowned
		this.goalSelector.add(5, new LeaveWaterGoal(this, 1.0));
		this.goalSelector.add(6, new TargetAboveWaterGoal(this, 1.0, this.getEntityWorld().getSeaLevel()));
		//Skeleton
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8));
		this.goalSelector.add(6, new LookAroundGoal(this));
		//Drowned
		this.goalSelector.add(1, new WanderAroundOnSurfaceGoal(this, 1.0));
		this.goalSelector.add(7, new WanderAroundGoal(this, 1.0));
		//Skeleton
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
		//Drowned
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, AxolotlEntity.class, true, false));
		//Skeleton
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
	}
	@Override protected EntityNavigation createNavigation(World world) { return new AmphibiousSwimNavigation(this, world); }
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
		if (persistentProjectileEntity instanceof SunkenSkeletonWaterDragControllable dragControllable) dragControllable.WaterDragControllable_SetDragInWater(1f);
		return persistentProjectileEntity;
	}
	@Override public boolean canSpawn(WorldView world) { return world.doesNotIntersectEntities(this); }
	public static boolean canSpawn(EntityType<? extends SunkenSkeletonEntity> ignoredType, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		boolean spawner = SpawnReason.isAnySpawner(reason);
		if (world.getDifficulty() != Difficulty.PEACEFUL && (spawner || world.getFluidState(pos.down()).isIn(FluidTags.WATER))) {
			if (SpawnReason.isTrialSpawner(reason) || isSpawnDark(world, pos, random)) {
				return random.nextInt(15) == 0 && (spawner || isValidSpawnDepth(world, pos));
			}
		}
		return false;
	}
	public static boolean isValidSpawnDepth(WorldAccess world, BlockPos pos) { return pos.getY() < world.getSeaLevel() - 5; }
	@Override public boolean isPushedByFluids() { return !this.isSwimming(); }
	public boolean isTargetingUnderwater() {
		if (this.targetingUnderwater) return true;
		else {
			LivingEntity livingEntity = this.getTarget();
			return livingEntity != null && livingEntity.isTouchingWater();
		}
	}
	public void setTargetingUnderwater(boolean targetingUnderwater) { this.targetingUnderwater = targetingUnderwater; }
	@Override
	public void travel(Vec3d movementInput) {
		if (this.isSubmergedInWater() && this.isTargetingUnderwater()) {
			this.updateVelocity(0.01F, movementInput);
			this.move(MovementType.SELF, this.getVelocity());
			this.setVelocity(this.getVelocity().multiply(0.9));
		}
		else super.travel(movementInput);
	}
	@Override
	public void updateSwimming() {
		if (!this.getWorld().isClient) {
			if (this.canMoveVoluntarily() && this.isTouchingWater() && this.isTargetingUnderwater()) {
				this.navigation = this.waterNavigation;
				this.setSwimming(true);
			}
			else {
				this.navigation = this.landNavigation;
				this.setSwimming(false);
			}
		}
	}
	protected boolean hasFinishedCurrentPath() {
		Path path = this.getNavigation().getCurrentPath();
		if (path != null) {
			BlockPos pos = path.getTarget();
			if (pos != null) return this.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 4.0;
		}
		return false;
	}
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
			LootContextParameterSet loot = new LootContextParameterSet.Builder(serverWorld)
					.add(LootContextParameters.ORIGIN, this.getPos())
					.add(LootContextParameters.THIS_ENTITY, this)
					.build(LootContextTypes.SHEARING);
			for (ItemStack itemStack : lootTable.generateLoot(loot)) this.dropStack(itemStack, this.getHeight());
		}
		this.setSheared(true);
	}
	@Override public boolean isShearable() { return !this.isSheared() && this.isAlive(); }

	protected static class DrownedMoveControl extends MoveControl {
		private final SunkenSkeletonEntity mob;
		public DrownedMoveControl(SunkenSkeletonEntity mob) {
			super(mob);
			this.mob = mob;
		}
		@Override
		public void tick() {
			LivingEntity livingEntity = this.mob.getTarget();
			if (this.mob.isTargetingUnderwater() && this.mob.isTouchingWater()) {
				if (livingEntity != null && livingEntity.getY() > this.mob.getY() || this.mob.targetingUnderwater) {
					this.mob.setVelocity(this.mob.getVelocity().add(0, 0.002, 0));
				}
				if (this.state != MoveControl.State.MOVE_TO || this.mob.getNavigation().isIdle()) {
					this.mob.setMovementSpeed(0);
					return;
				}
				double d = this.targetX - this.mob.getX();
				double e = this.targetY - this.mob.getY();
				double f = this.targetZ - this.mob.getZ();
				double g = Math.sqrt(d * d + e * e + f * f);
				e /= g;
				float h = (float) (MathHelper.atan2(f, d) * 57.295776f) - 90;
				this.mob.setYaw(this.wrapDegrees(this.mob.getYaw(), h, 90));
				this.mob.bodyYaw = this.mob.getYaw();
				float i = (float) (this.speed * this.mob.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
				float j = MathHelper.lerp(0.125F, this.mob.getMovementSpeed(), i);
				this.mob.setMovementSpeed(j);
				this.mob.setVelocity(this.mob.getVelocity().add(j * d * 0.005, j * e * 0.1, j * f * 0.005));
			}
			else {
				if (!this.mob.isOnGround()) this.mob.setVelocity(this.mob.getVelocity().add(0, -0.008, 0));
				super.tick();
			}
		}
	}
	protected static class LeaveWaterGoal extends MoveToTargetPosGoal {
		public LeaveWaterGoal(SunkenSkeletonEntity drowned, double speed) {
			super(drowned, speed, 8, 2);
		}
		@Override
		public boolean canStart() {
			return super.canStart()
					&& !this.mob.getEntityWorld().isDay()
					&& this.mob.isTouchingWater()
					&& this.mob.getY() >= this.mob.getEntityWorld().getSeaLevel() - 3;
		}
		@Override public boolean shouldContinue() { return super.shouldContinue(); }
		@Override
		protected boolean isTargetPos(WorldView world, BlockPos pos) {
			BlockPos blockPos = pos.up();
			return world.isAir(blockPos) && world.isAir(blockPos.up()) && world.getBlockState(pos).hasSolidTopSurface(world, pos, this.mob);
		}
		@Override
		public void start() {
			SunkenSkeletonEntity sunkenSkeletonEntity = (SunkenSkeletonEntity)this.mob;
			sunkenSkeletonEntity.setTargetingUnderwater(false);
			sunkenSkeletonEntity.navigation = sunkenSkeletonEntity.landNavigation;
			super.start();
		}
	}
	protected static class TargetAboveWaterGoal extends Goal {
		private final SunkenSkeletonEntity mob;
		private final double speed;
		private final int minY;
		private boolean foundTarget;
		public TargetAboveWaterGoal(SunkenSkeletonEntity mob, double speed, int minY) {
			this.mob = mob;
			this.speed = speed;
			this.minY = minY;
		}
		@Override
		public boolean canStart() {
			return !this.mob.getEntityWorld().isDay() && this.mob.isTouchingWater() && this.mob.getY() < this.minY - 2;
		}
		@Override public boolean shouldContinue() { return this.canStart() && !this.foundTarget; }
		@Override
		public void tick() {
			if (this.mob.getY() < this.minY - 1 && (this.mob.getNavigation().isIdle() || this.mob.hasFinishedCurrentPath())) {
				Vec3d vec3d = NoPenaltyTargeting.findTo(this.mob, 4, 8, new Vec3d(this.mob.getX(), this.minY - 1, this.mob.getZ()), (float) (Math.PI / 2));
				if (vec3d == null) {
					this.foundTarget = true;
					return;
				}
				this.mob.getNavigation().startMovingTo(vec3d.x, vec3d.y, vec3d.z, this.speed);
			}
		}
		@Override
		public void start() {
			this.mob.setTargetingUnderwater(true);
			this.foundTarget = false;
		}
		@Override public void stop() { this.mob.setTargetingUnderwater(false); }
	}
	protected static class WanderAroundOnSurfaceGoal extends Goal {
		private final SunkenSkeletonEntity mob;
		private double x;
		private double y;
		private double z;
		private final double speed;
		private final World world;
		public WanderAroundOnSurfaceGoal(SunkenSkeletonEntity mob, double speed) {
			this.mob = mob;
			this.speed = speed;
			this.world = mob.getEntityWorld();
			this.setControls(EnumSet.of(Goal.Control.MOVE));
		}
		@Override
		public boolean canStart() {
			if (!this.world.isDay() || this.mob.isTouchingWater()) return false;
			else {
				Vec3d vec3d = this.getWanderTarget();
				if (vec3d == null) return false;
				else {
					this.x = vec3d.x;
					this.y = vec3d.y;
					this.z = vec3d.z;
					return true;
				}
			}
		}
		@Override public boolean shouldContinue() { return !this.mob.getNavigation().isIdle(); }
		@Override public void start() { this.mob.getNavigation().startMovingTo(this.x, this.y, this.z, this.speed); }
		private Vec3d getWanderTarget() {
			Random random = this.mob.getRandom();
			BlockPos blockPos = this.mob.getBlockPos();
			for (int i = 0; i < 10; i++) {
				BlockPos blockPos2 = blockPos.add(random.nextInt(20) - 10, 2 - random.nextInt(8), random.nextInt(20) - 10);
				if (this.world.getBlockState(blockPos2).isOf(Blocks.WATER)) {
					return Vec3d.ofBottomCenter(blockPos2);
				}
			}
			return null;
		}
	}
}
