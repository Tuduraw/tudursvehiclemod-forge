package com.example.tudursvehiclemod.compat.mc.gizmos;

/** Forge 1.20.1 stand-in for 1.21.11's net.minecraft.gizmos.GizmoStyle (only the stroke colour is used). */
public record GizmoStyle(int strokeColor, float strokeWidth) {
	public static GizmoStyle stroke(int argb) {
		return new GizmoStyle(argb, 2.0f);
	}

	public static GizmoStyle stroke(int argb, float width) {
		return new GizmoStyle(argb, width);
	}
}
