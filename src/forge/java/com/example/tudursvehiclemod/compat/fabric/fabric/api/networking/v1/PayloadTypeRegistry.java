package com.example.tudursvehiclemod.compat.fabric.fabric.api.networking.v1;

import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import com.example.tudursvehiclemod.compat.net.Channel;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** Forge stand-in for Fabric API's PayloadTypeRegistry (codecs are kept by Channel). */
public final class PayloadTypeRegistry {
	private static final PayloadTypeRegistry C2S = new PayloadTypeRegistry(true);
	private static final PayloadTypeRegistry S2C = new PayloadTypeRegistry(false);
	private final boolean toServer;

	private PayloadTypeRegistry(boolean toServer) {
		this.toServer = toServer;
	}

	public static PayloadTypeRegistry playC2S() {
		return C2S;
	}

	public static PayloadTypeRegistry playS2C() {
		return S2C;
	}

	public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<? super com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf, T> codec) {
		Channel.registerCodec(type.id(), codec, toServer);
	}
}
