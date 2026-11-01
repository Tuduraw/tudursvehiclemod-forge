package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "toggle my VTOL between helicopter and aircraft mode now". */
public record VtolModeTogglePayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<VtolModeTogglePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "vtol_mode_toggle"));

	public static final StreamCodec<RegistryFriendlyByteBuf, VtolModeTogglePayload> CODEC =
			StreamCodec.unit(new VtolModeTogglePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
