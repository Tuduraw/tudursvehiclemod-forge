package com.example.tudursvehiclemod.compat.fabric.fabric.api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Forge stand-in for Fabric API's Event: listeners are kept in order; invoker() calls them all. */
public class Event<T> {
	private final List<T> listeners = new ArrayList<>();
	private final Function<List<T>, T> invokerFactory;
	private T invoker;

	public Event(Function<List<T>, T> invokerFactory) {
		this.invokerFactory = invokerFactory;
		this.invoker = invokerFactory.apply(List.of());
	}

	public synchronized void register(T listener) {
		listeners.add(listener);
		invoker = invokerFactory.apply(List.copyOf(listeners));
	}

	public T invoker() {
		return invoker;
	}

	public List<T> listeners() {
		return listeners;
	}
}
