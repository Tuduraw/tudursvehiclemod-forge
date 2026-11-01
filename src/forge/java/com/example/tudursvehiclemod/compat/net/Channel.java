package com.example.tudursvehiclemod.compat.net;

import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * The one Forge channel every payload of this mod travels over. A message is the payload type id
 * followed by the payload encoded with the codec registered for it (PayloadTypeRegistry), so the
 * 1.21.11 payload records work unchanged.
 */
public final class Channel {
	private Channel() {
	}

	private static final String PROTOCOL = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("tudursvehiclemod", "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

	/** Raw message: payload id + encoded bytes. */
	public record Message(ResourceLocation id, FriendlyByteBuf data) {
	}

	@SuppressWarnings("rawtypes")
	static final Map<ResourceLocation, StreamCodec> C2S = new HashMap<>();
	@SuppressWarnings("rawtypes")
	static final Map<ResourceLocation, StreamCodec> S2C = new HashMap<>();
	static final Map<ResourceLocation, BiConsumer<CustomPacketPayload, NetworkEvent.Context>> SERVER_HANDLERS = new HashMap<>();
	static final Map<ResourceLocation, BiConsumer<CustomPacketPayload, NetworkEvent.Context>> CLIENT_HANDLERS = new HashMap<>();

	private static boolean registered;

	/** Whether a server-to-client payload with this id has a codec. */
	public static boolean hasS2C(ResourceLocation id) {
		return S2C.containsKey(id);
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		CHANNEL.registerMessage(0, Message.class,
				(msg, buf) -> {
					buf.writeResourceLocation(msg.id());
					buf.writeBytes(msg.data(), msg.data().readerIndex(), msg.data().readableBytes());
				},
				buf -> {
					ResourceLocation id = buf.readResourceLocation();
					return new Message(id, new FriendlyByteBuf(buf.copy()));
				},
				Channel::handle);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Message encode(CustomPacketPayload payload, boolean toServer) {
		ResourceLocation id = payload.type().id();
		StreamCodec codec = (toServer ? C2S : S2C).get(id);
		if (codec == null) {
			throw new IllegalStateException("Unregistered payload " + id);
		}
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer());
		codec.encode(buf, payload);
		return new Message(id, buf);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void handle(Message msg, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		boolean toServer = ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER;
		StreamCodec codec = (toServer ? C2S : S2C).get(msg.id());
		var handler = (toServer ? SERVER_HANDLERS : CLIENT_HANDLERS).get(msg.id());
		if (codec != null && handler != null) {
			CustomPacketPayload payload = (CustomPacketPayload) codec.decode(new RegistryFriendlyByteBuf(msg.data()));
			// Fabric 1.21 runs payload handlers on the main thread; do the same.
			ctx.enqueueWork(() -> handler.accept(payload, ctx));
		}
		msg.data().release();
		ctx.setPacketHandled(true);
	}

	public static void registerCodec(ResourceLocation id, StreamCodec<?, ?> codec, boolean toServer) {
		(toServer ? C2S : S2C).put(id, codec);
	}

	public static void registerServerHandler(ResourceLocation id, BiConsumer<CustomPacketPayload, NetworkEvent.Context> handler) {
		SERVER_HANDLERS.put(id, handler);
	}

	public static void registerClientHandler(ResourceLocation id, BiConsumer<CustomPacketPayload, NetworkEvent.Context> handler) {
		CLIENT_HANDLERS.put(id, handler);
	}

	public static void sendToServer(CustomPacketPayload payload) {
		CHANNEL.sendToServer(encode(payload, true));
	}

	public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), encode(payload, false));
	}
}
