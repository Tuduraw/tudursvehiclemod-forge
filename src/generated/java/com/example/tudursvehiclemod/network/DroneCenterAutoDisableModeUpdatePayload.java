package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "set this Drone Center's own auto-disable resume mode" - sent when the player presses the toggle button on client.screen.DummyPilotConfigScreen. See DroneCenterAutoDisableModeOpenPayload's own doc for why this is a separate payload rather than an extra field on an existing one. */
public record DroneCenterAutoDisableModeUpdatePayload(int x, int y, int z, boolean autoResume) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DroneCenterAutoDisableModeUpdatePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_center_auto_disable_mode_update"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DroneCenterAutoDisableModeUpdatePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, DroneCenterAutoDisableModeUpdatePayload::x,
			ByteBufCodecs.VAR_INT, DroneCenterAutoDisableModeUpdatePayload::y,
			ByteBufCodecs.VAR_INT, DroneCenterAutoDisableModeUpdatePayload::z,
			ByteBufCodecs.BOOL, DroneCenterAutoDisableModeUpdatePayload::autoResume,
			DroneCenterAutoDisableModeUpdatePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
