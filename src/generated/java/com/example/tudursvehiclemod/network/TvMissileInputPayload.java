package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: raw (pre-vanilla-clamp) mouse yaw/pitch deltas accumulated since the last tick, while directly steering a TV-guided missile (see entity.projectile.VehicleProjectileEntity's own tudursvehiclemod$updateTvMissileControl() doc) - same convention as AircraftOrientationInputPayload's own doc. */
public record TvMissileInputPayload(float yawDelta, float pitchDelta) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<TvMissileInputPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "tv_missile_input"));

	public static final StreamCodec<RegistryFriendlyByteBuf, TvMissileInputPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, TvMissileInputPayload::yawDelta,
			ByteBufCodecs.FLOAT, TvMissileInputPayload::pitchDelta,
			TvMissileInputPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
