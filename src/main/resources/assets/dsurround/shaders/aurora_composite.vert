#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
// Ported from the 26.x line (shaders/core/aurora_composite.vsh).

// Lays the cinematic aurora, drawn at half the screen's size, over the sky: a quad over the whole screen, its corners
// in clip space, on the far plane so terrain and clouds cover it (CinematicAuroraRenderer).

varying vec2 texCoord;

void main() {
    texCoord = gl_Vertex.xy * 0.5 + 0.5;
    gl_Position = vec4(gl_Vertex.xy, 1.0, 1.0);
}
