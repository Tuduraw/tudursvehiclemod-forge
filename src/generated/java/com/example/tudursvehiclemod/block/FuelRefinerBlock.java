package com.example.tudursvehiclemod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FuelRefinerBlock extends BaseEntityBlock {

	/** Forge 1.20.1: runs the block entity's removal cleanup (1.21.5 preRemoveSideEffects) and drops its inventory when the block is broken or replaced. */
	@Override
	public void onRemove(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState newState, boolean moved) {
		if (!state.is(newState.getBlock())) {
			com.example.tudursvehiclemod.compat.Compat.preRemove(level, pos, state);
		}
		super.onRemove(state, level, pos, newState, moved);
	}

	public FuelRefinerBlock(Properties settings) {
		super(settings);
	}

	@Override
	public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
		return net.minecraft.world.level.block.RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FuelRefinerBlockEntity(pos, state);
	}

	// Reuses BlockWithEntity's own protected static validateTicker(..) helper (defining another one here with the same erasure would conflict with it).
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, com.example.tudursvehiclemod.registry.ModBlockEntities.FUEL_REFINER,
				FuelRefinerBlockEntity::tick);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, net.minecraft.world.InteractionHand tvmHand, BlockHitResult hit) {
		if (!(world instanceof ServerLevel)) {
			return InteractionResult.SUCCESS;
		}
		if (world.getBlockEntity(pos) instanceof FuelRefinerBlockEntity blockEntity
				&& player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			serverPlayer.openMenu(blockEntity);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		if (world.getBlockEntity(pos) instanceof FuelRefinerBlockEntity blockEntity) {
			return Math.round(blockEntity.getStoredBurnTimeRatio() * 15f);
		}
		return 0;
	}
}
