package com.example.tudursvehiclemod.compat.mc.network.codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** Forge 1.20.1 stand-in for 1.21.11's ByteBufCodecs - only the codecs this mod uses. */
public final class ByteBufCodecs {
	private ByteBufCodecs() {
	}

	public static final StreamCodec<FriendlyByteBuf, Integer> VAR_INT = StreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
	public static final StreamCodec<FriendlyByteBuf, Integer> INT = StreamCodec.of((b, v) -> b.writeInt(v), FriendlyByteBuf::readInt);
	public static final StreamCodec<FriendlyByteBuf, Float> FLOAT = StreamCodec.of((b, v) -> b.writeFloat(v), FriendlyByteBuf::readFloat);
	public static final StreamCodec<FriendlyByteBuf, Double> DOUBLE = StreamCodec.of((b, v) -> b.writeDouble(v), FriendlyByteBuf::readDouble);
	public static final StreamCodec<FriendlyByteBuf, Boolean> BOOL = StreamCodec.of((b, v) -> b.writeBoolean(v), FriendlyByteBuf::readBoolean);
	public static final StreamCodec<FriendlyByteBuf, String> STRING_UTF8 = StreamCodec.of((b, v) -> b.writeUtf(v), b -> b.readUtf());
	public static final StreamCodec<FriendlyByteBuf, CompoundTag> COMPOUND_TAG = StreamCodec.of(FriendlyByteBuf::writeNbt, FriendlyByteBuf::readNbt);
	/** Replaces 1.21.11's BlockPos.STREAM_CODEC (see rules.json). */
	public static final StreamCodec<FriendlyByteBuf, BlockPos> BLOCK_POS = StreamCodec.of(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
	/** Replaces 1.21.11's Identifier.STREAM_CODEC. */
	public static final StreamCodec<FriendlyByteBuf, ResourceLocation> RESOURCE_LOCATION = StreamCodec.of(FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::readResourceLocation);
	/** Replaces 1.21.11's GlobalPos.STREAM_CODEC. */
	public static final StreamCodec<FriendlyByteBuf, GlobalPos> GLOBAL_POS = StreamCodec.of(FriendlyByteBuf::writeGlobalPos, FriendlyByteBuf::readGlobalPos);

	public static <B extends FriendlyByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
		return element -> StreamCodec.of((buf, values) -> {
			buf.writeVarInt(values.size());
			for (V v : values) {
				element.encode(buf, v);
			}
		}, buf -> {
			int n = buf.readVarInt();
			List<V> out = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				out.add(element.decode(buf));
			}
			return out;
		});
	}
}
