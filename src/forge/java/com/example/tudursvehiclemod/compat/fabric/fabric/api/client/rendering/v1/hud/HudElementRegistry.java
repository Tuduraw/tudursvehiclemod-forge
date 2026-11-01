package com.example.tudursvehiclemod.compat.fabric.fabric.api.client.rendering.v1.hud;

import com.example.tudursvehiclemod.compat.mc.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/** Elements are drawn on top of the vanilla HUD from RegisterGuiOverlaysEvent (compat.ForgeClientEvents). */
public final class HudElementRegistry {
	@FunctionalInterface
	public interface HudElement {
		void render(GuiGraphics context, DeltaTracker tickCounter);
	}

	public static final Map<ResourceLocation, HudElement> ELEMENTS = new LinkedHashMap<>();

	private HudElementRegistry() {
	}

	public static void addLast(ResourceLocation id, HudElement element) {
		ELEMENTS.put(id, element);
	}
}
