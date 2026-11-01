package com.example.tudursvehiclemod.compat.mc.gizmos;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge 1.20.1 stand-in for 1.21.11's per-tick gizmos (Minecraft.collectPerTickGizmos() + Gizmos.line/circle).
 * Lines added inside a scope during a client tick are shown from the next tick start until the one after,
 * drawn as world-space lines after translucent blocks (ForgeClientEvents calls beginTick() and render()).
 */
public final class Gizmos {
	private record Line(Vec3 a, Vec3 b, int argb) {
	}

	private static List<Line> pending = new ArrayList<>();
	private static List<Line> visible = List.of();
	private static int scopes;

	private Gizmos() {
	}

	public static final class Scope implements AutoCloseable {
		private Scope() {
			scopes++;
		}

		@Override
		public void close() {
			scopes--;
		}
	}

	public static Scope collectPerTick() {
		return new Scope();
	}

	public static void line(Vec3 a, Vec3 b, int argb, float width) {
		pending.add(new Line(a, b, argb));
	}

	public static void line(Vec3 a, Vec3 b, int argb) {
		line(a, b, argb, 2.0f);
	}

	public static void circle(Vec3 center, float radius, GizmoStyle style) {
		int n = 32;
		Vec3 prev = null;
		for (int i = 0; i <= n; i++) {
			double t = Math.PI * 2 * i / n;
			Vec3 p = center.add(Math.cos(t) * radius, 0, Math.sin(t) * radius);
			if (prev != null) {
				pending.add(new Line(prev, p, style.strokeColor()));
			}
			prev = p;
		}
	}

	public static void circle(Vec3 center, double radius, GizmoStyle style) {
		circle(center, (float) radius, style);
	}

	/** Client tick start: what the previous tick collected becomes visible. */
	public static void beginTick() {
		visible = pending;
		pending = new ArrayList<>();
	}

	public static void render(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 camera) {
		if (visible.isEmpty()) {
			return;
		}
		VertexConsumer vc = buffers.getBuffer(RenderType.lines());
		PoseStack.Pose pose = poseStack.last();
		for (Line l : visible) {
			float x1 = (float) (l.a.x - camera.x), y1 = (float) (l.a.y - camera.y), z1 = (float) (l.a.z - camera.z);
			float x2 = (float) (l.b.x - camera.x), y2 = (float) (l.b.y - camera.y), z2 = (float) (l.b.z - camera.z);
			float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
			float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (len < 1.0e-6f) {
				continue;
			}
			dx /= len;
			dy /= len;
			dz /= len;
			int a = (l.argb >>> 24) & 0xFF, r = (l.argb >> 16) & 0xFF, g = (l.argb >> 8) & 0xFF, b = l.argb & 0xFF;
			if (a == 0) {
				a = 255;
			}
			vc.vertex(pose.pose(), x1, y1, z1).color(r, g, b, a).normal(pose.normal(), dx, dy, dz).endVertex();
			vc.vertex(pose.pose(), x2, y2, z2).color(r, g, b, a).normal(pose.normal(), dx, dy, dz).endVertex();
		}
		buffers.endBatch(RenderType.lines());
	}
}
