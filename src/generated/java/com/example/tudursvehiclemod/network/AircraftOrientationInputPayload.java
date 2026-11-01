package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: raw (pre-vanilla-clamp) mouse yaw/pitch deltas accumulated since the last tick, while piloting an aircraft. */
public record AircraftOrientationInputPayload(float yawDelta, float pitchDelta) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<AircraftOrientationInputPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "aircraft_orientation_input"));

	public static final StreamCodec<RegistryFriendlyByteBuf, AircraftOrientationInputPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, AircraftOrientationInputPayload::yawDelta,
			ByteBufCodecs.FLOAT, AircraftOrientationInputPayload::pitchDelta,
			AircraftOrientationInputPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
