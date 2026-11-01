package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: sent exactly once by AbstractVehicleEntity's own onSpawnPacket() override, the moment the client actually finishes loading that specific vehicle entity (covers both a fresh world join and an ordinary chunk reload identically, since both trigger a genuine client-side spawn) - asks the server to resend this vehicle's own passenger/seat-assignment data to just this one requesting player, replacing an earlier server-side polling approach that ran unconditionally every tick for every vehicle regardless of whether anyone actually needed it. */
public record RequestSeatResyncPayload(int vehicleEntityId) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<RequestSeatResyncPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "request_seat_resync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, RequestSeatResyncPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, RequestSeatResyncPayload::vehicleEntityId,
			RequestSeatResyncPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
