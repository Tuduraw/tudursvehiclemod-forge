package com.example.tudursvehiclemod.compat.mc.client.renderer.entity.state;

import net.minecraft.world.entity.EntityType;

/** Forge 1.20.1 stand-in for 1.21.2's EntityRenderState (filled by compat EntityRenderer before drawing). */
public class EntityRenderState {
	public EntityType<?> entityType;
	public double x;
	public double y;
	public double z;
	public float ageInTicks;
	public float partialTick;
	public float boundingBoxWidth;
	public float boundingBoxHeight;
	public float eyeHeight;
	public double distanceToCameraSq;
	public boolean isInvisible;
	public int lightCoords;
}
