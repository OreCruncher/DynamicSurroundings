#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
//
// Particle rain and snow made on the GPU (GpuDrops, drops.vert): the snow texture for flakes, none for rain, lit by
// the lightmap and fogged as vanilla's fixed-function pipeline does

uniform sampler2D Sampler0;
uniform sampler2D Lightmap;
// x: the fog's mode: 0 none, 1 linear, 2 exp, 3 exp2
uniform vec4 Fog;

varying vec2 texCoord;
varying vec2 lightCoord;
varying vec4 vertexColor;

void main() {
    vec4 color = vertexColor;
    // Rain streaks and plain flakes have no texture: their coordinates are below zero
    if (texCoord.x >= 0.0)
        color *= texture2D(Sampler0, texCoord);
    color.rgb *= texture2D(Lightmap, lightCoord).rgb;
    if (color.a < 0.0039)
        discard;
    float visible = 1.0;
    if (Fog.x > 2.5)
        visible = exp(-pow(gl_Fog.density * gl_FogFragCoord, 2.0));
    else if (Fog.x > 1.5)
        visible = exp(-gl_Fog.density * gl_FogFragCoord);
    else if (Fog.x > 0.5)
        visible = (gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale;
    color.rgb = mix(gl_Fog.color.rgb, color.rgb, clamp(visible, 0.0, 1.0));
    gl_FragColor = color;
}
