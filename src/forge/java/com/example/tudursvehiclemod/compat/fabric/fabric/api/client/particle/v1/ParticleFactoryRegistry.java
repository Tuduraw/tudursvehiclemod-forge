package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.particle.v1;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Collected here, registered in RegisterParticleProvidersEvent (compat.ForgeClientEvents). */
public final class ParticleFactoryRegistry {
	private static final ParticleFactoryRegistry INSTANCE = new ParticleFactoryRegistry();
	public final List<Consumer<net.minecraftforge.client.event.RegisterParticleProvidersEvent>> registrations = new ArrayList<>();

	public static ParticleFactoryRegistry getInstance() {
		return INSTANCE;
	}

	public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> factory) {
		registrations.add(e -> e.registerSpriteSet(type, factory));
	}

	@FunctionalInterface
	public interface PendingParticleFactory<T extends ParticleOptions> extends ParticleEngine.SpriteParticleRegistration<T> {
	}
}
