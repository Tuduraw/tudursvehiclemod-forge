package com.example.tudursvehiclemod.compat.mc.client.renderer.entity;

import com.example.tudursvehiclemod.compat.mc.client.renderer.SubmitNodeCollector;
import com.example.tudursvehiclemod.compat.mc.client.renderer.entity.state.EntityRenderState;
import com.example.tudursvehiclemod.compat.mc.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Runs a 1.21.11-style renderer (createRenderState / extractRenderState / render(state, ...)) on top of
 * 1.20.1's immediate EntityRenderer: every frame the state is created and filled, then drawn at once.
 */
public abstract class EntityRenderer<E extends Entity, S extends EntityRenderState> extends net.minecraft.client.renderer.entity.EntityRenderer<E> {
	private S lastState;
	private E lastEntity;

	protected EntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public abstract S createRenderState();

	public void extractRenderState(E entity, S state, float partialTick) {
		state.entityType = entity.getType();
		state.x = Mth.lerp(partialTick, entity.xOld, entity.getX());
		state.y = Mth.lerp(partialTick, entity.yOld, entity.getY());
		state.z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
		state.ageInTicks = entity.tickCount + partialTick;
		state.partialTick = partialTick;
		state.boundingBoxWidth = entity.getBbWidth();
		state.boundingBoxHeight = entity.getBbHeight();
		state.eyeHeight = entity.getEyeHeight();
		state.distanceToCameraSq = this.entityRenderDispatcher.distanceToSqr(entity);
		state.isInvisible = entity.isInvisible();
	}

	/** 1.21.9+ submit(state, ...): draws from the state; the default draws nothing (name tags are left to vanilla). */
	public void submit(S state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
	}

	public Vec3 getRenderOffset(S state) {
		return Vec3.ZERO;
	}

	private S stateFor(E entity, float partialTick) {
		if (lastEntity != entity || lastState == null || lastState.partialTick != partialTick) {
			S state = createRenderState();
			extractRenderState(entity, state, partialTick);
			lastState = state;
			lastEntity = entity;
		}
		return lastState;
	}

	@Override
	public Vec3 getRenderOffset(E entity, float partialTick) {
		return getRenderOffset(stateFor(entity, partialTick));
	}

	@Override
	public void render(E entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
		S state = stateFor(entity, partialTick);
		state.lightCoords = light;
		submit(state, poseStack, new SubmitNodeCollector(buffers), CameraRenderState.current());
		lastState = null;
		lastEntity = null;
		super.render(entity, yaw, partialTick, poseStack, buffers, light);
	}

	@Override
	@SuppressWarnings("deprecation")
	public ResourceLocation getTextureLocation(E entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
