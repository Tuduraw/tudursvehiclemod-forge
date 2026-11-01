package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "fire the weapon mounted at this index on the vehicle I'm currently riding". */
public record FireWeaponPayload(int weaponIndex) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<FireWeaponPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "fire_weapon"));

	public static final StreamCodec<RegistryFriendlyByteBuf, FireWeaponPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, FireWeaponPayload::weaponIndex,
			FireWeaponPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
