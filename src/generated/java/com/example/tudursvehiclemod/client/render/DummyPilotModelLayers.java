package com.example.tudursvehiclemod.client.render;

import com.example.tudursvehiclemod.compat.fabric.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/** Registers the dummy pilot's own dedicated, standalone model layer - see DummyPilotModel's own doc for why this shares no geometry or construction code with vanilla's player model at all. */
public final class DummyPilotModelLayers {

	public static final ModelLayerLocation DUMMY_PILOT =
			new ModelLayerLocation(new net.minecraft.resources.ResourceLocation("tudursvehiclemod", "dummy_pilot"), "main");

	private DummyPilotModelLayers() {
	}

	/** Called once from VehicleModClient's own client-init, before any DummyPilotEntityRenderer is constructed (registration must happen before EntityRendererFactory.Context.getPart() can resolve this layer at all). */
	public static void register() {
		EntityModelLayerRegistry.registerModelLayer(DUMMY_PILOT, DummyPilotModel::getTexturedModelData);
	}
}
