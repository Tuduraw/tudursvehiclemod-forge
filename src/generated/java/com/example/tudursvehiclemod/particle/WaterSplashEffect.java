package com.example.tudursvehiclemod.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;

/** This mod's own custom water-splash/column visual
 * effect (see client.particle.WaterSplashParticle for the actual
 * rendering), used for both the large water column on an explosion at/in
 * water, and the small splash on a plain projectile's own first contact
 * with the water surface - scale carries how big a given spawn should be
 * (see AbstractVehicleEntity/VehicleProjectileEntity's own call sites for
 * how that's actually computed in each of those two cases). Same
 * Codec/PacketCodec-based approach as ExplosionFlashEffect (see that
 * record's own doc for why this is necessary rather than smuggling scale
 * through spawnParticles()'s own velocity parameters instead). */
public record WaterSplashEffect(float scale) implements ParticleOptions {

	public static final MapCodec<WaterSplashEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.FLOAT.fieldOf("scale").forGetter(WaterSplashEffect::scale)
	).apply(instance, WaterSplashEffect::new));

	/** Forge 1.20.1: particle types take a Codec and a ParticleOptions.Deserializer (no MapCodec/StreamCodec). */
	public static final Codec<WaterSplashEffect> CODEC_1201 = CODEC.codec();

	@SuppressWarnings("deprecation")
	public static final ParticleOptions.Deserializer<WaterSplashEffect> DESERIALIZER = new ParticleOptions.Deserializer<>() {
		@Override
		public WaterSplashEffect fromCommand(ParticleType<WaterSplashEffect> type, com.mojang.brigadier.StringReader reader) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
			reader.expect(' ');
			return new WaterSplashEffect(reader.readFloat());
		}

		@Override
		public WaterSplashEffect fromNetwork(ParticleType<WaterSplashEffect> type, net.minecraft.network.FriendlyByteBuf buf) {
			return new WaterSplashEffect(buf.readFloat());
		}
	};

	@Override
	public void writeToNetwork(net.minecraft.network.FriendlyByteBuf buf) {
		buf.writeFloat(this.scale());
	}

	@Override
	public String writeToString() {
		return net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.scale();
	}

	@Override
	public ParticleType<WaterSplashEffect> getType() {
		return com.example.tudursvehiclemod.registry.ModParticleTypes.WATER_SPLASH;
	}
}
