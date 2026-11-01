package com.example.tudursvehiclemod.compat.fabric.fabric.api.event.player;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.Event;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Checked from compat.ForgeEvents (PlayerInteractEvent.RightClickItem); the first result other than PASS wins. */
@FunctionalInterface
public interface UseItemCallback {
	InteractionResult interact(Player player, Level world, InteractionHand hand);

	Event<UseItemCallback> EVENT = new Event<>(l -> (p, w, h) -> {
		for (UseItemCallback x : l) {
			InteractionResult result = x.interact(p, w, h);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});
}
