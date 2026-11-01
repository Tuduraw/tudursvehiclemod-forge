package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.rendering.v1;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Collected here, registered in EntityRenderersEvent.RegisterLayerDefinitions (compat.ForgeClientEvents). */
public final class EntityModelLayerRegistry {
	public static final Map<ModelLayerLocation, Supplier<LayerDefinition>> LAYERS = new LinkedHashMap<>();

	private EntityModelLayerRegistry() {
	}

	@FunctionalInterface
	public interface TexturedModelDataProvider {
		LayerDefinition createModelData();
	}

	public static void registerModelLayer(ModelLayerLocation layer, TexturedModelDataProvider provider) {
		LAYERS.put(layer, provider::createModelData);
	}
}
