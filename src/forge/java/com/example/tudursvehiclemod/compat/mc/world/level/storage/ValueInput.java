package com.example.tudursvehiclemod.compat.mc.world.level.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

/**
 * Forge 1.20.1 stand-in for 1.21.6's ValueInput, backed by the CompoundTag 1.20.1 passes to
 * load()/readAdditionalSaveData(). rules.json turns `void load(ValueInput view)` into
 * `void load(CompoundTag tvmTag) { ValueInput view = ValueInput.of(tvmTag); ...`.
 */
public final class ValueInput {
	private final CompoundTag tag;

	private ValueInput(CompoundTag tag) {
		this.tag = tag;
	}

	public static ValueInput of(CompoundTag tag) {
		return new ValueInput(tag);
	}

	public CompoundTag tag() {
		return tag;
	}

	public int getIntOr(String key, int fallback) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
	}

	public long getLongOr(String key, long fallback) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : fallback;
	}

	public float getFloatOr(String key, float fallback) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getFloat(key) : fallback;
	}

	public double getDoubleOr(String key, double fallback) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getDouble(key) : fallback;
	}

	public short getShortOr(String key, short fallback) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getShort(key) : fallback;
	}

	public boolean getBooleanOr(String key, boolean fallback) {
		return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
	}

	public String getStringOr(String key, String fallback) {
		return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : fallback;
	}

	public Optional<int[]> getIntArray(String key) {
		return tag.contains(key, Tag.TAG_INT_ARRAY) ? Optional.of(tag.getIntArray(key)) : Optional.empty();
	}

	public Optional<ValueInput> child(String key) {
		return tag.contains(key, Tag.TAG_COMPOUND) ? Optional.of(new ValueInput(tag.getCompound(key))) : Optional.empty();
	}

	public ValueInput childOrEmpty(String key) {
		return new ValueInput(tag.getCompound(key));
	}

	public boolean contains(String key) {
		return tag.contains(key);
	}
}
