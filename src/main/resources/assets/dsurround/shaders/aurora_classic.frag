#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
// Ported from the 26.x line (shaders/core/aurora.fsh, include/noise.glsl).

// Dynamic Surroundings aurora: one curtain of light, drawn with additive blending.
//
// texCoord0.x runs along the curtain (in sky units, roughly 6 to 10 across the whole curtain) and texCoord0.y up it,
// from 0 at the lower edge to 1 at the top. The look:
//  - fine vertical rays: noise that changes quickly along the curtain and slowly up it
//  - large slow folds, which bend the rays sideways as they drift
//  - a sharp, bright lower edge, with the light dying away above it (further up along the brighter rays)
//  - color by height: BottomColor at the edge, through MiddleColor, to TopColor where it fades out
//  - patches of brightness traveling along the curtain, and a faint flicker

uniform vec3 BottomColor;
uniform vec3 MiddleColor;
uniform vec3 TopColor;
uniform float AuroraTime;  // seconds
uniform float Alpha;       // overall strength, 0 to 1

// A random value a texel, read smoothly between texels and repeating (AuroraRenderer.noise)
uniform sampler2D NoiseSampler;

varying vec2 texCoord0;
varying float vertexAlpha;

// The noise texture's size, which it repeats at (AuroraRenderer.NOISE_SIZE): wider than a curtain's finest noise
const float NOISE_SIZE = 512.0;

// Smooth value noise, 0 to 1, a cell to a texel of the noise texture, repeating every NOISE_SIZE cells. The texture is
// read between texels in a straight line; moving the point within its cell along an S-curve first rounds that off, as
// working out the four corners would, in one read
float noise(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return texture2D(NoiseSampler, (cell + f + 0.5) / NOISE_SIZE).r;
}

void main() {
    float x = texCoord0.x;
    float v = clamp(texCoord0.y, 0.0, 1.0);
    float t = AuroraTime;

    // Folds: shift where along the curtain the rays are taken from, slowly and by a lot, so they sway and bunch
    float fold = (noise(vec2(x * 0.6, t * 0.05)) - 0.5) * 1.0
               + (noise(vec2(x * 1.7 + 13.0, t * 0.11)) - 0.5) * 0.35;
    float rx = x + fold;

    // Rays: noise stretched up the curtain, two sizes of it, drifting
    float rays = noise(vec2(rx * 9.0, v * 0.6 + t * 0.15)) * 0.65
               + noise(vec2(rx * 23.0 - t * 0.4, v * 1.2 + 7.0)) * 0.35;
    rays = smoothstep(0.25, 0.95, rays);

    // The lower edge, waving a little
    float edge = 0.04 + 0.08 * noise(vec2(x * 2.5, t * 0.3));
    float above = max(v - edge, 0.0);
    float lower = smoothstep(edge - 0.03, edge + 0.02, v);
    // Light dies away above the edge: slowly along bright rays, quickly between them; plus a glow right at the edge
    float decay = mix(1.4, 3.6, 1.0 - rays);
    float glow = exp(-above * 18.0);
    float profile = lower * (exp(-above * decay) + 0.6 * glow);
    profile *= 1.0 - smoothstep(0.75, 1.0, v);

    // Bright patches traveling along the curtain, and a faint flicker
    float pulse = 0.55 + 0.45 * noise(vec2(x * 0.45 - t * 0.35, t * 0.07));
    float flicker = 0.9 + 0.1 * noise(vec2(x * 4.0, t * 4.0));

    float intensity = profile * (0.25 + 0.75 * rays) * pulse * flicker * vertexAlpha * Alpha;

    vec3 color = v < 0.3
        ? mix(BottomColor, MiddleColor, smoothstep(0.0, 0.3, v))
        : mix(MiddleColor, TopColor, smoothstep(0.3, 0.7, v));
    // The lower edge burns a little whiter
    color = mix(color, vec3(1.0), 0.25 * lower * glow);

    gl_FragColor = vec4(color, clamp(intensity, 0.0, 1.0));
}
