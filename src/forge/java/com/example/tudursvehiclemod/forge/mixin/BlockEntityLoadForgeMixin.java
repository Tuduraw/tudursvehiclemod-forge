package com.example.tudursvehiclemod.forge.mixin;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fires the compat BLOCK_ENTITY_LOAD event where Fabric API does: whenever a block entity is added to a server chunk, both
 * when the chunk loads from disk and when the block is placed. */
@Mixin(LevelChunk.class)
public abstract class BlockEntityLoadForgeMixin {
	@Shadow
	@Final
	Level level;

	@Inject(method = "addAndRegisterBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("TAIL"))
	private void tudursvehiclemod$fireBlockEntityLoad(BlockEntity blockEntity, CallbackInfo ci) {
		if (this.level instanceof ServerLevel serverLevel && !blockEntity.isRemoved()) {
			ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(blockEntity, serverLevel);
		}
	}
}
