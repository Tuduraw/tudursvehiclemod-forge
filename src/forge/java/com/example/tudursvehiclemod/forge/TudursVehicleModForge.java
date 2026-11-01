package com.example.tudursvehiclemod.forge;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.Registration;
import com.example.tudursvehiclemod.compat.net.Channel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Forge entry point. The shared (Fabric-shaped) initializers run once, at the first RegisterEvent: Forge
 * only lets blocks/items/entity types be constructed while registration is open (their intrusive holders
 * need an unfrozen registry), and the client initializer needs those objects. Registrations are queued by
 * compat.Registration and handed to Forge in each registry's own RegisterEvent; the Fabric-style event
 * listeners are fed by ForgeEvents / ForgeClientEvents (registered at construction, before any of them fire).
 */
@Mod(VehicleMod.MOD_ID)
public final class TudursVehicleModForge {
	private static boolean initialized;

	public TudursVehicleModForge() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(EventPriority.HIGH, (RegisterEvent e) -> {
			if (!initialized) {
				initialized = true;
				new VehicleMod().onInitialize();
				if (FMLEnvironment.dist == Dist.CLIENT) {
					ClientInit.initClient();
				}
			}
			Registration.onRegister(e);
		});
		modBus.addListener((FMLCommonSetupEvent e) -> Registration.finish());
		Channel.register();
		ForgeEvents.register(modBus, MinecraftForge.EVENT_BUS);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ClientInit.registerEvents(modBus);
		}
	}

	/** Separate class so the dedicated server never loads client classes. */
	private static final class ClientInit {
		static void registerEvents(IEventBus modBus) {
			ForgeClientEvents.register(modBus, MinecraftForge.EVENT_BUS);
		}

		static void initClient() {
			new com.example.tudursvehiclemod.client.VehicleModClient().onInitializeClient();
		}
	}
}
