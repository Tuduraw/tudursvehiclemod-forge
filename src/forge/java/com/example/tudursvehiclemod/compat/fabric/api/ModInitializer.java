package com.example.tudursvehiclemod.compat.fabric.api;

/** Kept so the upstream entry point classes compile unchanged; Forge calls them from the @Mod class. */
public interface ModInitializer {
	void onInitialize();
}
