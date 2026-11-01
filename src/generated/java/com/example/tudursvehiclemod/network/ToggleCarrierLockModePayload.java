package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "toggle lock mode" for a Carrier formation's own lead aircraft - see entity.AbstractVehicleEntity's own CARRIER_LOCK_MODE_ACTIVE doc for what this actually does. Sent when the player presses the dedicated toggle key WITHOUT Alt held (see client.VehicleModClient's own carrierLockToggleKey doc - Alt-held instead sends ReleaseAllCarrierLocksPayload). No payload data needed - the server flips whichever vehicle the player is CURRENTLY riding. */
public record ToggleCarrierLockModePayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<ToggleCarrierLockModePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "toggle_carrier_lock_mode"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCarrierLockModePayload> CODEC =
			StreamCodec.unit(new ToggleCarrierLockModePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
