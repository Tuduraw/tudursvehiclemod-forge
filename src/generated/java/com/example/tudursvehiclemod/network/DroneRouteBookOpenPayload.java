package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: "open your own drone route book editor for whichever hand you just used this item in" - sent when the player right-clicks a held item.DroneRouteBookItem. Only the hand is sent, not the book's own contents - the client already has its own local copy of the held stack (standard item sync), so it reads the route directly from that rather than needing it duplicated over the network. */
public record DroneRouteBookOpenPayload(boolean mainHand) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DroneRouteBookOpenPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_route_book_open"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DroneRouteBookOpenPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, DroneRouteBookOpenPayload::mainHand,
			DroneRouteBookOpenPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
