package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "my forward/backward and left/right movement input are currently these values".. */
public record ThrottleInputPayload(float forwardInput, float sidewaysInput) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<ThrottleInputPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "throttle_input"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ThrottleInputPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, ThrottleInputPayload::forwardInput,
			ByteBufCodecs.FLOAT, ThrottleInputPayload::sidewaysInput,
			ThrottleInputPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
