package com.example.tudursvehiclemod.compat.fabric.fabric.api.event.player;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/** Checked from compat.ForgeEvents (PlayerInteractEvent.RightClickBlock); the first result other than PASS wins. */
@FunctionalInterface
public interface UseBlockCallback {
	InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult);

	Event<UseBlockCallback> EVENT = new Event<>(l -> (p, w, h, r) -> {
		for (UseBlockCallback x : l) {
			InteractionResult result = x.interact(p, w, h, r);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});
}
