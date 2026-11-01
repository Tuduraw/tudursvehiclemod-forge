package com.example.tudursvehiclemod.compat.fabric.fabric.api.networking.v1;

import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import com.example.tudursvehiclemod.compat.net.Channel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Forge stand-in for Fabric API's ServerPlayNetworking. */
public final class ServerPlayNetworking {
	private ServerPlayNetworking() {
	}

	public interface Context {
		ServerPlayer player();

		MinecraftServer server();
	}

	@FunctionalInterface
	public interface PlayPayloadHandler<T extends CustomPacketPayload> {
		void receive(T payload, Context context);
	}

	@SuppressWarnings("unchecked")
	public static <T extends CustomPacketPayload> boolean registerGlobalReceiver(CustomPacketPayload.Type<T> type, PlayPayloadHandler<T> handler) {
		Channel.registerServerHandler(type.id(), (payload, ctx) -> {
			ServerPlayer player = ctx.getSender();
			if (player == null) {
				return;
			}
			handler.receive((T) payload, new Context() {
				@Override
				public ServerPlayer player() {
					return player;
				}

				@Override
				public MinecraftServer server() {
					return player.getServer();
				}
			});
		});
		return true;
	}

	/** Whether the player's client has this mod's channel (the payload type itself is not negotiated on Forge). */
	public static boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
		return Channel.hasS2C(type.id()) && Channel.CHANNEL.isRemotePresent(player.connection.connection);
	}

	public static void send(ServerPlayer player, CustomPacketPayload payload) {
		Channel.sendToPlayer(player, payload);
	}
}
