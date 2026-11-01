package com.example.tudursvehiclemod.forge.mixin.client;

import com.example.tudursvehiclemod.entity.AbstractVehicleEntity;
import com.example.tudursvehiclemod.entity.FreeCameraVehicle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge 1.20.1 version of the 1.21.11 LivingEntityRendererMixin / LivingEntityRenderStateMixin pair. 1.20.1 renders straight
 * from the entity (no render state), so the values are read from the entity at each step instead of being captured first.
 *
 * <p>A living entity riding any vehicle is drawn as a rigid part of it: its body turns with the vehicle's yaw and tilts with its
 * pitch and roll, the head faces forward on a FreeCameraVehicle outside free-look, MC Heli's HideEntity (and a remote-control
 * pilot) hides it, and EntityWidth/EntityHeight scale it.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererForgeMixin {

	private static AbstractVehicleEntity tudursvehiclemod$vehicleOf(LivingEntity entity) {
		return entity.getVehicle() instanceof AbstractVehicleEntity vehicle ? vehicle : null;
	}

	/** HideEntity: skip drawing entirely (the passenger stays present and interactive). */
	@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("HEAD"), cancellable = true)
	private void tudursvehiclemod$hideIfConfigured(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffers, int light, CallbackInfo ci) {
		AbstractVehicleEntity vehicle = tudursvehiclemod$vehicleOf(entity);
		if (vehicle != null && (vehicle.getDefinition().hideEntity() || vehicle.tudursvehiclemod$isRemoteControlHiddenPilot(entity))) {
			ci.cancel();
		}
	}

	/** The head faces straight ahead (matching the body) on a FreeCameraVehicle while not in free-look. Two @ModifyArg rather
	 * than @ModifyArgs: the latter's generated Args class can't be loaded under Forge's module layers. */
	@ModifyArg(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"), index = 4)
	private float tudursvehiclemod$lockHeadYaw(net.minecraft.world.entity.Entity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		return tudursvehiclemod$headLocked(entity) ? 0f : netHeadYaw;
	}

	@ModifyArg(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"), index = 5)
	private float tudursvehiclemod$lockHeadPitch(net.minecraft.world.entity.Entity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		return tudursvehiclemod$headLocked(entity) ? 0f : headPitch;
	}

	private static boolean tudursvehiclemod$headLocked(net.minecraft.world.entity.Entity entity) {
		return entity.getVehicle() instanceof AbstractVehicleEntity vehicle && vehicle instanceof FreeCameraVehicle
				&& !vehicle.tudursvehiclemod$isEffectiveFreeLook(entity);
	}

	/** Body yaw = the vehicle's. */
	@ModifyVariable(method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
			at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private float tudursvehiclemod$overrideBodyYaw(float rotationYaw, LivingEntity entity, PoseStack poseStack, float ageInTicks,
			float rotationYawArg, float partialTicks) {
		AbstractVehicleEntity vehicle = tudursvehiclemod$vehicleOf(entity);
		return vehicle != null ? vehicle.getViewYRot(partialTicks) : rotationYaw;
	}

	/** Pitch and roll of the vehicle, then EntityWidth/EntityHeight. */
	@Inject(method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At("TAIL"))
	private void tudursvehiclemod$applyVehicleTilt(LivingEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw,
			float partialTicks, CallbackInfo ci) {
		AbstractVehicleEntity vehicle = tudursvehiclemod$vehicleOf(entity);
		if (vehicle == null) {
			return;
		}
		// Both negated relative to the vehicle's own getXRot()/getRoll(), as in 1.21.11.
		poseStack.mulPose(new Quaternionf()
				.rotateX((float) Math.toRadians(-vehicle.getViewXRot(partialTicks)))
				.rotateZ((float) Math.toRadians(-vehicle.getRoll(partialTicks))));
		var def = vehicle.getDefinition();
		if (def.entityWidth() != 1f || def.entityHeight() != 1f) {
			poseStack.scale(def.entityWidth(), def.entityHeight(), def.entityWidth());
		}
	}
}
