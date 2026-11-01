package com.example.tudursvehiclemod.compat;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

/** 1.20.5+ SynchedEntityData.Builder.define(key, value) on top of 1.20.1's entityData.define(key, value). */
public final class SyncedDataBuilder {
	private final SynchedEntityData data;

	public SyncedDataBuilder(SynchedEntityData data) {
		this.data = data;
	}

	public <T> SyncedDataBuilder define(EntityDataAccessor<T> key, T value) {
		data.define(key, value);
		return this;
	}
}
