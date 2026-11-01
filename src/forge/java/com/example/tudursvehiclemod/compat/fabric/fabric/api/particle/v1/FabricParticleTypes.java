package com.example.tudursvehiclemod.compat.fabric.fabric.api.particle.v1;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

public final class FabricParticleTypes {
	private FabricParticleTypes() {
	}

	public static SimpleParticleType simple(boolean alwaysSpawn) {
		return new SimpleParticleType(alwaysSpawn);
	}

	/** 1.20.1 particle types use a Codec plus a ParticleOptions.Deserializer instead of MapCodec + StreamCodec. */
	@SuppressWarnings("deprecation")
	public static <T extends ParticleOptions> ParticleType<T> complex(boolean alwaysSpawn, ParticleOptions.Deserializer<T> deserializer, Codec<T> codec) {
		return new ParticleType<>(alwaysSpawn, deserializer) {
			@Override
			public Codec<T> codec() {
				return codec;
			}
		};
	}
}
