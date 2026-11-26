#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
// Made from the 26.x line's shaders/core/aurora_cinematic.fsh by scripts in its port: GLSL 1.20, uniforms in place of
// its uniform block, the noise function inlined, ints clamped as floats.

// Dynamic Surroundings cinematic aurora (CinematicAuroraRenderer), drawn over the sky with its light added to it: the
// stylized ribbons of photographs and games, in one signature palette.
//
// Each pixel's line of sight goes up from a viewer on the ground of a round Earth, through the layer the ribbons hang in
// (SHELL_BOTTOM to SHELL_TOP kilometers up, no further away than FARTHEST). A ribbon is a curtain hanging straight down
// from a folded line in that layer, wound into a spiral or a hook where it curls: a soft sheet, its light falling away across it as a bell curve THICKNESS wide. The
// line of sight is stepped along, and each step adds how much of the sheet it goes through there, worked out exactly
// for a line crossing a bell curve evenly (sheet_path), so no step misses a ribbon, and one seen edge on (where it folds
// toward the viewer, or low toward the horizon) brightens of itself. What it sees there:
//  - color by height in the curtain: yellow-green at its lower edge, emerald above, violet toward its top
//  - rays: streaks hanging down the curtain, crisp in some ribbons and smoky in others, moving along it
//  - a narrow bright line along its lower edge, the glow above it fainter, as photographs show a curtain
//  - brighter and dimmer stretches along it, and gaps, and now and then a stretch far brighter than the rest
//  - a wider, fainter violet sheet around it: its halo
// Then the air dimming and yellowing what is low, and all of it brought into range so the brightest parts pale toward
// white rather than cutting off. Photographs show violet only at the tops of rays, never apart from them, and no band
// along the horizon (the purple glow over the sky and the pink band came from AI-made pictures, and went).

// What the shader is given (CinematicAuroraRenderer, CinematicAuroraFrame). Distances are in kilometers; the ground's
// directions are the world's x and z.
#define MAX_RIBBONS 4
// From the screen back to directions from the camera: the inverse of the projection and the camera's view together
uniform mat4 InvViewProj;
// x: seconds; y: how strongly it shows, 0 to 1; z: radians across a pixel, up and down the screen, at its middle;
// w: how many ribbons there are
uniform vec4 Frame;
// x: how distinct the rays are, 0 to 1; y: how strong the violet toward the ribbons' tops is
uniform vec4 Look;
// x: how high the ribbons' lower edge is
uniform vec4 Edge;
// Each ribbon's line: xy the way it runs, a unit vector in x and z; z how far across from the viewer it is now, to its
// left; w how far its ends bow
uniform vec4 RibbonAxis[MAX_RIBBONS];
// Each ribbon's folds: x how far its large folds reach across; y how far along from one to the next; z where along
// their cycle they are now, in radians; w how far its traveling wave reaches across
uniform vec4 RibbonFold[MAX_RIBBONS];
// Each ribbon's wave and sheet: x how far from one crest of the wave to the next; y how far the wave has traveled;
// z how far its rays have; w how thick its sheet is
uniform vec4 RibbonWave[MAX_RIBBONS];
// Each ribbon's light: x how bright it is now; y how far it rises above its lower edge; z how distinct its rays are;
// w where along the noise its patches start
uniform vec4 RibbonLight[MAX_RIBBONS];
// Each ribbon's curl: xy its middle on the ground; z how far out its twist reaches; w how far it turns the layer at
// its middle now, in radians (0: no curl)
uniform vec4 RibbonCurl[MAX_RIBBONS];

// A random value a texel, read smoothly between texels and repeating (NoiseTexture)
uniform sampler2D NoiseSampler;

varying vec2 screenPos;

// How many times farther a curl's twist reaches along its ribbon than across it (CinematicAurora.CURL_STRETCH)
const float CURL_STRETCH = 2.5;
// The noise texture's size, which it repeats at (CinematicAuroraRenderer.NOISE_SIZE)
const float NOISE_SIZE = 512.0;
const float EARTH_RADIUS = 6371.0;
// The layer the ribbons hang in, in kilometers up, and how far along a line of sight it is looked for: beyond that a
// ribbon is a thread on the horizon
const float SHELL_BOTTOM = 70.0;
const float SHELL_TOP = 400.0;
const float FARTHEST = 1800.0;
// How far along a ribbon, either way, its bow has bent it half as far as it goes (CinematicAurora.BOW_LENGTH)
const float BOW_LENGTH = 400.0;
// Kilometers along a ribbon to a cell of the rays' finest noise (CinematicAuroraRenderer.FINE_RAY), and of the coarser
const float FINE_RAY = 1.2;
const float COARSE_RAY = 4.0;
// Steps along the stretch of a line of sight a ribbon can be in. The same number for every pixel: a count that changed
// from one pixel to the next moved every step, and the light jumped along the line where it did
const int STEPS = 20;
// Each step that holds any of a ribbon's light is gone through again in this many, each lit where its own light is:
// lit once, a step looking up a curtain spans kilometers of its height, and its soft lower edge rippled
const int SUBSTEPS = 4;
// Near a curl, every step is gone through again in pieces no longer than this share of the curl's radius, up to
// CURL_SUBSTEPS of them. A step can be longer than a whole curl, and the line can cross the curled ribbon twice within
// it and end on the side it started: judged by its ends alone, it held nothing, and the steps skipped that way lined up
// into rings
const float CURL_PIECE = 0.15;
const int CURL_SUBSTEPS = 24;
// Where a curl's twist is taken as gone, as q (see curl_warp): exp(-16) is a ten-millionth, so a point 500 km out
// moves a few centimeters
const float CURL_FADED = 16.0;
// The most of a sheet a line of sight is counted as going through, in kilometers: seen exactly edge on, a curtain's
// height and length limit it
const float MAX_PATH = 40.0;
// How much wider the halo is than the sheet, and how bright, next to it
const float HALO_WIDTH = 6.0;
const float HALO = 0.008;
// How many of the halo's widths out a step is passed over (see the steps in main)
const float HALO_FAR = 4.0;
// The bright line along a curtain's lower edge: how many kilometers up it fades over, and how much brighter than the
// glow above it it is at its brightest (added to the glow, which stays as it was)
const float EDGE_LINE = 5.0;
const float EDGE_GAIN = 1.5;
// The stretches far brighter than the rest: how long, in kilometers, and how much brighter at their brightest. Rare:
// the brightest few percent of a noise
const float HOT_LENGTH = 300.0;
const float HOT_GAIN = 2.5;
// How bright a kilometer of the light is, before it is brought into range; and how much the brightest parts pale
// toward white
const float EXPOSURE = 0.08;
const float WHITENING = 0.08;
// How much the air dims each color, looking straight up: blue most, so what is low turns yellow-green
const vec3 EXTINCTION = vec3(0.04, 0.07, 0.18);
// The palette: the lower edge's yellow-green (as photographs show the brightest part of a curtain), the curtain's
// emerald, the violet toward its top and in its halo
const vec3 EDGE_COLOR = vec3(0.65, 1.0, 0.3);
const vec3 GREEN = vec3(0.1, 1.0, 0.45);
const vec3 VIOLET = vec3(0.55, 0.2, 1.0);
const float TWO_PI = 6.2831853;
// The sky around a ribbon, lifted to a dim teal as photographs show it (their long exposures catch the aurora's faint
// diffuse light): how strong, how far across it falls away (a bell curve's width, in kilometers), how deep a layer
// it glows in and how high above the lower edge it is looked for, in kilometers, and its color, emerald with some of
// the night sky's blue. Start faint: too much washes out the sky and drowns the stars
const float TINT = 0.004;
const float TINT_WIDTH = 50.0;
const float TINT_DEPTH = 60.0;
const float TINT_HEIGHT = 30.0;
const vec3 TINT_COLOR = vec3(0.16, 0.86, 0.62);

// Smooth value noise, 0 to 1, a cell to a texel of the noise texture, repeating every NOISE_SIZE cells: the S-curve
// within the cell, then one read between texels
float noise(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return texture2D(NoiseSampler, (cell + f + 0.5) / NOISE_SIZE).r;
}

// How far along a line of sight, going up at the sine mu from the ground, it reaches the height h
float shell_distance(float mu, float h) {
    float b = EARTH_RADIUS * mu;
    return -b + sqrt(b * b + h * (2.0 * EARTH_RADIUS + h));
}

// How high a line of sight going up at the sine mu is, t kilometers along it. Worked out without taking the Earth's
// radius from a number close to it, which loses a float's precision
float altitude_at(float t, float mu) {
    float q = t * t + 2.0 * EARTH_RADIUS * t * mu;
    return q / (sqrt(EARTH_RADIUS * EARTH_RADIUS + q) + EARTH_RADIUS);
}

// How far ribbon i's bow bends it, along kilometers along it (CinematicAurora.Ribbon.bowAt)
float ribbon_bow(int i, float along) {
    float a2 = along * along;
    return RibbonAxis[i].w * a2 / (a2 + BOW_LENGTH * BOW_LENGTH);
}

// How far across ribbon i's line is, along kilometers along it (CinematicAurora.Ribbon.centerAt)
float ribbon_center(int i, float along) {
    vec4 fold = RibbonFold[i];
    vec4 wave = RibbonWave[i];
    return RibbonAxis[i].z + ribbon_bow(i, along)
         + fold.x * sin(along * TWO_PI / fold.y + fold.z)
         + fold.w * sin((along - wave.y) * TWO_PI / wave.x);
}

// Where a point on the ground is looked for in ribbon i's untwisted layer: xy the point, z how far it was turned, w q,
// its distance from the curl's middle squared over the radius squared, the part along the ribbon shortened
// CURL_STRETCH times. Turned about the curl's middle, most there and less with distance, farther along the ribbon than
// across it (CinematicAurora.twist), so the ribbon seen bends into a long hook or S
vec4 curl_warp(int i, vec2 ground) {
    vec4 curl = RibbonCurl[i];
    vec2 axis = RibbonAxis[i].xy;
    vec2 r = ground - curl.xy;
    float along = dot(r, axis) / CURL_STRETCH;
    float across = dot(r, vec2(-axis.y, axis.x));
    float q = (along * along + across * across) / (curl.z * curl.z);
    // Far enough out, the turn is under a millionth of the twist: left as it is, without the turning
    if (q > CURL_FADED) {
        return vec4(ground, 0.0, q);
    }
    float angle = curl.w * exp(-q);
    float c = cos(angle);
    float s = sin(angle);
    return vec4(curl.xy + vec2(r.x * c - r.y * s, r.x * s + r.y * c), angle, q);
}

// The farthest ribbon i's curl moves a point, in kilometers (CinematicAurora.Curl.reachOf)
float curl_reach(int i) {
    return RibbonCurl[i].z * CURL_STRETCH * min(0.43 * abs(RibbonCurl[i].w), 2.6);
}

// How fast ribbon i's line goes across for each kilometer along it: the slope of ribbon_center
float ribbon_slope(int i, float along) {
    vec4 fold = RibbonFold[i];
    vec4 wave = RibbonWave[i];
    float spread = along * along + BOW_LENGTH * BOW_LENGTH;
    return fold.x * TWO_PI / fold.y * cos(along * TWO_PI / fold.y + fold.z)
         + fold.w * TWO_PI / wave.x * cos((along - wave.y) * TWO_PI / wave.x)
         + RibbonAxis[i].w * 2.0 * along * BOW_LENGTH * BOW_LENGTH / (spread * spread);
}

// How far a point in ribbon i's untwisted layer is from its line, across it and nearly square to it (the offset
// straight across its frame, shortened by the line's slope there); negative to its right
float layer_offset(int i, vec2 p) {
    vec2 axis = RibbonAxis[i].xy;
    float along = dot(p, axis);
    float across = dot(p, vec2(-axis.y, axis.x));
    float slope = ribbon_slope(i, along);
    return (across - ribbon_center(i, along)) * inversesqrt(1.0 + slope * slope);
}

// How far a point on the ground is from ribbon i's line, as layer_offset, the curl's twist undone first
float ribbon_offset(int i, vec2 ground) {
    return layer_offset(i, curl_warp(i, ground).xy);
}

// The error function, near enough (Winitzki's approximation, within 0.0002)
float erf_approx(float x) {
    float x2 = x * x;
    float e = sqrt(1.0 - exp(-x2 * (1.2732395 + 0.147 * x2) / (1.0 + 0.147 * x2)));
    return x < 0.0 ? -e : e;
}

// How much of a soft sheet, its light falling away across it as a bell curve sigma wide, a stretch of line of sight dt
// long goes through, its offset from the sheet going evenly from g0 to g1 along it: x the integral of
// exp(-g*g / (2 sigma^2)) along the stretch; y where along the stretch, 0 to 1, the middle of that light is (its
// weighted mean), where the light is looked at. That moves smoothly as the stretch does: taking the crossing, or the
// nearer end where there is none, jumped from one to the other, and the sky broke into flat panels with hard edges.
// A stretch that barely moves across the sheet (running along it) takes its middle's value all the way
vec2 sheet_path(float g0, float g1, float dt, float sigma) {
    float dg = g1 - g0;
    float s2 = 2.0 * sigma * sigma;
    if (abs(dg) < sigma * 0.05) {
        float g = 0.5 * (g0 + g1);
        return vec2(dt * exp(-g * g / s2), 0.5);
    }
    float k = 0.70710678 / sigma;
    // The integral across the offsets, which go the same way as dg
    float area = 0.88622693 * (erf_approx(g1 * k) - erf_approx(g0 * k)) / k;
    float path = dt * area / dg;
    // The mean offset under the bell curve: the integral of g exp(-g*g / s2) is -sigma^2 exp(-g*g / s2). Far out in
    // its tail, where the two are lost to rounding, the nearer end does: there is next to no light there
    float middle = abs(g0) < abs(g1) ? 0.0 : 1.0;
    if (abs(area) > sigma * 1e-4) {
        float mean = sigma * sigma * (exp(-g0 * g0 / s2) - exp(-g1 * g1 / s2)) / area;
        middle = clamp((mean - g0) / dg, 0.0, 1.0);
    }
    return vec2(path, middle);
}

// A random value from 0 to 1 for a point
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

// The light seen of ribbon i, where the line of sight passes nearest its sheet on a step, t kilometers along it; path
// and halo are how much of the sheet and its halo the step went through
// The halo alone, where a step goes through it and not the sheet: broad and faint, so worked out plainly, without the
// rays, patches or curl ribbon_light looks up (where its edge is waved, how bright the stretch is). Lit in full there,
// steps through the halo alone took more than half the whole effect's time (10.9 ms a frame at 1920x1080, 4.9 without)
vec3 halo_light(int i, float mu, float t, float halo) {
    vec4 light = RibbonLight[i];
    float above = altitude_at(t, mu) - Edge.x;
    float haloHeight = smoothstep(-30.0, 10.0, above) * (1.0 - smoothstep(light.y * 0.8, light.y * 1.6, above));
    return light.x * 0.6 * halo * HALO * haloHeight * mix(GREEN, VIOLET, 0.85);
}

vec3 ribbon_light(int i, vec3 view, float mu, float t, float path, float halo) {
    vec4 light = RibbonLight[i];
    float time = Frame.x;
    float altitude = altitude_at(t, mu);
    // Where it is in the untwisted layer
    vec2 axis = RibbonAxis[i].xy;
    vec2 warped = curl_warp(i, view.xz * t).xy;
    float along = dot(warped, axis);
    // How far a pixel spans there: the lower edge is no sharper
    float footprint = t * Frame.z;
    // How fast the line of sight goes along the ribbon (in the rays' own measure) and across its sheet there, for each
    // kilometer along the line: measured, by looking the layer up a little further along the line. Worked out from the
    // ribbon's slope and the curl's turn there, they missed how a curl shears the layer (it turns some places more than
    // others), and the rays in curls showed as fine nested arcs, and as fine ripples across them where curls reached
    float LOOK_AHEAD = 0.25;
    vec2 further = curl_warp(i, view.xz * (t + LOOK_AHEAD)).xy;
    float running = abs(dot(further, axis) - along) / LOOK_AHEAD;
    float crossing = abs(layer_offset(i, further) - layer_offset(i, warped)) / LOOK_AHEAD;
    // How far the line of sight runs inside the sheet, from how squarely it crosses: the whole crossing, not the part
    // of it on this step, which halved where a crossing fell across two steps and rippled in bands that followed them
    float inside = min(2.5 * RibbonWave[i].w / max(crossing, 0.001), MAX_PATH);
    // How far along the ribbon (in the rays' own measure) one pixel to the side moves the line of sight: rays hang
    // straight down, so it is a sideways step across the screen that crowds them. Measured by looking the ribbon up a
    // pixel over, which takes in a curtain seen slanting, its folds and its curl's crowding all at once. Worked out
    // from how squarely the curtain faces the viewer, it missed the crowding in curls and capped it on curtains seen
    // edge on, and both showed fine lines
    vec2 side = normalize(vec2(-view.z, view.x) + vec2(1e-6, 0.0));
    float pixelAlong = abs(dot(curl_warp(i, view.xz * t + side * footprint).xy, axis) - along);
    // and how far along it the stretch the line of sight runs inside the sheet reaches, half of it either way: rays
    // finer than that are evened out; they showed as wavy moiré, like hair, on curtains seen slanting
    float blur = pixelAlong + 0.5 * inside * running;

    // Rays, two sizes of them, hanging down the curtain, moving along it and shimmering as they go
    float s = along - RibbonWave[i].z;
    float fine = mix(0.5, noise(vec2(s / FINE_RAY, altitude / 60.0 + time * 0.12)),
                     1.0 - smoothstep(0.4, 1.2, blur / FINE_RAY));
    float coarse = mix(0.5, noise(vec2(s / COARSE_RAY + 17.0, altitude / 200.0 + time * 0.03)),
                       1.0 - smoothstep(0.4, 1.2, blur / COARSE_RAY));
    float streaks = smoothstep(0.15, 0.85, coarse * 0.6 + fine * 0.4);
    float rays = mix(0.6, streaks, light.z * Look.x);

    // Brighter and dimmer stretches along it, slowly changing, some dim enough to read as gaps. They stay where they
    // are along it rather than move with its rays
    float stretch = along + light.w;
    float patches = mix(0.2, 1.0, smoothstep(0.2, 0.75, noise(vec2(stretch / 140.0 + 41.0, time * 0.01))));
    // Now and then a stretch far brighter, where most of the sky's light is: one or two at a time across the sky
    patches *= 1.0 + HOT_GAIN * smoothstep(0.72, 0.92, noise(vec2(stretch / HOT_LENGTH + 83.0, time * 0.005)));

    // The lower edge, waving up and down along it, each ray starting a little lower or higher, the brighter lower
    float edge = Edge.x + 12.0 * (noise(vec2(stretch / 45.0 + 5.0, time * 0.02)) - 0.5) - 6.0 * (coarse - 0.5);
    float above = altitude - edge;
    // How far up this part of the curtain reaches: higher along the brighter rays
    float height = light.y * mix(0.6, 1.2, coarse);
    float k = above / height;
    // Softened by the height the light seen on the step spans as well (the line runs that far up inside the sheet), so
    // the edge isn't looked at in one place where it lies across a stretch: that rippled, or speckled where the steps
    // were moved about to hide the ripples
    float soft = max(max(3.0, footprint * 2.0), inside * mu);
    // Brightest just above the edge, falling away upward, slower along the brighter rays; gone by its top
    float body = above < 0.0 ? exp(above / soft) : exp(-k * mix(5.5, 3.0, rays));
    // The bright line along the lower edge, no narrower than the edge is soft (narrower, it rippled with the steps)
    float line = above < 0.0 ? exp(above / soft) : exp(-above / max(EDGE_LINE, soft));
    body += EDGE_GAIN * line;
    body *= 1.0 - smoothstep(0.8, 1.3, k);
    // Yellow-green only where the curtain is bright: dim, it read olive, where photographs show the faint parts green
    float vivid = smoothstep(0.35, 1.0, light.x * patches);
    vec3 color = mix(GREEN, EDGE_COLOR, vivid * (1.0 - smoothstep(0.0, 0.12, max(k, 0.0))));
    // Fading upward too, more slowly than the green, so the tops are a faint violet the stars show through
    float violet = Look.y * 0.9 * smoothstep(0.15, 0.6, k) * exp(-1.5 * k) * (1.0 - smoothstep(0.9, 1.4, k));

    vec3 sheet = path * (color * body + VIOLET * violet) * (0.25 + 0.75 * rays);
    // The halo: violet with some green, broad and soft, from a little below the edge to above the curtain's top
    float haloHeight = smoothstep(-30.0, 10.0, above) * (1.0 - smoothstep(light.y * 0.8, light.y * 1.6, above));
    vec3 glow = halo * HALO * haloHeight * mix(GREEN, VIOLET, 0.85);
    return light.x * patches * (sheet + glow);
}

void main() {
    // The pixel's direction: from one point along its line of sight (the near plane, z -1 here) to another further
    // along it, not from the camera,
    // which view bobbing moves (see aurora_realistic.fsh)
    vec4 near = InvViewProj * vec4(screenPos, -1.0, 1.0);
    vec4 far = InvViewProj * vec4(screenPos, 0.5, 1.0);
    vec3 view = normalize(far.xyz / far.w - near.xyz / near.w);
    if (view.y < -0.01 || Frame.y <= 0.0) {
        discard;
    }
    float mu = max(view.y, 0.0);

    float tIn = shell_distance(mu, SHELL_BOTTOM);
    float tOut = min(shell_distance(mu, SHELL_TOP), FARTHEST);
    vec3 light = vec3(0.0);
    // Never past the arrays, whatever the count says: reading past a uniform array is undefined on Vulkan
    int ribbons = int(min(floor(Frame.w + 0.5), float(MAX_RIBBONS)));
    for (int i = 0; i < ribbons && tIn < tOut; i++) {
        vec2 axis = RibbonAxis[i].xy;
        float sigma = RibbonWave[i].w;
        // How fast the line of sight goes along the ribbon and across it, for each kilometer along the line
        float alongward = dot(view.xz, axis);
        float acrossward = dot(view.xz, vec2(-axis.y, axis.x));
        // The sky lifted around the ribbon, faintly: looked up once, where the line of sight is TINT_HEIGHT above the
        // lower edge, by how far that is from the ribbon, and thicker the lower it is looked through. Not stepped
        // along: far wider than the band the steps look in, its edges would show where that ends
        float tTint = shell_distance(mu, Edge.x + TINT_HEIGHT);
        if (tTint < FARTHEST) {
            float gTint = ribbon_offset(i, view.xz * tTint);
            light += RibbonLight[i].x * TINT * TINT_DEPTH / (mu + 0.15)
                   * exp(-gTint * gTint / (2.0 * TINT_WIDTH * TINT_WIDTH)) * TINT_COLOR;
        }
        // Only the stretch of the line within the band the ribbon and its halo can be in, across it, is stepped along.
        // The offset worked out square to the line is never more than the one straight across, so this holds. A curl
        // moves points by up to its reach, so the band is that much wider, and the stretch along it looked at too
        float curled = curl_reach(i);
        float alongIn = alongward * tIn;
        float alongOut = alongward * tOut;
        float least = alongIn * alongOut <= 0.0 ? 0.0 : max(min(abs(alongIn), abs(alongOut)) - curled, 0.0);
        float most = max(abs(alongIn), abs(alongOut)) + curled;
        float bowNear = ribbon_bow(i, least);
        float bowFar = ribbon_bow(i, most);
        float reach = RibbonFold[i].x + RibbonFold[i].w + sigma * HALO_WIDTH * 3.0 + curled;
        float lowest = RibbonAxis[i].z - reach + min(bowNear, bowFar);
        float highest = RibbonAxis[i].z + reach + max(bowNear, bowFar);
        float tFrom = tIn;
        float tTo = tOut;
        if (abs(acrossward) > 0.0001) {
            float a = lowest / acrossward;
            float b = highest / acrossward;
            tFrom = max(tIn, min(a, b));
            tTo = min(tOut, max(a, b));
        } else if (lowest > 0.0 || highest < 0.0) {
            continue;
        }
        if (tFrom >= tTo) {
            continue;
        }
        // The same steps for every pixel: moved a little for each one, to break up ripples, they left a sandy grain
        // along the ribbons' edges
        float dt = (tTo - tFrom) / float(STEPS);
        // How far across the ground a step goes, and how near a curl's middle has to be for a step to be split up
        float ground = dt * length(view.xz);
        vec4 curl = RibbonCurl[i];
        float curlNear = 3.0 * curl.z * CURL_STRETCH + 0.5 * ground;
        int curlPieces = int(clamp(ceil(ground / (CURL_PIECE * curl.z)), float(SUBSTEPS), float(CURL_SUBSTEPS)));
        float t0 = tFrom;
        float g0 = ribbon_offset(i, view.xz * t0);
        for (int n = 1; n <= STEPS; n++) {
            float t1 = tFrom + dt * float(n);
            float g1 = ribbon_offset(i, view.xz * t1);
            bool curledStep = curl.w != 0.0 && distance(view.xz * (0.5 * (t0 + t1)), curl.xy) < curlNear;
            // A step that stays on one side of the ribbon, out past four of its halo's widths, holds less of it than
            // a step is ever lit for (exp(-8) of the longest step): passed over without working it out. Where the
            // ribbon curls, the line can cross it and come back within a step, so those are always looked at
            if (!curledStep && g0 * g1 > 0.0 && min(abs(g0), abs(g1)) > HALO_FAR * sigma * HALO_WIDTH) {
                t0 = t1;
                g0 = g1;
                continue;
            }
            vec2 sheet = sheet_path(g0, g1, dt, sigma);
            vec2 aura = sheet_path(g0, g1, dt, sigma * HALO_WIDTH);
            if (!curledStep && sheet.x <= 0.001 && aura.x > 0.05) {
                // Only the halo: looked at where its light is
                light += halo_light(i, mu, mix(t0, t1, aura.y), min(aura.x, MAX_PATH * HALO_WIDTH));
            } else if (curledStep || sheet.x > 0.001) {
                // Away from a curl, the halo is broad enough to be lit once for the whole step, plainly, and the pieces
                // look only at the sheet: working the halo out again in every piece cost 0.35 ms a frame at 1920x1080
                if (!curledStep && aura.x > 0.05) {
                    light += halo_light(i, mu, mix(t0, t1, aura.y), min(aura.x, MAX_PATH * HALO_WIDTH));
                }
                int pieces = curledStep ? curlPieces : SUBSTEPS;
                float sub = dt / float(pieces);
                float s0 = t0;
                float h0 = g0;
                for (int m = 1; m <= pieces; m++) {
                    float s1 = t0 + sub * float(m);
                    float h1 = m == pieces ? g1 : ribbon_offset(i, view.xz * s1);
                    vec2 part = sheet_path(h0, h1, sub, sigma);
                    vec2 partAura = curledStep ? sheet_path(h0, h1, sub, sigma * HALO_WIDTH) : vec2(0.0, 0.5);
                    float path = min(part.x, MAX_PATH);
                    float halo = min(partAura.x, MAX_PATH * HALO_WIDTH);
                    if (path > 0.001) {
                        // Looked at where the light seen on the substep is: the sheet's middle and the halo's, as much
                        // of each as there is of it
                        float sheetWeight = path;
                        float haloWeight = halo * HALO;
                        float middle = (part.y * sheetWeight + partAura.y * haloWeight) / (sheetWeight + haloWeight);
                        light += ribbon_light(i, view, mu, mix(s0, s1, middle), path, halo);
                    } else if (halo > 0.05) {
                        light += halo_light(i, mu, mix(s0, s1, partAura.y), halo);
                    }
                    s0 = s1;
                    h0 = h1;
                }
            }
            t0 = t1;
            g0 = g1;
        }
    }

    // The air between: the lower the line of sight, the more of it, dimming blue most
    float airMass = 1.0 / (mu + 0.05);
    light *= exp(-EXTINCTION * airMass);

    // Into range by its brightest color, the others scaled with it, so it keeps its color however bright; then a
    // little of each color on its own, which pales the very brightest parts toward white
    vec3 exposed = light * EXPOSURE;
    float peak = max(exposed.r, max(exposed.g, exposed.b));
    vec3 kept = peak > 0.0 ? exposed * ((1.0 - exp(-peak)) / peak) : vec3(0.0);
    vec3 color = mix(kept, 1.0 - exp(-exposed), WHITENING);
    color *= Frame.y;
    color *= smoothstep(-0.01, 0.02, view.y);
    // A little noise against banding in the smooth glow
    if (max(color.r, max(color.g, color.b)) > 0.5 / 255.0) {
        color += hash(gl_FragCoord.xy) / 255.0;
    }
    gl_FragColor = vec4(color, 0.0);
}
