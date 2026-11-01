package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "update the Drone Center at (x,y,z)'s own GROUND route to this" - the surface-vehicle counterpart to DroneWaypointsUpdatePayload (see that payload's own doc). Always sends the full current route, same "resend everything" reasoning. */
public record GroundWaypointsUpdatePayload(int x, int y, int z, String encodedWaypoints) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<GroundWaypointsUpdatePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "ground_waypoints_update"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GroundWaypointsUpdatePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, GroundWaypointsUpdatePayload::x,
			ByteBufCodecs.VAR_INT, GroundWaypointsUpdatePayload::y,
			ByteBufCodecs.VAR_INT, GroundWaypointsUpdatePayload::z,
			ByteBufCodecs.STRING_UTF8, GroundWaypointsUpdatePayload::encodedWaypoints,
			GroundWaypointsUpdatePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
