package com.example.tudursvehiclemod.compat.fabric.fabric.api.itemgroup.v1;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;

/** Fired from compat.ForgeEvents (BuildCreativeModeTabContentsEvent). */
public final class ItemGroupEvents {
	private ItemGroupEvents() {
	}

	public interface Entries {
		void accept(ItemLike item);

		void accept(ItemStack stack);
	}

	@FunctionalInterface
	public interface ModifyEntries {
		void modifyEntries(Entries entries);
	}

	public static final Map<ResourceKey<CreativeModeTab>, Event<ModifyEntries>> EVENTS = new HashMap<>();

	public static Event<ModifyEntries> modifyEntriesEvent(ResourceKey<CreativeModeTab> key) {
		return EVENTS.computeIfAbsent(key, k -> new Event<>(l -> e -> l.forEach(x -> x.modifyEntries(e))));
	}
}
