package com.example.tudursvehiclemod.entity;

import net.minecraft.resources.ResourceLocation;

/** Implemented by any entity that should be drawn by the generic OBJ-based renderer (MeshedEntityRenderer, client-side). */
public interface MeshedEntity {
	ResourceLocation getModelId();

	ResourceLocation getTextureId();

	float getScale();

	/** Purely cosmetic animation state (rotor spin, wheel spin, propeller RPM,..). */
	float getAnimationPhase(float tickDelta);

	/** Purely cosmetic bank/tilt angle in degrees, applied as an extra roll rotation by the renderer. Defaults to 0 (no roll). */
	default float getRoll(float tickDelta) {
		return 0f;
	}
}
