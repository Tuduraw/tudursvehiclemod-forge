package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: "open your own Drone Center config screen for the block at (x,y,z), currently configured with these values" - sent when a player right-clicks a bound Drone Center. See client.screen.DroneCenterConfigScreen's own doc for the actual UI this populates.
 *
 * FormationConfig - "formationSize,formationType,formationSpacing,formationElementSpacing" (comma-separated) rather than 4 separate fields - avoids risking PacketCodec.tuple's own 12-field arity limit (8 existing fields + 4 new would land exactly at that boundary). Parsed/built via client.screen.DroneCenterConfigScreen's own tudursvehiclemod$encodeFormationConfig()/-decodeFormationConfig() helpers, shared with DroneCenterConfigUpdatePayload's own identical field. */
public record DroneCenterConfigOpenPayload(
		int x, int y, int z, float speedFraction, float orbitAltitude, float radiusMultiplier, boolean active,
		boolean redstoneControlEnabled, String formationConfig
) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DroneCenterConfigOpenPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_center_config_open"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DroneCenterConfigOpenPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, DroneCenterConfigOpenPayload::x,
			ByteBufCodecs.VAR_INT, DroneCenterConfigOpenPayload::y,
			ByteBufCodecs.VAR_INT, DroneCenterConfigOpenPayload::z,
			ByteBufCodecs.FLOAT, DroneCenterConfigOpenPayload::speedFraction,
			ByteBufCodecs.FLOAT, DroneCenterConfigOpenPayload::orbitAltitude,
			ByteBufCodecs.FLOAT, DroneCenterConfigOpenPayload::radiusMultiplier,
			ByteBufCodecs.BOOL, DroneCenterConfigOpenPayload::active,
			ByteBufCodecs.BOOL, DroneCenterConfigOpenPayload::redstoneControlEnabled,
			ByteBufCodecs.STRING_UTF8, DroneCenterConfigOpenPayload::formationConfig,
			DroneCenterConfigOpenPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
