package com.example.tudursvehiclemod.compat.fabric.fabric.api.object.builder.v1.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricBlockEntityTypeBuilder<T extends BlockEntity> {
	@FunctionalInterface
	public interface Factory<T extends BlockEntity> {
		T create(BlockPos pos, BlockState state);
	}

	private final Factory<T> factory;
	private final Block[] blocks;

	private FabricBlockEntityTypeBuilder(Factory<T> factory, Block[] blocks) {
		this.factory = factory;
		this.blocks = blocks;
	}

	public static <T extends BlockEntity> FabricBlockEntityTypeBuilder<T> create(Factory<T> factory, Block... blocks) {
		return new FabricBlockEntityTypeBuilder<>(factory, blocks);
	}

	public BlockEntityType<T> build() {
		return BlockEntityType.Builder.of(factory::create, blocks).build(null);
	}
}
