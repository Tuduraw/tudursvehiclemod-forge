package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "move me to the PREVIOUS available seat on the vehicle I'm riding" - see SwitchSeatPayload's own doc for the (forward) counterpart, and AbstractVehicleEntity's own tudursvehiclemod$trySwitchSeat() doc for the shared implementation both go through. */
public record SwitchSeatPreviousPayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<SwitchSeatPreviousPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "switch_seat_previous"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SwitchSeatPreviousPayload> CODEC =
			StreamCodec.unit(new SwitchSeatPreviousPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
