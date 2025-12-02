package fun.wich.mixin;

import fun.wich.SunkenSkeletonMod;
import fun.wich.SunkenSkeletonVariant;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkeletonEntity.class)
public abstract class SunkenSkeletons_SkeletonEntityMixin extends AbstractSkeletonEntity {
	@Unique @SuppressWarnings("WrongEntityDataParameterClass")
	private static final TrackedData<Boolean> SKELETON_CONVERTING_IN_WATER = DataTracker.registerData(SkeletonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	@Unique
	private int inWaterTime;
	@Unique
	private int ticksUntilWaterConversion;
	protected SunkenSkeletons_SkeletonEntityMixin(EntityType<? extends AbstractSkeletonEntity> entityType, World world) {
		super(entityType, world);
	}
	@Inject(method="initDataTracker", at=@At("TAIL"))
	protected void Mixin_InitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
		builder.add(SKELETON_CONVERTING_IN_WATER, false);
	}
	@Inject(method="isShaking", at=@At("HEAD"), cancellable = true)
	public void Mixin_IsShaking(CallbackInfoReturnable<Boolean> cir) {
		if (this.getDataTracker().get(SKELETON_CONVERTING_IN_WATER)) cir.setReturnValue(true);
	}
	@Inject(method="writeCustomData", at=@At("TAIL"))
	protected void Mixin_WriteCustomData(WriteView view, CallbackInfo ci) {
		view.putInt("InWaterTime", this.isTouchingWater() ? this.inWaterTime : -1);
		view.putInt("WaterConversionTime", this.getDataTracker().get(SKELETON_CONVERTING_IN_WATER) ? this.ticksUntilWaterConversion : -1);
	}
	@Inject(method="readCustomData", at=@At("TAIL"))
	protected void Mixin_ReadCustomData(ReadView view, CallbackInfo ci) {
		this.inWaterTime = view.getInt("InWaterTime", -1);
		int i = view.getInt("WaterConversionTime", -1);
		if (i < 0) this.ticksUntilWaterConversion = -1;
		this.getDataTracker().set(SKELETON_CONVERTING_IN_WATER, false);
	}
	@Inject(method="tick", at=@At("HEAD"))
	public void Mixin_Tick(CallbackInfo ci) {
		if (this.getType() != EntityType.SKELETON) return; //only default skeletons can convert
		World world = this.getEntityWorld();
		if (!world.isClient() && this.isAlive() && !this.isAiDisabled()) {
			BlockPos pos = this.getBlockPos();
			RegistryEntry<Biome> biome = world.getBiome(pos);
			if (this.isSubmergedIn(FluidTags.WATER) && biome.isIn(SunkenSkeletonMod.TAG_SKELETONS_CAN_BECOME_SUNKEN)) {
				if (this.getDataTracker().get(SKELETON_CONVERTING_IN_WATER)) {
					--this.ticksUntilWaterConversion;
					if (this.ticksUntilWaterConversion < 0) {
						this.convertTo(SunkenSkeletonMod.SUNKEN_SKELETON, EntityConversionContext.create(this, true, true), sunkenSkeleton -> {
							if (!this.isSilent()) {
								this.getEntityWorld().playSound(null, this.getBlockPos(), SunkenSkeletonMod.ENTITY_SKELETON_CONVERTED_TO_SUNKEN_SKELETON, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1);
							}
							sunkenSkeleton.setVariant(random.nextInt(SunkenSkeletonVariant.values().length));
						});
					}
				}
				else {
					++this.inWaterTime;
					if (this.inWaterTime >= 140) {
						this.ticksUntilWaterConversion = 300;
						this.getDataTracker().set(SKELETON_CONVERTING_IN_WATER, true);
					}
				}
			}
			else {
				this.inWaterTime = -1;
				this.getDataTracker().set(SKELETON_CONVERTING_IN_WATER, false);
			}
		}
	}
}
