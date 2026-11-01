package com.example.tudursvehiclemod.forge.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

/**
 * Forge 1.20.1 version of the 1.21.11 "dither_shader" pipeline (client.render.DitherCutoutLayers): an entity-cutout render
 * type whose fragment shader discards by a 4x4 dither pattern instead of a fixed alpha cutoff
 * (assets/tudursvehiclemod/shaders/core/rendertype_entity_dither_cutout.*). The shader is loaded in ForgeClientEvents
 * (RegisterShadersEvent). Extends RenderType only to reach its protected state shards.
 */
public final class DitherRenderTypes extends RenderType {
	public static final ResourceLocation SHADER_ID = new ResourceLocation("tudursvehiclemod", "rendertype_entity_dither_cutout");
	public static ShaderInstance shader;

	private static final ShaderStateShard DITHER_SHADER = new ShaderStateShard(() -> shader);

	private DitherRenderTypes() {
		super("tudursvehiclemod_unused", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, () -> {
		}, () -> {
		});
	}

	/** Same state as vanilla entityCutout (culling on, as CURRENT_MODE_CULLS says), with the dither shader; TRIANGLES for the
	 * experimental triangle path. */
	public static RenderType entityDitherCutout(ResourceLocation texture, boolean triangles) {
		CompositeState state = CompositeState.builder()
				.setShaderState(DITHER_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return create("tudursvehiclemod_entity_dither_cutout", DefaultVertexFormat.NEW_ENTITY,
				triangles ? VertexFormat.Mode.TRIANGLES : VertexFormat.Mode.QUADS, 256, true, false, state);
	}
}
