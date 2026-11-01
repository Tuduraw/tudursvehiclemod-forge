package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: "remote control of whatever vehicle you were controlling has ended - revert your own camera/input back to normal". Paired with RemoteControlStartPayload for the initial hand-off. */
public record RemoteControlEndPayload() implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<RemoteControlEndPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "remote_control_end"));

	public static final StreamCodec<RegistryFriendlyByteBuf, RemoteControlEndPayload> CODEC =
			StreamCodec.unit(new RemoteControlEndPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
