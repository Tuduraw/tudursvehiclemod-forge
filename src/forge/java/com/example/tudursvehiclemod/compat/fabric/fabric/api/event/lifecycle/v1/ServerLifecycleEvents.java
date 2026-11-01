package com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.server.MinecraftServer;

public final class ServerLifecycleEvents {
	private ServerLifecycleEvents() {
	}

	@FunctionalInterface
	public interface ServerCallback {
		void on(MinecraftServer server);
	}

	public static final Event<ServerCallback> SERVER_STARTING = new Event<>(l -> s -> l.forEach(x -> x.on(s)));
	public static final Event<ServerCallback> SERVER_STARTED = new Event<>(l -> s -> l.forEach(x -> x.on(s)));
	public static final Event<ServerCallback> SERVER_STOPPING = new Event<>(l -> s -> l.forEach(x -> x.on(s)));
	@FunctionalInterface
	public interface EndDataPackReload {
		void endDataPackReload(MinecraftServer server, net.minecraft.server.packs.resources.ResourceManager resourceManager, boolean success);
	}

	/** After /reload (forge.ForgeEvents: OnDatapackSyncEvent for all players). */
	public static final Event<EndDataPackReload> END_DATA_PACK_RELOAD = new Event<>(l -> (s, r, ok) -> l.forEach(x -> x.endDataPackReload(s, r, ok)));
	public static final Event<ServerCallback> SERVER_STOPPED = new Event<>(l -> s -> l.forEach(x -> x.on(s)));
}
