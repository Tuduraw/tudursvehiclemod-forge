package com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom;

import net.minecraft.resources.ResourceLocation;

/** Forge 1.20.1 stand-in for 1.21.11's CustomPacketPayload; carried over this mod's own Forge channel (see compat.net.Channel). */
public interface CustomPacketPayload {
	Type<? extends CustomPacketPayload> type();

	record Type<T extends CustomPacketPayload>(ResourceLocation id) {
	}
}
