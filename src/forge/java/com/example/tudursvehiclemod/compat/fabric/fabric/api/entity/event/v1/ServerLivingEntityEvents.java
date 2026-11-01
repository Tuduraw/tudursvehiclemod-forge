package com.example.tudursvehiclemod.compat.fabric.fabric.api.entity.event.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** ALLOW_DAMAGE is checked from compat.ForgeEvents (LivingAttackEvent): false cancels the damage. */
public final class ServerLivingEntityEvents {
	private ServerLivingEntityEvents() {
	}

	@FunctionalInterface
	public interface AllowDamage {
		boolean allowDamage(LivingEntity entity, DamageSource source, float amount);
	}

	public static final Event<AllowDamage> ALLOW_DAMAGE = new Event<>(l -> (e, s, a) -> {
		for (AllowDamage x : l) {
			if (!x.allowDamage(e, s, a)) {
				return false;
			}
		}
		return true;
	});
}
