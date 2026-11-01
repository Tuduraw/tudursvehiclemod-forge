package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "my dedicated descend key is currently held/released". */
public record DescendPayload(boolean descending) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DescendPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "descend"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DescendPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, DescendPayload::descending,
			DescendPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
