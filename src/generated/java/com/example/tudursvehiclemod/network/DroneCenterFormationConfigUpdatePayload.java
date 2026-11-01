package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server: "update the Drone Center at (x,y,z) to this formation config" - sent whenever the player adjusts a value in client.screen.DroneCenterFormationConfigScreen. those controls moved to their own dedicated screen, which has no access to that other screen's own speed/orbitAltitude/radiusMultiplier/active/redstoneControlEnabled values at all - a separate, formation-only payload avoids needing to thread all of that unrelated state through just to keep reusing DroneCenterConfigUpdatePayload.
 *
 * formationConfig - same "formationSize,formationType,formationSpacing,formationElementSpacing" compact format DroneCenterConfigOpenPayload/UpdatePayload's own identical field already uses. */
public record DroneCenterFormationConfigUpdatePayload(int x, int y, int z, String formationConfig) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DroneCenterFormationConfigUpdatePayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_center_formation_config_update"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DroneCenterFormationConfigUpdatePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, DroneCenterFormationConfigUpdatePayload::x,
			ByteBufCodecs.VAR_INT, DroneCenterFormationConfigUpdatePayload::y,
			ByteBufCodecs.VAR_INT, DroneCenterFormationConfigUpdatePayload::z,
			ByteBufCodecs.STRING_UTF8, DroneCenterFormationConfigUpdatePayload::formationConfig,
			DroneCenterFormationConfigUpdatePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
