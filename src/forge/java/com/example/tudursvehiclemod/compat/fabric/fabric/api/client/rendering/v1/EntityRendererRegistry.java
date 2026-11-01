package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.rendering.v1;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.LinkedHashMap;
import java.util.Map;

/** Collected here, registered in EntityRenderersEvent.RegisterRenderers (compat.ForgeClientEvents). */
public final class EntityRendererRegistry {
	public static final Map<EntityType<?>, EntityRendererProvider<?>> RENDERERS = new LinkedHashMap<>();

	private EntityRendererRegistry() {
	}

	public static <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> provider) {
		RENDERERS.put(type, provider);
	}
}
