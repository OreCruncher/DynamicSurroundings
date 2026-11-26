#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
// Ported from the 26.x line (shaders/core/aurora_cinematic.vsh).

// Dynamic Surroundings cinematic aurora: a quad over the whole of what it draws to, its corners given in clip space
// (CinematicAuroraRenderer). What turns a pixel back into a direction is InvViewProj, worked out in Java. On the far
// plane: drawn straight onto the screen, tested less-or-equal against the depth buffer, it shows only where nothing
// but the sky was drawn.

varying vec2 screenPos;

void main() {
    screenPos = gl_Vertex.xy;
    gl_Position = vec4(gl_Vertex.xy, 1.0, 1.0);
}
