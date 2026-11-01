package com.example.tudursvehiclemod.client.render;

import com.example.tudursvehiclemod.VehicleMod;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;

/** This mod's own translucent-looking vehicle parts (glass, etc.) render this way rather than as a true alpha-blended translucent layer: approximates partial alpha via per-pixel ordered/Bayer dithering (see shaders/core/entity_dither_cutout.fsh's own doc) instead of true blending - every surviving fragment is either fully drawn or fully discarded, never partially blended, so this uses a normal, depth-WRITING pipeline (same behavior as cutout) instead of a translucent one. Normal GPU depth testing (not draw order) resolves all occlusion correctly this way, exactly like any other opaque/cutout surface - including between different vehicles and between different parts of the SAME vehicle (rotor vs glass vs body). Memoized per texture Identifier (same convention as vanilla's own RenderLayers factory methods), so this applies automatically to any vehicle type's own texture without needing any per-entity-type registration at all. */
public final class DitherCutoutLayers {

	private DitherCutoutLayers() {
	}

	/** Whether translucency is handled by a VANILLA pipeline rather than this mod's own dithered one. True for both "translucent" and "dither_texture". Null-guarded because this class initialises lazily on first render, and there is no guarantee it could never be touched before the client config finishes loading - defaulting to the safe path in that case. */
	static final boolean USE_VANILLA_LAYER = !tudursvehiclemod$mode().equals("dither_shader");

	/** Whether the dither pattern is baked into textures at load, on a vanilla CUTOUT pipeline - see VehicleModConfig.translucencyMode's own doc. Read by AddonTextureLoader, which does the baking. */
	public static final boolean BAKE_DITHER_INTO_TEXTURES = tudursvehiclemod$mode().equals("dither_texture");

	/** Upscale factor applied before baking - see VehicleModConfig.translucencyDitherUpscale's own doc. 1 when texture baking isn't in use at all.
	 *
	 * Read fresh on every call rather than captured once, so changing it takes effect on the next resource reload instead of needing the game restarted. Textures are only re-dithered when they are re-read, which is exactly what a reload does. */
	public static double textureDitherUpscale() {
		var config = com.example.tudursvehiclemod.client.VehicleModClient.getConfig();
		// Values below 1 are allowed and shrink the texture instead, trading pattern quality for memory on weaker hardware. Floored well above zero so a stray value can't collapse a texture to nothing.
		return BAKE_DITHER_INTO_TEXTURES && config != null ? Math.max(0.05, config.translucencyDitherUpscale) : 1.0;
	}

	/** Whether to upload dithered textures as BC1 rather than RGBA8. */
	public static boolean textureDitherGpuCompression() {
		var config = com.example.tudursvehiclemod.client.VehicleModClient.getConfig();
		return BAKE_DITHER_INTO_TEXTURES && config != null && config.translucencyDitherGpuCompression;
	}

	/** Ceiling on the larger side of an upscaled texture - see VehicleModConfig.translucencyDitherMaxTextureSize's own doc. Floored at 16 so a nonsensical value can't disable upscaling entirely by accident. Read fresh for the same reason as textureDitherUpscale() above. */
	public static int textureDitherMaxSize() {
		var config = com.example.tudursvehiclemod.client.VehicleModClient.getConfig();
		return BAKE_DITHER_INTO_TEXTURES && config != null ? Math.max(16, config.translucencyDitherMaxTextureSize) : 16;
	}

	/** This mod's own configured translucency mode, normalised, falling back to the default for an unset config or an unrecognised value rather than failing. */
	private static String tudursvehiclemod$mode() {
		var config = com.example.tudursvehiclemod.client.VehicleModClient.getConfig();
		if (config == null || config.translucencyMode == null) {
			// Matches VehicleModConfig.translucencyMode's own default.
			return "dither_texture";
		}
		String mode = config.translucencyMode.trim().toLowerCase(java.util.Locale.ROOT);
		return switch (mode) {
			case "translucent", "dither_texture" -> mode;
			case "dither_shader" -> "dither_shader";
			default -> "dither_texture";
		};
	}

	/** Every mode now actually culls (see the pipeline changes above - "dither_texture" moved from ENTITY_CUTOUT_NO_CULL to ENTITY_CUTOUT, "dither_shader" from withCull(false) to withCull(true)), so the beam/blades/bands/dashes rendering (which reads this exact flag to decide whether it needs to double its own quad geometry - 8 vertices instead of 4 - since the camera sits inside the cone where back-face culling would otherwise hide half of it) now needs that doubling in every mode, not only "translucent" as before this change. */
	public static final boolean CURRENT_MODE_CULLS = true;

	/** Shader-pack compatibility is already broken, with SEUS PTGI rendering vehicles oversized, entirely black and skewed; and the follow-up request to make the change opt-in: whether the experimental TRIANGLES path is active, read ONCE here when this pipeline is built.
	 *
	 * Defaults to false, which is the original pre-change behaviour. Reverting to false did NOT fix the shader-pack breakage, so this is confirmed not to have been its cause and is kept purely as a performance experiment.
	 *
	 * Forced off whenever a vanilla pipeline is in use: such a pipeline carries its own draw mode, which this mod does not get to choose, so vertex submission must fall back to the four-vertex form there regardless. */
	static final boolean EXPERIMENTAL_TRIANGLES = !USE_VANILLA_LAYER
			&& com.example.tudursvehiclemod.client.VehicleModClient.getConfig() != null
			&& com.example.tudursvehiclemod.client.VehicleModClient.getConfig().experimentalTriangleRendering;

	/** Forge 1.20.1: RenderTypes stand in for the 1.21 RenderPipeline/RenderSetup layers - baked dither (the default) uses
	 * entityCutout (culling, like ENTITY_CUTOUT), "translucent" entityTranslucentCull, and "dither_shader" this mod's own
	 * dither shader (forge.client.DitherRenderTypes, in TRIANGLES mode when EXPERIMENTAL_TRIANGLES is on). */
	private static final Function<ResourceLocation, RenderType> ENTITY_DITHER_CUTOUT = Util.memoize(texture -> {
		if (!USE_VANILLA_LAYER) {
			return com.example.tudursvehiclemod.forge.client.DitherRenderTypes.entityDitherCutout(texture, EXPERIMENTAL_TRIANGLES);
		}
		return tudursvehiclemod$mode().equals("translucent") ? RenderType.entityTranslucentCull(texture) : RenderType.entityCutout(texture);
	});

	/** This is the moment something first draws with a given texture, so it is where that texture is QUEUED for loading if it has not been already.
	 *
	 * Deliberately called on every invocation rather than inside the memoized factory below: the memo survives a resource reload, so putting it there would mean a reloaded texture never got re-registered. requestLoad() only ever does a couple of lookups - the actual loading happens later, from the client tick, because doing it here (mid-frame, inside rendering) caused system-wide GPU driver stalls. See AddonTextureLoader.processPendingLoads() own doc. */
	public static RenderType entityDitherCutout(ResourceLocation texture) {
		AddonTextureLoader.requestLoad(texture);
		return ENTITY_DITHER_CUTOUT.apply(texture);
	}

	/** Unlike the model and the beam, these draw thin, sparse overlays rather than large continuous surfaces, so the sorting risk against water and entities that motivated translucencyMode's own dither options in the first place was judged not to apply here. Fixed to translucent blending unconditionally - no mode branch at all.
	 *
	 * Deliberately a VANILLA pipeline. A custom fragment shader could mask the overlay by a block's own texture alpha, but doing so cost shader-pack compatibility outright - shader packs replace pipelines they recognise, and a mod-specific one is not among them - which was judged too high a price for a partial gain (it worked for leaves but not for plants, and washed lit blocks out into flat colour). */
	private static final Function<ResourceLocation, RenderType> ILLUMINATION_TRANSLUCENT = Util.memoize((Function<ResourceLocation, RenderType>) RenderType::entityTranslucent);

	/** The illumination system's own fixed-translucent layer. Same requestLoad() handling as entityDitherCutout() above, for the same reason. */
	public static RenderType illuminationTranslucent(ResourceLocation texture) {
		AddonTextureLoader.requestLoad(texture);
		return ILLUMINATION_TRANSLUCENT.apply(texture);
	}

}
