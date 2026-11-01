package com.example.tudursvehiclemod.compat.fabric.fabric.api.event;

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Function;

public final class EventFactory {
	private EventFactory() {
	}

	@SuppressWarnings("unchecked")
	public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		return new Event<>((List<T> list) -> invokerFactory.apply(list.toArray((T[]) Array.newInstance(type, list.size()))));
	}

	public static <T> Event<T> createArrayBacked(Class<? super T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
		return createArrayBacked(type, invokerFactory);
	}
}
