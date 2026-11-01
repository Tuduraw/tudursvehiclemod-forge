package com.example.tudursvehiclemod.compat;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.UUID;

/**
 * Small adapters for 1.21.11 calls whose 1.20.1 equivalent has a different shape. rules.json rewrites
 * the call sites (e.g. `new DustParticleOptions(rgb, size)` -> `Compat.dust(rgb, size)`), so the
 * upstream code stays readable and the rewrite survives regeneration.
 */
public final class Compat {
	private Compat() {
	}

	/** 1.21: DustParticleOptions(int rgb, float scale). 1.20.1: (Vector3f color, float scale). */
	public static DustParticleOptions dust(int rgb, float scale) {
		return new DustParticleOptions(Vec3.fromRGB24(rgb & 0xFFFFFF).toVector3f(), Math.max(0.01f, Math.min(4.0f, scale)));
	}

	/** 1.21: DyedItemColor.getOrDefault(stack, fallback). 1.20.1 keeps the dye in the display tag. */
	public static int dyedColor(ItemStack stack, int fallback) {
		CompoundTag display = stack.getTagElement("display");
		return display != null && display.contains("color", 99) ? display.getInt("color") : fallback;
	}

	/** 1.21: Entity.absSnapRotationTo(yaw, pitch). */
	public static void snapRotation(Entity entity, float yaw, float pitch) {
		entity.setYRot(yaw % 360.0f);
		entity.setXRot(Math.max(-90.0f, Math.min(90.0f, pitch)) % 360.0f);
		entity.yRotO = entity.getYRot();
		entity.xRotO = entity.getXRot();
	}

	/** LivingEntity.jumping (set from the player input packet while riding) - protected in 1.20.1. */
	public static boolean isJumping(LivingEntity entity) {
		Boolean value = ObfuscationReflectionHelper.getPrivateValue(LivingEntity.class, entity, "f_20899_");
		return value != null && value;
	}

	/** 1.21: Level.getEntity(UUID). 1.20.1 has it on ServerLevel only (client side: null). */
	public static Entity entityByUuid(Level level, UUID uuid) {
		if (uuid == null) {
			return null;
		}
		if (level instanceof ServerLevel server) {
			return server.getEntity(uuid);
		}
		return null;
	}

	/** 1.21.11 ClientboundPlayerRotationPacket(delta, relative...): turns a player's view by yawDelta without moving them. */
	public static void turnView(net.minecraft.server.level.ServerPlayer player, float yawDelta) {
		player.connection.teleport(0.0, 0.0, 0.0, yawDelta, 0.0f, java.util.EnumSet.allOf(net.minecraft.world.entity.RelativeMovement.class));
	}

	/** Block removal (1.20.1 Block.onRemove): the block entity's preRemoveSideEffects(pos, state) if it has one, otherwise just drop its inventory. */
	public static void preRemove(Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
		net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
		if (be == null) {
			return;
		}
		try {
			java.lang.reflect.Method m = be.getClass().getMethod("preRemoveSideEffects", net.minecraft.core.BlockPos.class, net.minecraft.world.level.block.state.BlockState.class);
			m.invoke(be, pos, state);
			return;
		} catch (NoSuchMethodException ignored) {
			// no cleanup of its own
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		if (be instanceof net.minecraft.world.Container container) {
			net.minecraft.world.Containers.dropContents(level, pos, container);
		}
	}

	// ---- NativeImage: 1.21 getPixel/setPixel are ARGB, 1.20.1 getPixelRGBA/setPixelRGBA are ABGR in memory order.
	private static int swapRB(int c) {
		return (c & 0xFF00FF00) | ((c >> 16) & 0xFF) | ((c & 0xFF) << 16);
	}

	public static int getPixelArgb(com.mojang.blaze3d.platform.NativeImage image, int x, int y) {
		return swapRB(image.getPixelRGBA(x, y));
	}

	public static void setPixelArgb(com.mojang.blaze3d.platform.NativeImage image, int x, int y, int argb) {
		image.setPixelRGBA(x, y, swapRB(argb));
	}

	// ---- 1.21.5 CompoundTag/ListTag getters with defaults (1.20.1 getters already default to 0/""/empty).
	public static String nbtStringOr(CompoundTag tag, String key, String fallback) {
		return tag.contains(key, net.minecraft.nbt.Tag.TAG_STRING) ? tag.getString(key) : fallback;
	}

	public static int nbtIntOr(CompoundTag tag, String key, int fallback) {
		return tag.contains(key, net.minecraft.nbt.Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
	}

	public static long nbtLongOr(CompoundTag tag, String key, long fallback) {
		return tag.contains(key, net.minecraft.nbt.Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : fallback;
	}

	public static float nbtFloatOr(CompoundTag tag, String key, float fallback) {
		return tag.contains(key, net.minecraft.nbt.Tag.TAG_ANY_NUMERIC) ? tag.getFloat(key) : fallback;
	}

	public static double nbtDoubleOr(CompoundTag tag, String key, double fallback) {
		return tag.contains(key, net.minecraft.nbt.Tag.TAG_ANY_NUMERIC) ? tag.getDouble(key) : fallback;
	}

	public static boolean nbtBooleanOr(CompoundTag tag, String key, boolean fallback) {
		return tag.contains(key, net.minecraft.nbt.Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
	}

	/** 1.21.5 getListOrEmpty: any list type (1.20.1 getList needs the element type). */
	public static net.minecraft.nbt.ListTag nbtListOrEmpty(CompoundTag tag, String key) {
		return tag.get(key) instanceof net.minecraft.nbt.ListTag list ? list : new net.minecraft.nbt.ListTag();
	}

	public static CompoundTag nbtCompoundOrEmpty(CompoundTag tag, String key) {
		return tag.getCompound(key);
	}

	public static CompoundTag nbtCompoundOrEmpty(net.minecraft.nbt.ListTag list, int index) {
		return list.getCompound(index);
	}

	public static String nbtStringOr(net.minecraft.nbt.ListTag list, int index, String fallback) {
		return index >= 0 && index < list.size() && list.get(index) instanceof net.minecraft.nbt.StringTag s ? s.getAsString() : fallback;
	}
}
