#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
//
// Particle rain and snow made on the GPU (GpuDrops): the drops are uploaded once a tick, four vertices each carrying
// the drop's record and which corner it is, and this makes each frame's quad from them, for the frame's camera and
// partial tick, as DropRenderer.build does on the CPU (whose math this follows step for step; keep them alike). GLSL
// 1.20 and plain vertex attributes, as this line keeps to OpenGL 2.1: no texel buffers or vertex index, as the 26.x
// line's GPU drops use.

// Where the drop was last tick and how far it has moved since, from the anchor the drops were packed around
attribute vec3 Prev;
attribute vec3 Move;
// Rain: the way it falls, a unit vector. Snow: the cos and sin of its turn as of its last tick.
attribute vec3 Direction;
// x: kind (0 rain, 1 snow); y: corner, 0 to 3; z: size, 0 to 1; w: its fixed random number for thinning
attribute vec4 Info;
// x: ticks old; y: ticks under cover (0 in the open); z: lifetime (dust only); w: a flake's size as a share of the
// largest's
attribute vec4 Life;
// xy: the corner's texture coordinate (x below 0 for none: rain and plain flakes); zw: block and sky light
attribute vec4 TexLight;

// xyz: the anchor from the camera; w: the partial tick
uniform vec4 Offset;
// The camera's left and up, as particles are turned
uniform vec3 Left;
uniform vec3 Up;
// x: the field's radius
uniform vec4 Field;
uniform vec3 RainColor;

varying vec2 texCoord;
varying vec2 lightCoord;
varying vec4 vertexColor;

// PrecipitationField's and DropRenderer's constants
const float RAIN_FADE_IN = 3.0;
const float SNOW_FADE_IN = 15.0;
const float RAIN_COVER_TICKS = 8.0;
const float SNOW_COVER_TICKS = 40.0;
const float SNOW_SWAY_RATE = 0.12;
const float SNOW_SPIN = 0.25;
const float RAIN_MIN_LENGTH = 0.35;
const float RAIN_LENGTH_RANGE = 0.65;
const float RAIN_MIN_HALF_WIDTH = 0.015;
const float RAIN_HALF_WIDTH_RANGE = 0.015;
const float RAIN_MIN_ALPHA = 0.25;
const float RAIN_ALPHA_RANGE = 0.25;
const float SNOW_MIN_HALF_SIZE = 0.05;
const float SNOW_HALF_SIZE_RANGE = 0.07;
const float SNOW_ALPHA = 0.9;
const float THIN_FROM_SHARE = 1.0 / 3.0;
const float THIN_KEEP_AT_EDGE = 0.2;
const float THIN_FADE = 0.1;
const float EDGE_FADE = 2.0;

// PrecipitationField.fade, for rain and snow
float fade(bool snow, float ticksOld, float covered, float partialTick) {
    float age = ticksOld + partialTick;
    float f = min(1.0, age / (snow ? SNOW_FADE_IN : RAIN_FADE_IN));
    if (covered > 0.0)
        f *= clamp(1.0 - (covered - 1.0 + partialTick) / (snow ? SNOW_COVER_TICKS : RAIN_COVER_TICKS), 0.0, 1.0);
    return f;
}

// DropRenderer.thinned
float thinned(float thinning, float distance, float radius) {
    float from = radius * THIN_FROM_SHARE;
    float share = mix(1.0, THIN_KEEP_AT_EDGE, clamp((distance - from) / (radius - from), 0.0, 1.0));
    return clamp((share - thinning) / THIN_FADE + 1.0, 0.0, 1.0);
}

void main() {
    float partialTick = Offset.w;
    vec3 p = Prev + partialTick * Move + Offset.xyz;
    bool snow = Info.x > 0.5;
    int corner = int(Info.y + 0.5);
    float size = Info.z;
    float distance = length(p.xz);
    float radius = Field.x;
    float f = fade(snow, Life.x, Life.y, partialTick) * clamp((radius - distance) / EDGE_FADE, 0.0, 1.0)
            * thinned(Info.w, distance, radius);

    vec3 position = p;
    float alpha = 0.0;
    vec3 color = vec3(1.0);
    if (f > 0.0) {
        if (snow) {
            // Its turn as of its last tick, and how much further it has turned since
            float turn = partialTick * SNOW_SWAY_RATE * SNOW_SPIN;
            float turnCos = cos(turn), turnSin = sin(turn);
            float c = Direction.x * turnCos - Direction.y * turnSin;
            float s = Direction.y * turnCos + Direction.x * turnSin;
            float halfSize = (SNOW_MIN_HALF_SIZE + SNOW_HALF_SIZE_RANGE * size) * Life.w;
            vec3 a = (Left * c + Up * s) * halfSize;
            vec3 b = (Up * c - Left * s) * halfSize;
            if (corner == 0)
                position = p - a - b;
            else if (corner == 1)
                position = p - a + b;
            else if (corner == 2)
                position = p + a + b;
            else
                position = p + a - b;
            alpha = SNOW_ALPHA * f;
        } else {
            float len = RAIN_MIN_LENGTH + RAIN_LENGTH_RANGE * size;
            float halfWidth = RAIN_MIN_HALF_WIDTH + RAIN_HALF_WIDTH_RANGE * size;
            vec3 v = Direction;
            // Across the streak: square to both its length and the line to the camera
            vec3 across = cross(v, p);
            float l = dot(across, across);
            across = l > 1.0e-6 ? across * (halfWidth * inversesqrt(l)) : Left * halfWidth;
            vec3 tail = p - v * len;
            bool head = corner == 0 || corner == 3;
            vec3 end = head ? p : tail;
            position = corner < 2 ? end - across : end + across;
            // A streak's tail is clear
            alpha = head ? (RAIN_MIN_ALPHA + RAIN_ALPHA_RANGE * size) * f : 0.0;
            color = RainColor;
        }
    }

    vec4 eye = gl_ModelViewMatrix * vec4(position, 1.0);
    gl_Position = gl_ProjectionMatrix * eye;
    gl_FogFragCoord = length(eye.xyz);
    texCoord = TexLight.xy;
    // As vanilla's lightmap texture matrix: a sixteenth of the packed light, at the middle of a texel
    lightCoord = (TexLight.zw + 8.0) / 256.0;
    vertexColor = vec4(color, alpha);
}
