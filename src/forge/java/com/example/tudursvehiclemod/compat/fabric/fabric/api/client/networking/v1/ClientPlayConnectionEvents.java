package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.networking.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/** Fired from compat.ForgeClientEvents (ClientPlayerNetworkEvent.LoggingIn / LoggingOut). */
public final class ClientPlayConnectionEvents {
	private ClientPlayConnectionEvents() {
	}

	@FunctionalInterface
	public interface Join {
		void onPlayReady(ClientPacketListener handler, Object sender, Minecraft client);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onPlayDisconnect(ClientPacketListener handler, Minecraft client);
	}

	public static final Event<Join> JOIN = new Event<>(l -> (h, s, c) -> l.forEach(x -> x.onPlayReady(h, s, c)));
	public static final Event<Disconnect> DISCONNECT = new Event<>(l -> (h, c) -> l.forEach(x -> x.onPlayDisconnect(h, c)));
}
