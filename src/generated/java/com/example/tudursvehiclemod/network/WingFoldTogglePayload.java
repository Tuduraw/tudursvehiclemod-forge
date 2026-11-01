package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "try to toggle my vehicle's wing fold now" - see
 * AbstractVehicleEntity#tryToggleWingFold()'s own doc for why this can be
 * rejected (a non-variable-sweep wing only folds/unfolds while stationary). */
public record WingFoldTogglePayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<WingFoldTogglePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "wing_fold_toggle"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WingFoldTogglePayload> CODEC =
			StreamCodec.unit(new WingFoldTogglePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
