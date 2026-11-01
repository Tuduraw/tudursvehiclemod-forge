package com.example.tudursvehiclemod.forge;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.command.v2.CommandRegistrationCallback;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1.ServerTickEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.player.UseBlockCallback;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.player.UseItemCallback;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.itemgroup.v1.ItemGroupEvents;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;

/** Feeds the compat Fabric-style events (compat.fabric.*) from the matching Forge events (both sides). */
public final class ForgeEvents {
	private ForgeEvents() {
	}

	public static void register(IEventBus modBus, IEventBus forgeBus) {
		modBus.addListener((EntityAttributeCreationEvent e) -> FabricDefaultAttributeRegistry.ATTRIBUTES.forEach((type, attributes) -> e.put(type, attributes.get())));
		modBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
			var event = ItemGroupEvents.EVENTS.get(e.getTabKey());
			if (event == null) {
				return;
			}
			event.invoker().modifyEntries(new ItemGroupEvents.Entries() {
				@Override
				public void accept(ItemLike item) {
					e.accept(item);
				}

				@Override
				public void accept(ItemStack stack) {
					e.accept(stack);
				}
			});
		});

		forgeBus.addListener((TickEvent.ServerTickEvent e) -> {
			if (e.phase == TickEvent.Phase.START) {
				ServerTickEvents.START_SERVER_TICK.invoker().onStartTick(e.getServer());
			} else {
				ServerTickEvents.END_SERVER_TICK.invoker().onEndTick(e.getServer());
			}
		});
		forgeBus.addListener((TickEvent.LevelTickEvent e) -> {
			if (e.phase == TickEvent.Phase.END && e.level instanceof ServerLevel level) {
				ServerTickEvents.END_WORLD_TICK.invoker().onEndTick(level);
			}
		});
		forgeBus.addListener((ServerStartingEvent e) -> ServerLifecycleEvents.SERVER_STARTING.invoker().on(e.getServer()));
		forgeBus.addListener((ServerStartedEvent e) -> ServerLifecycleEvents.SERVER_STARTED.invoker().on(e.getServer()));
		forgeBus.addListener((ServerStoppingEvent e) -> ServerLifecycleEvents.SERVER_STOPPING.invoker().on(e.getServer()));
		forgeBus.addListener((ServerStoppedEvent e) -> ServerLifecycleEvents.SERVER_STOPPED.invoker().on(e.getServer()));

		forgeBus.addListener((net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) -> {
			if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
				com.example.tudursvehiclemod.compat.fabric.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.invoker()
						.onPlayReady(player.connection, null, player.getServer());
			}
		});
		// Fired with no player after /reload (and once per player on login - that case is the JOIN above).
		forgeBus.addListener((net.minecraftforge.event.OnDatapackSyncEvent e) -> {
			if (e.getPlayer() == null) {
				var server = e.getPlayerList().getServer();
				ServerLifecycleEvents.END_DATA_PACK_RELOAD.invoker().endDataPackReload(server, server.getResourceManager(), true);
			}
		});
		forgeBus.addListener((EntityJoinLevelEvent e) -> {
			if (e.getLevel() instanceof ServerLevel level) {
				ServerEntityEvents.ENTITY_LOAD.invoker().onLoad(e.getEntity(), level);
			}
		});
		forgeBus.addListener((EntityLeaveLevelEvent e) -> {
			if (e.getLevel() instanceof ServerLevel level) {
				ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(e.getEntity(), level);
			}
		});
		// BLOCK_ENTITY_LOAD: fired by forge.mixin.BlockEntityLoadForgeMixin (block entity added to a server chunk - load or placement).
		forgeBus.addListener((LivingAttackEvent e) -> {
			if (!e.getEntity().level().isClientSide
					&& !ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(e.getEntity(), e.getSource(), e.getAmount())) {
				e.setCanceled(true);
			}
		});
		forgeBus.addListener((PlayerInteractEvent.RightClickBlock e) -> {
			InteractionResult result = UseBlockCallback.EVENT.invoker().interact(e.getEntity(), e.getLevel(), e.getHand(), e.getHitVec());
			if (result != InteractionResult.PASS) {
				e.setCanceled(true);
				e.setCancellationResult(result);
			}
		});
		forgeBus.addListener((PlayerInteractEvent.RightClickItem e) -> {
			InteractionResult result = UseItemCallback.EVENT.invoker().interact(e.getEntity(), e.getLevel(), e.getHand());
			if (result != InteractionResult.PASS) {
				e.setCanceled(true);
				e.setCancellationResult(result);
			}
		});
		forgeBus.addListener((RegisterCommandsEvent e) ->
				CommandRegistrationCallback.EVENT.invoker().register(e.getDispatcher(), e.getBuildContext(), e.getCommandSelection()));
		forgeBus.addListener((AddReloadListenerEvent e) -> ResourceManagerHelper.get(PackType.SERVER_DATA).listeners.forEach(e::addListener));
	}
}
