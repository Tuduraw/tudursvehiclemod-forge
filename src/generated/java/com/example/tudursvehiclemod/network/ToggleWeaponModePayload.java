package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "toggle the currently-selected weapon's own mode (see Readme_Weapon.txt's own ModeNum doc / AbstractVehicleEntity's own tudursvehiclemod$tryToggleWeaponMode() doc)". */
public record ToggleWeaponModePayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<ToggleWeaponModePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "toggle_weapon_mode"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleWeaponModePayload> CODEC =
			StreamCodec.unit(new ToggleWeaponModePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
