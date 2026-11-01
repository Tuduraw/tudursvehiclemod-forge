package com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** BLOCK_ENTITY_LOAD is fired from compat.ForgeEvents (ChunkEvent.Load: every block entity of the chunk). */
public final class ServerBlockEntityEvents {
	private ServerBlockEntityEvents() {
	}

	@FunctionalInterface
	public interface Load {
		void onLoad(BlockEntity blockEntity, ServerLevel world);
	}

	public static final Event<Load> BLOCK_ENTITY_LOAD = new Event<>(l -> (b, w) -> l.forEach(x -> x.onLoad(b, w)));
}
