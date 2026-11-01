package com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/** Fired from compat.ForgeEvents (EntityJoinLevelEvent / EntityLeaveLevelEvent on the server). */
public final class ServerEntityEvents {
	private ServerEntityEvents() {
	}

	@FunctionalInterface
	public interface Load {
		void onLoad(Entity entity, ServerLevel world);
	}

	@FunctionalInterface
	public interface Unload {
		void onUnload(Entity entity, ServerLevel world);
	}

	public static final Event<Load> ENTITY_LOAD = new Event<>(l -> (e, w) -> l.forEach(x -> x.onLoad(e, w)));
	public static final Event<Unload> ENTITY_UNLOAD = new Event<>(l -> (e, w) -> l.forEach(x -> x.onUnload(e, w)));
}
