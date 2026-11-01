package com.example.tudursvehiclemod.forge.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge 1.20.1: keeps the position-delta base of an entity this client controls (the vehicle the local player pilots)
 * in step with the server, as 1.21.2+ does.
 *
 * <p>The server sends an entity's movement as deltas from the last position it sent ({@link VecDeltaCodec}). 1.20.1's
 * handleMoveEntity() drops the whole packet for an entity controlled by this client - including the base update - so
 * while the player pilots a vehicle its base stays where it was when they boarded. The moment they get off, the next
 * delta was decoded against that stale base: the vehicle jumped back to roughly where it had been boarded, drifted, and
 * only snapped to its real position with the next absolute (teleport) sync. 1.21.11 updates the base for a locally
 * controlled entity and ignores only the movement itself; this does the same.
 */
@Mixin(ClientPacketListener.class)
public abstract class MoveEntityCodecForgeMixin {

	@Shadow
	private ClientLevel level;

	@Inject(method = "handleMoveEntity", at = @At("HEAD"))
	private void tudursvehiclemod$keepControlledEntityCodecBase(ClientboundMoveEntityPacket packet, CallbackInfo ci) {
		// handleMoveEntity() is entered first on the network thread, where it only re-schedules itself.
		if (!Minecraft.getInstance().isSameThread() || !packet.hasPosition()) {
			return;
		}
		Entity entity = packet.getEntity(this.level);
		if (entity != null && entity.isControlledByLocalInstance()) {
			VecDeltaCodec codec = entity.getPositionCodec();
			codec.setBase(codec.decode(packet.getXa(), packet.getYa(), packet.getZa()));
		}
	}
}
