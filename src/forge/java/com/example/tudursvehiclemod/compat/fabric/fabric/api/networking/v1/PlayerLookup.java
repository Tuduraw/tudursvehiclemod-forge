package com.example.tudursvehiclemod.compat.fabric.fabric.api.networking.v1;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;

/** Forge stand-in for Fabric API's PlayerLookup.tracking(entity): the players that have the entity's chunk loaded (what vanilla uses to start tracking it). */
public final class PlayerLookup {
	private PlayerLookup() {
	}

	public static Collection<ServerPlayer> tracking(Entity entity) {
		Collection<ServerPlayer> out = new ArrayList<>();
		if (entity.level() instanceof ServerLevel level) {
			level.getChunkSource().chunkMap.getPlayers(entity.chunkPosition(), false).forEach(out::add);
		}
		return out;
	}
}
