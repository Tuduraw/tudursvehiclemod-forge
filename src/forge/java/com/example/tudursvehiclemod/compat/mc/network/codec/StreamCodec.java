package com.example.tudursvehiclemod.compat.mc.network.codec;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Forge 1.20.1 stand-in for 1.21.11's net.minecraft.network.codec.StreamCodec (which 1.20.1 does not have),
 * covering what this mod uses: composite (1-12 fields), unit, ofMember, of, map and apply.
 * The port's rules.json redirects the vanilla import here, so the payload classes stay unchanged.
 */
public interface StreamCodec<B, V> {
	V decode(B buf);

	void encode(B buf, V value);

	static <B, V> StreamCodec<B, V> of(BiConsumer<B, V> encoder, Function<B, V> decoder) {
		return new StreamCodec<>() {
			@Override
			public V decode(B buf) {
				return decoder.apply(buf);
			}

			@Override
			public void encode(B buf, V value) {
				encoder.accept(buf, value);
			}
		};
	}

	/** Same argument order as vanilla: encoder is (value, buf). */
	static <B, V> StreamCodec<B, V> ofMember(BiConsumer<V, B> encoder, Function<B, V> decoder) {
		return of((buf, value) -> encoder.accept(value, buf), decoder);
	}

	static <B, V> StreamCodec<B, V> unit(V value) {
		return of((buf, v) -> { }, buf -> value);
	}

	default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> to, Function<? super O, ? extends V> from) {
		StreamCodec<B, V> self = this;
		return of((buf, o) -> self.encode(buf, from.apply(o)), buf -> to.apply(self.decode(buf)));
	}

	default <O> StreamCodec<B, O> apply(CodecOperation<B, V, O> operation) {
		return operation.apply(this);
	}

	@FunctionalInterface
	interface CodecOperation<B, S, T> {
		StreamCodec<B, T> apply(StreamCodec<B, S> codec);
	}

	static <B, C, T1> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, Function1<T1, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			return factory.apply(v1);
		});
	}

	@FunctionalInterface
	interface Function1<T1, R> {
		R apply(T1 a1);
	}

	static <B, C, T1, T2> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, Function2<T1, T2, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			return factory.apply(v1, v2);
		});
	}

	@FunctionalInterface
	interface Function2<T1, T2, R> {
		R apply(T1 a1, T2 a2);
	}

	static <B, C, T1, T2, T3> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, Function3<T1, T2, T3, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			return factory.apply(v1, v2, v3);
		});
	}

	@FunctionalInterface
	interface Function3<T1, T2, T3, R> {
		R apply(T1 a1, T2 a2, T3 a3);
	}

	static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, Function4<T1, T2, T3, T4, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			return factory.apply(v1, v2, v3, v4);
		});
	}

	@FunctionalInterface
	interface Function4<T1, T2, T3, T4, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4);
	}

	static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, StreamCodec<? super B, T5> c5, Function<C, T5> g5, Function5<T1, T2, T3, T4, T5, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
			c5.encode(buf, g5.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			T5 v5 = c5.decode(buf);
			return factory.apply(v1, v2, v3, v4, v5);
		});
	}

	@FunctionalInterface
	interface Function5<T1, T2, T3, T4, T5, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5);
	}

	static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, StreamCodec<? super B, T5> c5, Function<C, T5> g5, StreamCodec<? super B, T6> c6, Function<C, T6> g6, Function6<T1, T2, T3, T4, T5, T6, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
			c5.encode(buf, g5.apply(value));
			c6.encode(buf, g6.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			T5 v5 = c5.decode(buf);
			T6 v6 = c6.decode(buf);
			return factory.apply(v1, v2, v3, v4, v5, v6);
		});
	}

	@FunctionalInterface
	interface Function6<T1, T2, T3, T4, T5, T6, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5, T6 a6);
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, StreamCodec<? super B, T5> c5, Function<C, T5> g5, StreamCodec<? super B, T6> c6, Function<C, T6> g6, StreamCodec<? super B, T7> c7, Function<C, T7> g7, Function7<T1, T2, T3, T4, T5, T6, T7, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
			c5.encode(buf, g5.apply(value));
			c6.encode(buf, g6.apply(value));
			c7.encode(buf, g7.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			T5 v5 = c5.decode(buf);
			T6 v6 = c6.decode(buf);
			T7 v7 = c7.decode(buf);
			return factory.apply(v1, v2, v3, v4, v5, v6, v7);
		});
	}

	@FunctionalInterface
	interface Function7<T1, T2, T3, T4, T5, T6, T7, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5, T6 a6, T7 a7);
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, StreamCodec<? super B, T5> c5, Function<C, T5> g5, StreamCodec<? super B, T6> c6, Function<C, T6> g6, StreamCodec<? super B, T7> c7, Function<C, T7> g7, StreamCodec<? super B, T8> c8, Function<C, T8> g8, Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
			c5.encode(buf, g5.apply(value));
			c6.encode(buf, g6.apply(value));
			c7.encode(buf, g7.apply(value));
			c8.encode(buf, g8.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			T5 v5 = c5.decode(buf);
			T6 v6 = c6.decode(buf);
			T7 v7 = c7.decode(buf);
			T8 v8 = c8.decode(buf);
			return factory.apply(v1, v2, v3, v4, v5, v6, v7, v8);
		});
	}

	@FunctionalInterface
	interface Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5, T6 a6, T7 a7, T8 a8);
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, StreamCodec<? super B, T5> c5, Function<C, T5> g5, StreamCodec<? super B, T6> c6, Function<C, T6> g6, StreamCodec<? super B, T7> c7, Function<C, T7> g7, StreamCodec<? super B, T8> c8, Function<C, T8> g8, StreamCodec<? super B, T9> c9, Function<C, T9> g9, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
			c5.encode(buf, g5.apply(value));
			c6.encode(buf, g6.apply(value));
			c7.encode(buf, g7.apply(value));
			c8.encode(buf, g8.apply(value));
			c9.encode(buf, g9.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			T5 v5 = c5.decode(buf);
			T6 v6 = c6.decode(buf);
			T7 v7 = c7.decode(buf);
			T8 v8 = c8.decode(buf);
			T9 v9 = c9.decode(buf);
			return factory.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9);
		});
	}

	@FunctionalInterface
	interface Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5, T6 a6, T7 a7, T8 a8, T9 a9);
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, StreamCodec<? super B, T5> c5, Function<C, T5> g5, StreamCodec<? super B, T6> c6, Function<C, T6> g6, StreamCodec<? super B, T7> c7, Function<C, T7> g7, StreamCodec<? super B, T8> c8, Function<C, T8> g8, StreamCodec<? super B, T9> c9, Function<C, T9> g9, StreamCodec<? super B, T10> c10, Function<C, T10> g10, Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
			c5.encode(buf, g5.apply(value));
			c6.encode(buf, g6.apply(value));
			c7.encode(buf, g7.apply(value));
			c8.encode(buf, g8.apply(value));
			c9.encode(buf, g9.apply(value));
			c10.encode(buf, g10.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			T5 v5 = c5.decode(buf);
			T6 v6 = c6.decode(buf);
			T7 v7 = c7.decode(buf);
			T8 v8 = c8.decode(buf);
			T9 v9 = c9.decode(buf);
			T10 v10 = c10.decode(buf);
			return factory.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
		});
	}

	@FunctionalInterface
	interface Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5, T6 a6, T7 a7, T8 a8, T9 a9, T10 a10);
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, StreamCodec<? super B, T5> c5, Function<C, T5> g5, StreamCodec<? super B, T6> c6, Function<C, T6> g6, StreamCodec<? super B, T7> c7, Function<C, T7> g7, StreamCodec<? super B, T8> c8, Function<C, T8> g8, StreamCodec<? super B, T9> c9, Function<C, T9> g9, StreamCodec<? super B, T10> c10, Function<C, T10> g10, StreamCodec<? super B, T11> c11, Function<C, T11> g11, Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
			c5.encode(buf, g5.apply(value));
			c6.encode(buf, g6.apply(value));
			c7.encode(buf, g7.apply(value));
			c8.encode(buf, g8.apply(value));
			c9.encode(buf, g9.apply(value));
			c10.encode(buf, g10.apply(value));
			c11.encode(buf, g11.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			T5 v5 = c5.decode(buf);
			T6 v6 = c6.decode(buf);
			T7 v7 = c7.decode(buf);
			T8 v8 = c8.decode(buf);
			T9 v9 = c9.decode(buf);
			T10 v10 = c10.decode(buf);
			T11 v11 = c11.decode(buf);
			return factory.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
		});
	}

	@FunctionalInterface
	interface Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5, T6 a6, T7 a7, T8 a8, T9 a9, T10 a10, T11 a11);
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> composite(StreamCodec<? super B, T1> c1, Function<C, T1> g1, StreamCodec<? super B, T2> c2, Function<C, T2> g2, StreamCodec<? super B, T3> c3, Function<C, T3> g3, StreamCodec<? super B, T4> c4, Function<C, T4> g4, StreamCodec<? super B, T5> c5, Function<C, T5> g5, StreamCodec<? super B, T6> c6, Function<C, T6> g6, StreamCodec<? super B, T7> c7, Function<C, T7> g7, StreamCodec<? super B, T8> c8, Function<C, T8> g8, StreamCodec<? super B, T9> c9, Function<C, T9> g9, StreamCodec<? super B, T10> c10, Function<C, T10> g10, StreamCodec<? super B, T11> c11, Function<C, T11> g11, StreamCodec<? super B, T12> c12, Function<C, T12> g12, Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> factory) {
		return of((buf, value) -> {
			c1.encode(buf, g1.apply(value));
			c2.encode(buf, g2.apply(value));
			c3.encode(buf, g3.apply(value));
			c4.encode(buf, g4.apply(value));
			c5.encode(buf, g5.apply(value));
			c6.encode(buf, g6.apply(value));
			c7.encode(buf, g7.apply(value));
			c8.encode(buf, g8.apply(value));
			c9.encode(buf, g9.apply(value));
			c10.encode(buf, g10.apply(value));
			c11.encode(buf, g11.apply(value));
			c12.encode(buf, g12.apply(value));
		}, buf -> {
			T1 v1 = c1.decode(buf);
			T2 v2 = c2.decode(buf);
			T3 v3 = c3.decode(buf);
			T4 v4 = c4.decode(buf);
			T5 v5 = c5.decode(buf);
			T6 v6 = c6.decode(buf);
			T7 v7 = c7.decode(buf);
			T8 v8 = c8.decode(buf);
			T9 v9 = c9.decode(buf);
			T10 v10 = c10.decode(buf);
			T11 v11 = c11.decode(buf);
			T12 v12 = c12.decode(buf);
			return factory.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
		});
	}

	@FunctionalInterface
	interface Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> {
		R apply(T1 a1, T2 a2, T3 a3, T4 a4, T5 a5, T6 a6, T7 a7, T8 a8, T9 a9, T10 a10, T11 a11, T12 a12);
	}

}
