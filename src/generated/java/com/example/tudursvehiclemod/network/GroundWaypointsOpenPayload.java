package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: "open your own GROUND route settings screen for the block at (x,y,z), currently configured with this route" - the surface-vehicle counterpart to DroneWaypointsOpenPayload (see that payload's own doc for why the route travels as one encoded string). No Home Point field: that exists purely for the aircraft landing autopilot (see entity.AircraftEntity's own tudursvehiclemod$updateDroneLandingRouteAutopilot() doc), which a surface vehicle has no equivalent of - it simply stops. */
public record GroundWaypointsOpenPayload(int x, int y, int z, String encodedWaypoints) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<GroundWaypointsOpenPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "ground_waypoints_open"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GroundWaypointsOpenPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, GroundWaypointsOpenPayload::x,
			ByteBufCodecs.VAR_INT, GroundWaypointsOpenPayload::y,
			ByteBufCodecs.VAR_INT, GroundWaypointsOpenPayload::z,
			ByteBufCodecs.STRING_UTF8, GroundWaypointsOpenPayload::encodedWaypoints,
			GroundWaypointsOpenPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
