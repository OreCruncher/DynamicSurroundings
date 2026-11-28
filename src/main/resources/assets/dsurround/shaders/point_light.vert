#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
//
// A point light (a firefly's), from the 26.x line's point_light.vsh: a small light, lighting whatever is near it on
// screen. All four vertices of a light are at its center, relative to the camera; this spreads them into a square
// facing the camera that covers the light's sphere on screen. When the camera is in or right next to the sphere, the
// square covers the whole screen instead.
//
// The texture coordinates say which corner, at the light's radius (-radius or radius on each axis); the color is the
// light's, its alpha the light's strength.

varying vec3 lightCenter;  // in view space
varying vec4 lightColor;
varying float lightRadius;

void main() {
    vec3 center = (gl_ModelViewMatrix * gl_Vertex).xyz;
    float radius = abs(gl_MultiTexCoord0.x);
    vec2 corner = sign(gl_MultiTexCoord0.xy);
    lightCenter = center;
    lightColor = gl_Color;
    lightRadius = radius;

    float dist = length(center);
    if (dist < radius * 1.05) {
        gl_Position = vec4(corner, 0.0, 1.0);
    } else {
        // A square through the center, square to the view of it, as wide as the sphere looks from here
        vec3 dir = center / dist;
        vec3 up = abs(dir.y) < 0.99 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
        vec3 right = normalize(cross(dir, up));
        up = cross(right, dir);
        float halfSize = radius * dist / sqrt(dist * dist - radius * radius);
        vec3 cornerPosition = center + (right * corner.x + up * corner.y) * halfSize;
        gl_Position = gl_ProjectionMatrix * vec4(cornerPosition, 1.0);
    }

    // At the depth of the sphere's far side, drawn where the scene is nearer than that (GL_GREATER): pixels whose
    // surface is beyond the light's reach (the sky, the ground far behind it) are dropped before this is shaded,
    // which a light around the camera, covering the screen, needs most. Wholly behind the camera, it is moved off
    // the screen.
    float farZ = center.z - radius;
    if (farZ >= 0.0) {
        gl_Position = vec4(2.0, 2.0, 0.0, 1.0);
    } else {
        vec4 farClip = gl_ProjectionMatrix * vec4(0.0, 0.0, farZ, 1.0);
        float farNdc = clamp(farClip.z / farClip.w, -0.99999, 0.99999);
        gl_Position.z = farNdc * gl_Position.w;
    }
}
