package com.example.tudursvehiclemod.forge.mixin;

import com.example.tudursvehiclemod.entity.AbstractVehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Forge 1.20.1 version of ExplosionKnockbackMixin (1.21.11: ExplosionDamageCalculator.getKnockbackMultiplier, which 1.20.1
 * doesn't have): explosions still damage vehicles, but don't push them - the knockback velocity Explosion.explode() adds is
 * skipped for an AbstractVehicleEntity. */
@Mixin(Explosion.class)
public abstract class ExplosionKnockbackForgeMixin {
	@Redirect(method = "explode()V", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
	private void tudursvehiclemod$skipVehicleKnockback(Entity entity, Vec3 velocity) {
		if (!(entity instanceof AbstractVehicleEntity)) {
			entity.setDeltaMovement(velocity);
		}
	}
}
