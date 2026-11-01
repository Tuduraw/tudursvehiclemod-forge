package com.example.tudursvehiclemod.item;

import net.minecraft.world.item.Item;

/** Equippable in the chest slot (Item.Settings' own equippable(EquipmentSlot.CHEST), applied where this is constructed in ModItems), deployable via Space while falling - see client.VehicleModClient's own key-handling doc and entity.ParachuteEntity's own doc for the actual deploy/render mechanism, both entirely separate from this class. This class exists mainly as a distinct, identifiable type (instanceof-checkable) rather than for any behavior of its own - a plain Item with the right components would work identically for equipping/dyeing purposes alone. */
public class ParachuteItem extends Item implements net.minecraft.world.item.Equipable, net.minecraft.world.item.DyeableLeatherItem {

	/** Undyed parachutes are white (1.21.11: the item's dyed_color default). */
	public static final int DEFAULT_COLOR = 0xFFFFFF;

	public ParachuteItem(Properties settings) {
		super(settings);
	}

	// Forge 1.20.1: no item components - the 1.21.11 equippable(CHEST) and dyed_color become Equipable (worn in the chest slot,
	// right-click to put on) and DyeableLeatherItem (dyed in the crafting grid like leather armour; colour in display.color).

	@Override
	public net.minecraft.world.entity.EquipmentSlot getEquipmentSlot() {
		return net.minecraft.world.entity.EquipmentSlot.CHEST;
	}

	@Override
	public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(net.minecraft.world.level.Level level,
			net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
		return this.swapWithEquipmentSlot(this, level, player, hand);
	}

	@Override
	public int getColor(net.minecraft.world.item.ItemStack stack) {
		net.minecraft.nbt.CompoundTag display = stack.getTagElement("display");
		return display != null && display.contains("color", 99) ? display.getInt("color") : DEFAULT_COLOR;
	}
}
