package com.example.tudursvehiclemod.client.render;

import com.example.tudursvehiclemod.compat.mc.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

/** See VehicleProjectileRenderer's own doc for the two-step render-state pattern this fits into. */
public class VehicleProjectileRenderState extends EntityRenderState {
	public ResourceLocation model;
	public ResourceLocation texture;
	public float scale;
	public float yaw;
	public float pitch;
	public int light;
	/** Per Readme_Weapon.txt's own BulletColor/BulletColorInWater doc - packed ARGB, opaque white (no tint) by default. */
	public int tintColor = 0xFFFFFFFF;
}
