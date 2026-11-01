package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.networking.v1;

import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import com.example.tudursvehiclemod.compat.net.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Forge stand-in for Fabric API's ClientPlayNetworking. */
public final class ClientPlayNetworking {
	private ClientPlayNetworking() {
	}

	public interface Context {
		Minecraft client();

		LocalPlayer player();
	}

	@FunctionalInterface
	public interface PlayPayloadHandler<T extends CustomPacketPayload> {
		void receive(T payload, Context context);
	}

	@SuppressWarnings("unchecked")
	public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
		Channel.registerClientHandler(type.id(), (payload, ctx) -> handler.receive((T) payload, new Context() {
			@Override
			public Minecraft client() {
				return Minecraft.getInstance();
			}

			@Override
			public LocalPlayer player() {
				return Minecraft.getInstance().player;
			}
		}));
		return true;
	}

	public static void send(CustomPacketPayload payload) {
		Channel.sendToServer(payload);
	}

	public static boolean canSend(CustomPacketPayload.Type<?> type) {
		return Minecraft.getInstance().getConnection() != null;
	}
}
