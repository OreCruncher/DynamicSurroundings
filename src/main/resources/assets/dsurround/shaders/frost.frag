#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
//
// Frost on windows (FrostRenderer, frost.vert), ported from the 26.x line: shows the texels of the frost texture that
// have frosted by how far the frost has grown at this point (the vertex color's alpha). Premultiplied, drawn "over".

uniform sampler2D Sampler0;
uniform sampler2D Lightmap;
// x: the fog's mode: 0 none, 1 linear, 2 exp, 3 exp2
uniform vec4 Fog;

varying vec2 texCoord;
varying vec2 lightCoord;
varying vec4 vertexColor;

// How solid the haze and the brightest crystals are
const float HAZE_ALPHA = 0.3;
const float CRYSTAL_ALPHA = 0.85;
// Frost fades out toward the edge of its range (FrostField.RANGE)
const float FADE_DISTANCE = 32.0;

void main() {
    float grown = vertexColor.a;
    if (grown <= 0.0)
        discard;
    vec4 frost = texture2D(Sampler0, texCoord);
    float soft = fwidth(frost.a) + 0.02;
    float cover = 1.0 - smoothstep(grown - soft, grown + soft, frost.a);
    // Where nothing has grown yet, no frost at all, however soft the edge
    cover *= smoothstep(0.0, 0.05, grown);
    float alpha = cover * mix(HAZE_ALPHA, CRYSTAL_ALPHA, smoothstep(0.3, 1.0, frost.r));
    float visible = 1.0;
    if (Fog.x > 2.5)
        visible = exp(-pow(gl_Fog.density * gl_FogFragCoord, 2.0));
    else if (Fog.x > 1.5)
        visible = exp(-gl_Fog.density * gl_FogFragCoord);
    else if (Fog.x > 0.5)
        visible = (gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale;
    alpha *= clamp(visible, 0.0, 1.0);
    alpha *= 1.0 - smoothstep(FADE_DISTANCE * 0.75, FADE_DISTANCE, gl_FogFragCoord);
    if (alpha <= 0.004)
        discard;
    vec3 color = vertexColor.rgb * texture2D(Lightmap, lightCoord).rgb * mix(0.75, 1.0, frost.r);
    gl_FragColor = vec4(color * alpha, alpha);
}
