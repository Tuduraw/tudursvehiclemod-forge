package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.event.lifecycle.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.client.Minecraft;

/** Fired from compat.ForgeClientEvents (TickEvent.ClientTickEvent). */
public final class ClientTickEvents {
	private ClientTickEvents() {
	}

	@FunctionalInterface
	public interface Tick {
		void onTick(Minecraft client);
	}

	public static final Event<Tick> START_CLIENT_TICK = new Event<>(l -> c -> l.forEach(x -> x.onTick(c)));
	public static final Event<Tick> END_CLIENT_TICK = new Event<>(l -> c -> l.forEach(x -> x.onTick(c)));
}
