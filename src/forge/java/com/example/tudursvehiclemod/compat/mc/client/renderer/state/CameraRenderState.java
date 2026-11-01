package com.example.tudursvehiclemod.compat.mc.client.renderer.state;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/** Forge 1.20.1 stand-in for 1.21.9's CameraRenderState: a snapshot of the main camera. */
public final class CameraRenderState {
	public Vec3 pos = Vec3.ZERO;
	public boolean initialized;

	public static CameraRenderState current() {
		CameraRenderState state = new CameraRenderState();
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		if (camera != null) {
			state.pos = camera.getPosition();
			state.initialized = camera.isInitialized();
		}
		return state;
	}
}
