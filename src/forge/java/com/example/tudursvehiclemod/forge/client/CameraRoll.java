package com.example.tudursvehiclemod.forge.client;

/**
 * 1.20.1 builds the view matrix from the camera's yaw/pitch plus the roll of ViewportEvent.ComputeCameraAngles,
 * not from Camera.rotation() as 1.21.11 does. CameraMixin (patched) therefore sets yaw/pitch on the camera and
 * leaves the roll here; ForgeClientEvents hands it to the event for the same frame.
 */
public final class CameraRoll {
	private static float roll;
	private static boolean pending;

	private CameraRoll() {
	}

	public static void set(float degrees) {
		roll = degrees;
		pending = true;
	}

	/** The roll for this frame, or NaN when the camera was not touched by the mod (vanilla roll stays). */
	public static float take() {
		if (!pending) {
			return Float.NaN;
		}
		pending = false;
		return roll;
	}
}
