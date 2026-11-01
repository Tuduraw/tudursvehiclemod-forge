package com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/** Fired from compat.ForgeEvents (TickEvent.ServerTickEvent / LevelTickEvent, END phase). */
public final class ServerTickEvents {
	private ServerTickEvents() {
	}

	@FunctionalInterface
	public interface EndTick {
		void onEndTick(MinecraftServer server);
	}

	@FunctionalInterface
	public interface StartTick {
		void onStartTick(MinecraftServer server);
	}

	@FunctionalInterface
	public interface EndWorldTick {
		void onEndTick(ServerLevel world);
	}

	public static final Event<StartTick> START_SERVER_TICK = new Event<>(l -> s -> l.forEach(x -> x.onStartTick(s)));
	public static final Event<EndTick> END_SERVER_TICK = new Event<>(l -> s -> l.forEach(x -> x.onEndTick(s)));
	public static final Event<EndWorldTick> END_WORLD_TICK = new Event<>(l -> w -> l.forEach(x -> x.onEndTick(w)));
}
