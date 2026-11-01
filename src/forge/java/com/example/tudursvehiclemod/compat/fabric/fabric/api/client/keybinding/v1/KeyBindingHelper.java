package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.keybinding.v1;

import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.List;

/** Collected here, registered in RegisterKeyMappingsEvent (compat.ForgeClientEvents). */
public final class KeyBindingHelper {
	public static final List<KeyMapping> KEYS = new ArrayList<>();

	private KeyBindingHelper() {
	}

	public static KeyMapping registerKeyBinding(KeyMapping key) {
		KEYS.add(key);
		return key;
	}
}
