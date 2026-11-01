package com.example.tudursvehiclemod.network;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.compat.mc.network.RegistryFriendlyByteBuf;
import com.example.tudursvehiclemod.compat.mc.network.codec.ByteBufCodecs;
import com.example.tudursvehiclemod.compat.mc.network.codec.StreamCodec;
import com.example.tudursvehiclemod.compat.mc.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client: "you're now steering entityId (a TV-guided missile you just fired) directly - start rendering your own camera from its own position/orientation instead" - see entity.projectile.VehicleProjectileEntity's own tudursvehiclemod$setTvControlled() doc, and client.mixin.CameraMixin's own doc for where this actually gets consumed. Paired with TvMissileControlEndPayload for the reverse. */
public record TvMissileControlStartPayload(int entityId) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<TvMissileControlStartPayload> ID =
			new CustomPacketPayload.Type<>(new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "tv_missile_control_start"));

	public static final StreamCodec<RegistryFriendlyByteBuf, TvMissileControlStartPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, TvMissileControlStartPayload::entityId,
			TvMissileControlStartPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
