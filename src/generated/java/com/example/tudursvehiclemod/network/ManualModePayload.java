package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: toggle manual mode (disables the automatic level-flight assist) on the aircraft the sender is piloting. Held state, sent only on change. */
public record ManualModePayload(boolean enabled) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<ManualModePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "manual_mode"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ManualModePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, ManualModePayload::enabled,
			ManualModePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
