package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: "open your own drone control stick naming screen for this vehicle" - sent to the interacting player specifically, the instant they right-click a vehicle while holding item.DroneControlStickItem (see entity.AbstractVehicleEntity's own interact() doc for where that dispatch happens). Kept as a server round-trip (rather than the client opening the screen directly within its own interact() call) so this common-code entity class never needs to reference the client-only screen class at all - the client-side receiver that actually opens client.screen.DroneStickNameScreen lives entirely in client.VehicleModClient, same convention as this project's other drone-config "open" payloads (DroneWaypointsOpenPayload, DroneRouteBookOpenPayload). vehicleId is sent as its String form, same convention as DroneStickRegisterPayload's own doc explains. */
public record DroneStickNameOpenPayload(boolean mainHand, String vehicleId, String suggestedName) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DroneStickNameOpenPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_stick_name_open"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DroneStickNameOpenPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, DroneStickNameOpenPayload::mainHand,
			ByteBufCodecs.STRING_UTF8, DroneStickNameOpenPayload::vehicleId,
			ByteBufCodecs.STRING_UTF8, DroneStickNameOpenPayload::suggestedName,
			DroneStickNameOpenPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
