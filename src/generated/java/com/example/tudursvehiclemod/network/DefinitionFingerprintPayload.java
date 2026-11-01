package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import java.util.LinkedHashMap;
import java.util.Map;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client, on join and after /reload: a short hash of every vehicle definition and weapon config the server has loaded
 * (see asset.DefinitionFingerprint). The definitions themselves are not sent - the client compares this against its own copy of
 * the addon packs and warns when they differ (client.ClientDefinitions). A few dozen bytes per entry. */
public record DefinitionFingerprintPayload(Map<String, Long> vehicles, Map<String, Long> weapons) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DefinitionFingerprintPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "definition_fingerprint"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DefinitionFingerprintPayload> CODEC = StreamCodec.ofMember(
			(value, buf) -> {
				writeMap(buf, value.vehicles());
				writeMap(buf, value.weapons());
			},
			buf -> new DefinitionFingerprintPayload(readMap(buf), readMap(buf))
	);

	private static void writeMap(RegistryFriendlyByteBuf buf, Map<String, Long> map) {
		buf.writeVarInt(map.size());
		map.forEach((key, hash) -> {
			buf.writeUtf(key);
			buf.writeLong(hash);
		});
	}

	private static Map<String, Long> readMap(RegistryFriendlyByteBuf buf) {
		int size = buf.readVarInt();
		Map<String, Long> map = new LinkedHashMap<>();
		for (int i = 0; i < size; i++) {
			map.put(buf.readUtf(), buf.readLong());
		}
		return map;
	}

	/** What this server has loaded right now. */
	public static DefinitionFingerprintPayload current() {
		return new DefinitionFingerprintPayload(
				com.example.tudursvehiclemod.asset.DefinitionFingerprint.currentVehicles(),
				com.example.tudursvehiclemod.asset.DefinitionFingerprint.currentWeapons());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
