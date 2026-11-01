package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "my free-look key is currently held/released". Sent once on change (not every tick). */
public record FreeLookPayload(boolean freeLook) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<FreeLookPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "free_look"));

	public static final StreamCodec<RegistryFriendlyByteBuf, FreeLookPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, FreeLookPayload::freeLook,
			FreeLookPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
