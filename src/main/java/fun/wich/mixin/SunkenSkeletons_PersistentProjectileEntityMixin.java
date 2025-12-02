package fun.wich.mixin;

import fun.wich.WaterDragControllable;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentProjectileEntity.class)
public class SunkenSkeletons_PersistentProjectileEntityMixin implements WaterDragControllable {
	@Unique
	private float waterDrag = Float.NaN;
	@Inject(method="getDragInWater", at=@At("HEAD"), cancellable=true)
	private void OverrideGetDragInWater(CallbackInfoReturnable<Float> cir) {
		if (!Float.isNaN(waterDrag)) cir.setReturnValue(waterDrag);
	}
	@Override public float WaterDragControllable_GetDragInWater() { return this.waterDrag; }
	@Override public void WaterDragControllable_SetDragInWater(float drag) { this.waterDrag = drag; }
}
