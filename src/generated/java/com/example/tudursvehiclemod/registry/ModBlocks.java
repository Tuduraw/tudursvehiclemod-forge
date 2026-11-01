package com.example.tudursvehiclemod.registry;

import com.example.tudursvehiclemod.VehicleMod;
import com.example.tudursvehiclemod.block.FuelRefinerBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

	public static Block FUEL_REFINER;
	public static Block STATION;
	public static Block DRONE_CENTER;
	public static Block VEHICLE_CONVERTER;
	public static Block FIRE_SUPPORT_CENTER;

	public static void register() {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "fuel_refiner"));
		FUEL_REFINER = com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.BLOCK, blockKey,
				new FuelRefinerBlock(BlockBehaviour.Properties.of()
						
						.mapColor(MapColor.METAL)
						.strength(3.5f)
						.requiresCorrectToolForDrops()));

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "fuel_refiner"));
		com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.ITEM, itemKey,
				new BlockItem(FUEL_REFINER, new Item.Properties()));

		// This project's own UAV control station - see block.StationBlock's own doc.
		ResourceKey<Block> stationKey = ResourceKey.create(Registries.BLOCK, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "station"));
		STATION = com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.BLOCK, stationKey,
				new com.example.tudursvehiclemod.block.StationBlock(BlockBehaviour.Properties.of()
						
						.mapColor(MapColor.METAL)
						.strength(4.0f)
						.requiresCorrectToolForDrops()));

		ResourceKey<Item> stationItemKey = ResourceKey.create(Registries.ITEM, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "station"));
		com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.ITEM, stationItemKey,
				new BlockItem(STATION, new Item.Properties()));

		// This project's own target drone control center - see block.DroneCenterBlock's own doc.
		ResourceKey<Block> droneCenterKey = ResourceKey.create(Registries.BLOCK, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_center"));
		DRONE_CENTER = com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.BLOCK, droneCenterKey,
				new com.example.tudursvehiclemod.block.DroneCenterBlock(BlockBehaviour.Properties.of()
						
						.mapColor(MapColor.METAL)
						.strength(4.0f)
						.requiresCorrectToolForDrops()));

		ResourceKey<Item> droneCenterItemKey = ResourceKey.create(Registries.ITEM, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "drone_center"));
		com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.ITEM, droneCenterItemKey,
				new BlockItem(DRONE_CENTER, new Item.Properties()));

		// Converts a tier-based base item into a specific vehicle-category spawner item - see block.VehicleConverterBlockEntity's own doc.
		ResourceKey<Block> vehicleConverterKey = ResourceKey.create(Registries.BLOCK, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "vehicle_converter"));
		VEHICLE_CONVERTER = com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.BLOCK, vehicleConverterKey,
				new com.example.tudursvehiclemod.block.VehicleConverterBlock(BlockBehaviour.Properties.of()
						
						.mapColor(MapColor.METAL)
						.strength(3.5f)
						.requiresCorrectToolForDrops()));

		ResourceKey<Item> vehicleConverterItemKey = ResourceKey.create(Registries.ITEM, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "vehicle_converter"));
		com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.ITEM, vehicleConverterItemKey,
				new BlockItem(VEHICLE_CONVERTER, new Item.Properties()));

		// Lays linked vehicles' guns on a target point - see block.FireSupportCenterBlockEntity's own doc.
		ResourceKey<Block> fireSupportCenterKey = ResourceKey.create(Registries.BLOCK, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "fire_support_center"));
		FIRE_SUPPORT_CENTER = com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.BLOCK, fireSupportCenterKey,
				new com.example.tudursvehiclemod.block.FireSupportCenterBlock(BlockBehaviour.Properties.of()
						
						.mapColor(MapColor.METAL)
						.strength(4.0f)
						.requiresCorrectToolForDrops()));

		ResourceKey<Item> fireSupportCenterItemKey = ResourceKey.create(Registries.ITEM, new net.minecraft.resources.ResourceLocation(VehicleMod.MOD_ID, "fire_support_center"));
		com.example.tudursvehiclemod.compat.Registration.register(BuiltInRegistries.ITEM, fireSupportCenterItemKey,
				new BlockItem(FIRE_SUPPORT_CENTER, new Item.Properties()));
	}
}
