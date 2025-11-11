package fun.wich.mixin;

import fun.wich.SunkenSkeletonEntity;
import fun.wich.SunkenSkeletonMod;
import fun.wich.SunkenSkeletonVariant;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.data.DataTracker;
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
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkeletonEntity.class)
public abstract class SkeletonEntityMixin extends AbstractSkeletonEntity {
	@Unique
	private int inWaterTime;
	@Unique
	private int ticksUntilWaterConversion;

	protected SkeletonEntityMixin(EntityType<? extends AbstractSkeletonEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method="initDataTracker", at=@At("TAIL"))
	protected void Mixin_InitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
		builder.add(SunkenSkeletonMod.SKELETON_CONVERTING_IN_WATER, false);
	}
	@Unique
	public boolean Mixin_IsConvertingInWater() {
		return this.getDataTracker().get(SunkenSkeletonMod.SKELETON_CONVERTING_IN_WATER);
	}
	@Inject(method="isShaking", at=@At("HEAD"), cancellable = true)
	public void Mixin_IsShaking(CallbackInfoReturnable<Boolean> cir) {
		if (this.Mixin_IsConvertingInWater()) cir.setReturnValue(true);
	}
	@Inject(method="writeCustomData", at=@At("TAIL"))
	protected void Mixin_WriteCustomData(WriteView view, CallbackInfo ci) {
		view.putInt("InWaterTime", this.isTouchingWater() ? this.inWaterTime : -1);
		view.putInt("WaterConversionTime", this.Mixin_IsConvertingInWater() ? this.ticksUntilWaterConversion : -1);
	}
	@Inject(method="readCustomData", at=@At("TAIL"))
	protected void Mixin_ReadCustomData(ReadView view, CallbackInfo ci) {
		this.inWaterTime = view.getInt("InWaterTime", -1);
		int i = view.getInt("WaterConversionTime", -1);
		if (i != -1) this.Mixin_SetTicksUntilWaterConversion(i);
		this.Mixin_SetConvertingInWater(false);
	}
	@Unique
	private void Mixin_SetTicksUntilWaterConversion(int ticksUntilConversion) {
		this.ticksUntilWaterConversion = ticksUntilConversion;
		this.Mixin_SetConvertingInWater(true);
	}
	@Unique
	private void Mixin_SetConvertingInWater(boolean converting) {
		this.getDataTracker().set(SunkenSkeletonMod.SKELETON_CONVERTING_IN_WATER, converting);
	}
	@Inject(method="tick", at=@At("HEAD"))
	public void Mixin_Tick(CallbackInfo ci) {
		if ((Object)this instanceof SunkenSkeletonEntity) return;
		World world = this.getEntityWorld();
		if (!world.isClient() && this.isAlive() && !this.isAiDisabled()) {
			BlockPos pos = this.getBlockPos();
			RegistryEntry<Biome> biome = world.getBiome(pos);
			if (this.isSubmergedIn(FluidTags.WATER) && biome == BiomeKeys.WARM_OCEAN) {
				if (this.Mixin_IsConvertingInWater()) {
					--this.ticksUntilWaterConversion;
					if (this.ticksUntilWaterConversion < 0) this.Mixin_ConvertToSunkenSkeleton();
				}
				else {
					++this.inWaterTime;
					if (this.inWaterTime >= 140) this.Mixin_SetTicksUntilWaterConversion(300);
				}
			}
			else {
				this.inWaterTime = -1;
				this.Mixin_SetConvertingInWater(false);
			}
		}
	}
	@Unique
	protected void Mixin_ConvertToSunkenSkeleton() {
		this.convertTo(SunkenSkeletonMod.SUNKEN_SKELETON, EntityConversionContext.create(this, true, true), sunkenSkeleton -> {
			if (!this.isSilent()) {
				this.getEntityWorld().playSound(null, this.getBlockPos(), SunkenSkeletonMod.ENTITY_SKELETON_CONVERTED_TO_SUNKEN_SKELETON, SoundCategory.HOSTILE, 2.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
			}
			sunkenSkeleton.setVariant(random.nextInt(SunkenSkeletonVariant.values().length));
		});
	}
}
