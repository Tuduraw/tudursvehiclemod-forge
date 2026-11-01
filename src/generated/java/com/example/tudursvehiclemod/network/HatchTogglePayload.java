package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "toggle my vehicle's hatch/canopy now". */
public record HatchTogglePayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<HatchTogglePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "hatch_toggle"));

	public static final StreamCodec<RegistryFriendlyByteBuf, HatchTogglePayload> CODEC =
			StreamCodec.unit(new HatchTogglePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
