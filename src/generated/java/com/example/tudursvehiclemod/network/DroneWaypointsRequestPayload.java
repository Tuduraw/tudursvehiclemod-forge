package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "please send me the current route for the Drone Center at (x,y,z) so I can open its detailed settings screen" - sent when the player presses "Detailed Settings.." on the simple config screen. The server responds with DroneWaypointsOpenPayload (see that payload's own doc) once it has looked up the block's own current route - this round-trip exists because the simple config screen's own client-side state doesn't otherwise know the route (only the block entity itself does). */
public record DroneWaypointsRequestPayload(int x, int y, int z) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DroneWaypointsRequestPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_waypoints_request"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DroneWaypointsRequestPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, DroneWaypointsRequestPayload::x,
			ByteBufCodecs.VAR_INT, DroneWaypointsRequestPayload::y,
			ByteBufCodecs.VAR_INT, DroneWaypointsRequestPayload::z,
			DroneWaypointsRequestPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
