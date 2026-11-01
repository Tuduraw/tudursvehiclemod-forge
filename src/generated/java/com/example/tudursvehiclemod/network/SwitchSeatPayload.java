package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "move me to the next available seat on the vehicle I'm riding" - see AbstractVehicleEntity's own tudursvehiclemod$trySwitchSeat() doc. */
public record SwitchSeatPayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<SwitchSeatPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "switch_seat"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SwitchSeatPayload> CODEC =
			StreamCodec.unit(new SwitchSeatPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
