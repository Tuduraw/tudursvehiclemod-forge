package com.example.tudursvehiclemod.compat;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Forge 1.20.1 stand-in for vanilla Registry.register(...) calls made from the shared init code (rules.json
 * rewrites them to this class). The object is returned at once, so the upstream code keeps its
 * "FIELD = Registry.register(...)" shape; the actual registration happens later, in Forge's RegisterEvent
 * for that registry (registries are only open for writes there).
 */
public final class Registration {
	private record Entry(ResourceLocation id, Object value) {
	}

	private static final Map<ResourceKey<? extends Registry<?>>, List<Entry>> PENDING = new LinkedHashMap<>();
	private static boolean done;

	private Registration() {
	}

	public static <V, T extends V> T register(Registry<V> registry, ResourceLocation id, T value) {
		if (done) {
			throw new IllegalStateException("Registry " + registry.key().location() + " is closed - " + id + " was registered after Forge's RegisterEvent");
		}
		PENDING.computeIfAbsent(registry.key(), k -> new ArrayList<>()).add(new Entry(id, value));
		return value;
	}

	public static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> key, T value) {
		return register(registry, key.location(), value);
	}

	public static <V, T extends V> T register(Registry<V> registry, String id, T value) {
		return register(registry, new ResourceLocation(id), value);
	}

	/** Called from the mod's RegisterEvent listener: hands this registry's queued objects to Forge. */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void onRegister(RegisterEvent event) {
		List<Entry> entries = PENDING.remove(event.getRegistryKey());
		if (entries == null) {
			return;
		}
		ResourceKey key = event.getRegistryKey();
		event.register(key, helper -> {
			for (Entry e : entries) {
				((RegisterEvent.RegisterHelper) helper).register(e.id(), e.value());
			}
		});
	}

	/** After the last RegisterEvent: anything still queued means a registry this bridge never saw. */
	public static void finish() {
		done = true;
		if (!PENDING.isEmpty()) {
			throw new IllegalStateException("Unregistered objects left for " + PENDING.keySet());
		}
	}
}
