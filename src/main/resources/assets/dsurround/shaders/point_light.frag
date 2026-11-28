#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
//
// A point light (a firefly's), from the 26.x line's point_light.fsh: lights the scene already drawn, near the light.
// For each pixel, where in the world it is comes from a copy of the scene's depth, so this lights the grass, leaves and
// ground around the light rather than a flat disc. Added to what is there (see PointLights).

uniform sampler2D DepthSampler;
uniform vec2 ScreenSize;

varying vec3 lightCenter;
varying vec4 lightColor;
varying float lightRadius;

void main() {
    vec2 screen = gl_FragCoord.xy / ScreenSize;
    float depth = texture2D(DepthSampler, screen).r;
    // Nothing there but sky: the depth buffer is cleared to 1, the far end of its range
    if (depth >= 1.0) {
        discard;
    }

    // Where the scene is at this pixel, in view space
    vec4 view = gl_ProjectionMatrixInverse * vec4(screen * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec3 position = view.xyz / view.w;

    vec3 toLight = lightCenter - position;
    float dist = length(toLight);
    if (dist >= lightRadius) {
        discard;
    }

    // Fades smoothly to nothing at the radius
    float falloff = 1.0 - dist / lightRadius;
    falloff *= falloff;

    // Which way the surface faces, from how its position changes between neighboring pixels; turned toward the
    // camera. Surfaces facing the light are lit most, those facing away barely at all, so a wall between the light
    // and the camera mostly stays dark.
    vec3 normal = normalize(cross(dFdx(position), dFdy(position)));
    if (dot(normal, position) > 0.0) {
        normal = -normal;
    }
    float facing = clamp((dot(normal, toLight / max(dist, 0.0001)) + 0.25) / 1.25, 0.0, 1.0);

    // The light's color scaled by its strength: added as it is
    float strength = lightColor.a * falloff * facing;
    gl_FragColor = vec4(lightColor.rgb * strength, 0.0);
}
