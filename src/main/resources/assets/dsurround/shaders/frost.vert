#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
//
// Frost on windows (FrostRenderer): quads in vanilla's particle vertex format, positions relative to the camera. The
// color's alpha is not an alpha but how far the frost has grown there, so the light is applied to the color alone, in
// frost.frag.

varying vec2 texCoord;
varying vec2 lightCoord;
varying vec4 vertexColor;

void main() {
    vec4 eye = gl_ModelViewMatrix * gl_Vertex;
    gl_Position = gl_ProjectionMatrix * eye;
    gl_FogFragCoord = length(eye.xyz);
    texCoord = gl_MultiTexCoord0.xy;
    // As vanilla's lightmap texture matrix: a sixteenth of the packed light, at the middle of a texel
    lightCoord = (gl_MultiTexCoord1.xy + 8.0) / 256.0;
    vertexColor = gl_Color;
}
