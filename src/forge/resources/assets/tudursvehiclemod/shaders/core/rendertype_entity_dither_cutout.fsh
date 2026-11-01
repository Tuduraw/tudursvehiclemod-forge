#version 150

// Forge 1.20.1 port of shaders/core/entity_dither_cutout.fsh (1.21.11 "dither_shader" translucency mode): vanilla
// 1.20.1 rendertype_entity_cutout with the fixed 0.1 alpha cutoff replaced by a 4x4 ordered (Bayer) dither threshold,
// so partial alpha reads as "screen-door" transparency on a depth-writing pipeline. Vertex shader: vanilla
// rendertype_entity_cutout.vsh.

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

float tudursvehiclemod_ditherThreshold(vec2 fragCoord) {
    int x = int(mod(fragCoord.x, 4.0));
    int y = int(mod(fragCoord.y, 4.0));
    int index = y * 4 + x;
    float bayer[16] = float[16](
        0.0, 8.0, 2.0, 10.0,
        12.0, 4.0, 14.0, 6.0,
        3.0, 11.0, 1.0, 9.0,
        15.0, 7.0, 13.0, 5.0
    );
    return (bayer[index] + 0.5) / 16.0;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < tudursvehiclemod_ditherThreshold(gl_FragCoord.xy)) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    color.a = 1.0;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    fragColor.a = 1.0;
}
