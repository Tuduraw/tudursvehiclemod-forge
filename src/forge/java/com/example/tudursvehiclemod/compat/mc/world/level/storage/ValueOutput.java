package com.example.tudursvehiclemod.compat.mc.world.level.storage;

import net.minecraft.nbt.CompoundTag;

/** Forge 1.20.1 stand-in for 1.21.6's ValueOutput, writing into the CompoundTag 1.20.1 passes to saveAdditional()/addAdditionalSaveData(). */
public final class ValueOutput {
	private final CompoundTag tag;

	private ValueOutput(CompoundTag tag) {
		this.tag = tag;
	}

	public static ValueOutput of(CompoundTag tag) {
		return new ValueOutput(tag);
	}

	public CompoundTag tag() {
		return tag;
	}

	public void putInt(String key, int value) {
		tag.putInt(key, value);
	}

	public void putLong(String key, long value) {
		tag.putLong(key, value);
	}

	public void putFloat(String key, float value) {
		tag.putFloat(key, value);
	}

	public void putDouble(String key, double value) {
		tag.putDouble(key, value);
	}

	public void putShort(String key, short value) {
		tag.putShort(key, value);
	}

	public void putBoolean(String key, boolean value) {
		tag.putBoolean(key, value);
	}

	public void putString(String key, String value) {
		tag.putString(key, value);
	}

	public void putIntArray(String key, int[] value) {
		tag.putIntArray(key, value);
	}

	public ValueOutput child(String key) {
		CompoundTag child = new CompoundTag();
		tag.put(key, child);
		return new ValueOutput(child);
	}

	public void discard(String key) {
		tag.remove(key);
	}
}
