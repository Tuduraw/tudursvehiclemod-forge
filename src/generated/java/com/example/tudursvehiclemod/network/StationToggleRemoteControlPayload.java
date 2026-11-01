package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "toggle remote control at the station at this position" - sent when the player presses the "Toggle Remote Control" button inside client.screen.StationMenuScreen (see that class's own doc). Runs the exact same logic block.StationBlock's own right-click used to run directly, before that was replaced with this menu screen. */
public record StationToggleRemoteControlPayload(int x, int y, int z) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<StationToggleRemoteControlPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "station_toggle_remote_control"));

	public static final StreamCodec<RegistryFriendlyByteBuf, StationToggleRemoteControlPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, StationToggleRemoteControlPayload::x,
			ByteBufCodecs.INT, StationToggleRemoteControlPayload::y,
			ByteBufCodecs.INT, StationToggleRemoteControlPayload::z,
			StationToggleRemoteControlPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
