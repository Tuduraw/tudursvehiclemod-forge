package com.example.tudursvehiclemod.compat.fabric.fabric.api.networking.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/** Forge stand-in for Fabric API's ServerPlayConnectionEvents. JOIN is fired from forge.ForgeEvents (PlayerLoggedInEvent). */
public final class ServerPlayConnectionEvents {
	private ServerPlayConnectionEvents() {
	}

	@FunctionalInterface
	public interface Join {
		/** sender is always null on Forge (send with ServerPlayNetworking instead). */
		void onPlayReady(ServerGamePacketListenerImpl handler, Object sender, MinecraftServer server);
	}

	public static final Event<Join> JOIN = new Event<>(l -> (h, s, m) -> l.forEach(x -> x.onPlayReady(h, s, m)));
}
