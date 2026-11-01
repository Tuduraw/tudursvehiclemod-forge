package com.example.tudursvehiclemod.compat.mc.core.component;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Forge 1.20.1 stand-in for 1.20.5's item data components: the value is kept in the stack's NBT under
 * "tudursvehiclemod:<path>", encoded with the component's codec. rules.json rewrites
 * `stack.get(ModComponents.X)` to `ModComponents.X.get(stack)` (and set/has/remove/getOrDefault).
 */
public final class DataComponentType<T> {
	private final Codec<T> codec;
	private String key = "";

	private DataComponentType(Codec<T> codec) {
		this.codec = codec;
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public static <T> DataComponentType<T> register(ResourceLocation id, DataComponentType<T> type) {
		type.key = id.toString();
		return type;
	}

	public T get(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(key)) {
			return null;
		}
		return codec.parse(NbtOps.INSTANCE, tag.get(key)).result().orElse(null);
	}

	public T getOrDefault(ItemStack stack, T fallback) {
		T value = get(stack);
		return value != null ? value : fallback;
	}

	public boolean has(ItemStack stack) {
		return stack.getTag() != null && stack.getTag().contains(key);
	}

	public void set(ItemStack stack, T value) {
		if (value == null) {
			remove(stack);
			return;
		}
		Tag encoded = codec.encodeStart(NbtOps.INSTANCE, value).result().orElse(null);
		if (encoded != null) {
			stack.getOrCreateTag().put(key, encoded);
		}
	}

	public void remove(ItemStack stack) {
		if (stack.getTag() != null) {
			stack.getTag().remove(key);
		}
	}

	public static final class Builder<T> {
		private Codec<T> codec;

		public Builder<T> persistent(Codec<T> codec) {
			this.codec = codec;
			return this;
		}

		public Builder<T> networkSynchronized(Object ignored) {
			return this;
		}

		public DataComponentType<T> build() {
			return new DataComponentType<>(codec);
		}
	}
}
