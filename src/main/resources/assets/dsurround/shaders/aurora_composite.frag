#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
// Ported from the 26.x line (shaders/core/aurora_composite.fsh).

// The aurora's light, read smoothly between the pixels it was drawn at, added to the sky's. Four reads half a pixel
// apart, each between pixels, even out the ordered pattern its samples start in, which one read would only enlarge
// into grain between the rays.

uniform sampler2D AuroraSampler;
// Half a pixel of what the aurora was drawn to, in its texture's coordinates
uniform vec2 HalfPixel;

varying vec2 texCoord;

void main() {
    vec3 light = texture2D(AuroraSampler, texCoord + vec2(-HalfPixel.x, -HalfPixel.y)).rgb
               + texture2D(AuroraSampler, texCoord + vec2(HalfPixel.x, -HalfPixel.y)).rgb
               + texture2D(AuroraSampler, texCoord + vec2(-HalfPixel.x, HalfPixel.y)).rgb
               + texture2D(AuroraSampler, texCoord + vec2(HalfPixel.x, HalfPixel.y)).rgb;
    gl_FragColor = vec4(light * 0.25, 0.0);
}
