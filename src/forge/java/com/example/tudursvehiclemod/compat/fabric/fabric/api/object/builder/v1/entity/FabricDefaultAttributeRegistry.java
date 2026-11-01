package com.example.tudursvehiclemod.compat.fabric.fabric.api.object.builder.v1.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Collected here, handed to Forge in EntityAttributeCreationEvent (forge.ForgeEvents). Kept as suppliers:
 * on Forge the attribute builders reference Forge's own attributes (forge:swim_speed, ...), which only
 * exist after the ATTRIBUTE registry event - rules.json turns "X.createAttributes()" arguments into
 * "X::createAttributes" so they are built at event time.
 */
public final class FabricDefaultAttributeRegistry {
	public static final Map<EntityType<? extends LivingEntity>, Supplier<AttributeSupplier>> ATTRIBUTES = new LinkedHashMap<>();

	private FabricDefaultAttributeRegistry() {
	}

	public static void register(EntityType<? extends LivingEntity> type, Supplier<AttributeSupplier.Builder> builder) {
		ATTRIBUTES.put(type, () -> builder.get().build());
	}

	public static void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder) {
		AttributeSupplier built = builder.build();
		ATTRIBUTES.put(type, () -> built);
	}

	public static void register(EntityType<? extends LivingEntity> type, AttributeSupplier attributes) {
		ATTRIBUTES.put(type, () -> attributes);
	}
}
