package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "open the vehicle menu for whatever I'm currently riding". Sent once per key press (see VehicleModClient's own openVehicleMenuKey). */
public record OpenVehicleMenuPayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<OpenVehicleMenuPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "open_vehicle_menu"));

	public static final StreamCodec<RegistryFriendlyByteBuf, OpenVehicleMenuPayload> CODEC =
			StreamCodec.unit(new OpenVehicleMenuPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
