package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "manually flip my vehicle's landing gear now". */
public record GearTogglePayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<GearTogglePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "gear_toggle"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GearTogglePayload> CODEC =
			StreamCodec.unit(new GearTogglePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
