package com.example.tudursvehiclemod.forge;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.keybinding.v1.KeyBindingHelper;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.resource.ResourceManagerHelper;
import com.example.tudursvehiclemod.compat.mc.client.DeltaTracker;
import com.example.tudursvehiclemod.compat.mc.gizmos.Gizmos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client half of ForgeEvents: registration events on the mod bus, ticks/world/connection on the Forge bus. */
public final class ForgeClientEvents {
	private ForgeClientEvents() {
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void register(IEventBus modBus, IEventBus forgeBus) {
		modBus.addListener((RegisterKeyMappingsEvent e) -> KeyBindingHelper.KEYS.forEach(e::register));
		modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions e) -> EntityModelLayerRegistry.LAYERS.forEach(e::registerLayerDefinition));
		modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> EntityRendererRegistry.RENDERERS.forEach((type, provider) ->
				e.registerEntityRenderer((EntityType) type, (EntityRendererProvider) provider)));
		modBus.addListener((RegisterParticleProvidersEvent e) -> ParticleFactoryRegistry.getInstance().registrations.forEach(r -> r.accept(e)));
		modBus.addListener((RegisterGuiOverlaysEvent e) -> HudElementRegistry.ELEMENTS.forEach((id, element) ->
				e.registerAboveAll(id.getPath(), (gui, graphics, partialTick, width, height) -> element.render(graphics, new DeltaTracker(partialTick)))));
		// Parachute: dye colour on the item (1.21.11: the item model's "minecraft:dye" tint).
		modBus.addListener((net.minecraftforge.client.event.RegisterColorHandlersEvent.Item e) -> e.register(
				(stack, tintIndex) -> tintIndex == 0 && stack.getItem() instanceof com.example.tudursvehiclemod.item.ParachuteItem parachute
						? parachute.getColor(stack) : -1,
				com.example.tudursvehiclemod.item.ModItems.PARACHUTE));
		// "dither_shader" translucency mode: see forge.client.DitherRenderTypes.
		modBus.addListener((net.minecraftforge.client.event.RegisterShadersEvent e) -> {
			try {
				e.registerShader(new net.minecraft.client.renderer.ShaderInstance(e.getResourceProvider(),
						com.example.tudursvehiclemod.forge.client.DitherRenderTypes.SHADER_ID,
						com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY),
						shader -> com.example.tudursvehiclemod.forge.client.DitherRenderTypes.shader = shader);
			} catch (java.io.IOException ex) {
				throw new java.io.UncheckedIOException(ex);
			}
		});
		modBus.addListener((RegisterClientReloadListenersEvent e) -> ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).listeners.forEach(e::registerReloadListener));

		forgeBus.addListener((TickEvent.ClientTickEvent e) -> {
			Minecraft client = Minecraft.getInstance();
			if (e.phase == TickEvent.Phase.START) {
				Gizmos.beginTick();
				ClientTickEvents.START_CLIENT_TICK.invoker().onTick(client);
			} else {
				ClientTickEvents.END_CLIENT_TICK.invoker().onTick(client);
			}
		});
		forgeBus.addListener((LevelEvent.Load e) -> {
			if (e.getLevel() instanceof ClientLevel level) {
				ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.invoker().afterWorldChange(Minecraft.getInstance(), level);
			}
		});
		forgeBus.addListener((ClientPlayerNetworkEvent.LoggingIn e) ->
				ClientPlayConnectionEvents.JOIN.invoker().onPlayReady(e.getPlayer().connection, null, Minecraft.getInstance()));
		forgeBus.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> {
			if (e.getPlayer() != null) {
				ClientPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(e.getPlayer().connection, Minecraft.getInstance());
			}
		});
		// See forge.client.CameraRoll: the vehicle camera's roll (CameraMixin) goes in here.
		forgeBus.addListener((net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles e) -> {
			float roll = com.example.tudursvehiclemod.forge.client.CameraRoll.take();
			if (!Float.isNaN(roll)) {
				e.setRoll(roll);
			}
		});
		forgeBus.addListener((RenderLevelStageEvent e) -> {
			if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
				Gizmos.render(e.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), e.getCamera().getPosition());
			}
		});
	}
}
