#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
// Ported from the 26.x line (shaders/core/aurora.vsh).

// Dynamic Surroundings aurora: one curtain. The texture coordinate's x runs along the curtain, its y up it (0 at its
// lower edge, 1 at its top), and the vertex alpha fades it toward its ends. The curtains are drawn around the camera,
// as the sky is, after the world (AuroraRenderer).

varying vec2 texCoord0;
varying float vertexAlpha;

void main() {
    vec4 pos = gl_ModelViewProjectionMatrix * gl_Vertex;
    // On the far plane: tested less-or-equal against the depth buffer, the aurora shows only where nothing but the sky
    // was drawn, so terrain and clouds in front of it cover it
    gl_Position = pos.xyww;
    texCoord0 = gl_MultiTexCoord0.xy;
    vertexAlpha = gl_Color.a;
}
