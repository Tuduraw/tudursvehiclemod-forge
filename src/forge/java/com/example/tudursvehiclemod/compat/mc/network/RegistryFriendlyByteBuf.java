package com.example.tudursvehiclemod.compat.mc.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

/** 1.20.1 has no registry-aware buffer; the payloads only need FriendlyByteBuf's methods. */
public class RegistryFriendlyByteBuf extends FriendlyByteBuf {
	public RegistryFriendlyByteBuf(ByteBuf source) {
		super(source);
	}
}
