package com.example.tudursvehiclemod.compat.fabric.fabric.api.resource;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.ArrayList;
import java.util.List;

/** Listeners are handed to Forge in AddReloadListenerEvent (server data) / RegisterClientReloadListenersEvent (client resources). */
public final class ResourceManagerHelper {
	private static final ResourceManagerHelper SERVER = new ResourceManagerHelper();
	private static final ResourceManagerHelper CLIENT = new ResourceManagerHelper();
	public final List<PreparableReloadListener> listeners = new ArrayList<>();

	public static ResourceManagerHelper get(PackType type) {
		return type == PackType.SERVER_DATA ? SERVER : CLIENT;
	}

	public void registerReloadListener(PreparableReloadListener listener) {
		listeners.add(listener);
	}
}
