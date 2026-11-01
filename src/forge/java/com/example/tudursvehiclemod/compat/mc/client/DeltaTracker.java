package com.example.tudursvehiclemod.compat.mc.client;

/** Forge 1.20.1 stand-in for 1.21's DeltaTracker: just carries the frame's partial tick. */
public record DeltaTracker(float partialTick) {
	public float getGameTimeDeltaPartialTick(boolean ignoreFrozen) {
		return partialTick;
	}

	public float getGameTimeDeltaTicks() {
		return partialTick;
	}
}
