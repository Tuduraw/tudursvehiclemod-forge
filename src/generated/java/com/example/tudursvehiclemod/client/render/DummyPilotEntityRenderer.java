package com.example.tudursvehiclemod.client.render;

import net.minecraft.resources.ResourceLocation;
import com.example.tudursvehiclemod.entity.DummyPilotEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

/** Renders DummyPilotEntity with its own completely standalone model (DummyPilotModel) - see that class's own doc for why it shares nothing with PlayerEntityModel - and simply swaps in whichever skin that particular pilot is configured with (see getTexture()).
 *
 * No feature renderers are added at all (no armor layer, no held-item layer, no cape): this pilot has no equipment by design (see DummyPilotEntity's own getEquippedStack()), so every one of those would render nothing while still costing a pass. */
public class DummyPilotEntityRenderer extends LivingEntityRenderer<DummyPilotEntity, DummyPilotModel> {

	// Forge 1.20.1: renders straight from the entity (no render-state extraction step), so the skin is read from the entity itself.

	public DummyPilotEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new DummyPilotModel(context.bakeLayer(DummyPilotModelLayers.DUMMY_PILOT)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(DummyPilotEntity entity) {
		// Resolves through DummyPilotSkins so an addon-supplied skin gets registered on first use, and anything unknown/unreadable falls back to the built-in default - see that class's own doc.
		return DummyPilotSkins.tudursvehiclemod$getTexture(entity.tudursvehiclemod$getSkinId());
	}

	/** Only a custom name the player chose to make visible is shown (vanilla would also show it while the crosshair is on the pilot). */
	@Override
	protected boolean shouldShowName(DummyPilotEntity entity) {
		return entity.isCustomNameVisible() && super.shouldShowName(entity);
	}
}
