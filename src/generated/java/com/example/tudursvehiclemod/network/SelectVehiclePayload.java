package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "I picked this vehicle from the selection screen". Which hand is included since the player could be holding a valid tiered spawner in either. */
public record SelectVehiclePayload(ResourceLocation vehicleId, boolean mainHand) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<SelectVehiclePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "select_vehicle"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SelectVehiclePayload> CODEC = StreamCodec.composite(
			com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs.RESOURCE_LOCATION, SelectVehiclePayload::vehicleId,
			ByteBufCodecs.BOOL, SelectVehiclePayload::mainHand,
			SelectVehiclePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
