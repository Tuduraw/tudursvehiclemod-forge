package com.example.tudursvehiclemod.compat.mc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Forge 1.20.1 stand-in for 1.21.9's SubmitNodeCollector. 1.21.9 queues draw calls and runs them later;
 * 1.20.1 draws immediately, so a submit simply runs the geometry callback against the buffer source.
 */
public final class SubmitNodeCollector {
	private final MultiBufferSource buffers;

	public SubmitNodeCollector(MultiBufferSource buffers) {
		this.buffers = buffers;
	}

	public MultiBufferSource buffers() {
		return buffers;
	}

	@FunctionalInterface
	public interface CustomGeometryRenderer {
		void render(PoseStack.Pose pose, VertexConsumer consumer);
	}

	public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer renderer) {
		renderer.render(poseStack.last(), buffers.getBuffer(renderType));
	}
}
