package com.example.tudursvehiclemod.registry;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.block.FuelRefinerBlockEntity;
import com.example.tudursvehiclemod.compat.fabric.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

	public static BlockEntityType<FuelRefinerBlockEntity> FUEL_REFINER;
	public static BlockEntityType<com.example.tudursvehiclemod.block.StationBlockEntity> STATION;
	public static BlockEntityType<com.example.tudursvehiclemod.block.DroneCenterBlockEntity> DRONE_CENTER;
	public static BlockEntityType<com.example.tudursvehiclemod.block.VehicleConverterBlockEntity> VEHICLE_CONVERTER;
	public static BlockEntityType<com.example.tudursvehiclemod.block.FireSupportCenterBlockEntity> FIRE_SUPPORT_CENTER;

	public static void register() {
		FUEL_REFINER = com.example.tudursvehiclemod.compat.Registration.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "fuel_refiner"),
				FabricBlockEntityTypeBuilder.create(FuelRefinerBlockEntity::new, ModBlocks.FUEL_REFINER).build());

		STATION = com.example.tudursvehiclemod.compat.Registration.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "station"),
				FabricBlockEntityTypeBuilder.create(com.example.tudursvehiclemod.block.StationBlockEntity::new, ModBlocks.STATION).build());

		DRONE_CENTER = com.example.tudursvehiclemod.compat.Registration.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_center"),
				FabricBlockEntityTypeBuilder.create(com.example.tudursvehiclemod.block.DroneCenterBlockEntity::new, ModBlocks.DRONE_CENTER).build());

		VEHICLE_CONVERTER = com.example.tudursvehiclemod.compat.Registration.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "vehicle_converter"),
				FabricBlockEntityTypeBuilder.create(com.example.tudursvehiclemod.block.VehicleConverterBlockEntity::new, ModBlocks.VEHICLE_CONVERTER).build());

		FIRE_SUPPORT_CENTER = com.example.tudursvehiclemod.compat.Registration.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "fire_support_center"),
				FabricBlockEntityTypeBuilder.create(com.example.tudursvehiclemod.block.FireSupportCenterBlockEntity::new, ModBlocks.FIRE_SUPPORT_CENTER).build());
	}
}
