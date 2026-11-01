package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.event.lifecycle.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/** Fired from compat.ForgeClientEvents (LevelEvent.Load on the client). */
public final class ClientWorldEvents {
	private ClientWorldEvents() {
	}

	@FunctionalInterface
	public interface AfterChange {
		void afterWorldChange(Minecraft client, ClientLevel world);
	}

	public static final Event<AfterChange> AFTER_CLIENT_WORLD_CHANGE = new Event<>(l -> (c, w) -> l.forEach(x -> x.afterWorldChange(c, w)));
}
