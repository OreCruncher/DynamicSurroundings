"""Makes 1.12.2's GLSL 1.20 aurora_cinematic.frag from the 26.x line's aurora_cinematic.fsh (and its include).

usage: python scripts/port-cinematic-shader.py <26.x shaders folder> <out file>
  e.g. ../DynamicSurroundingsFabric/common/src/main/resources/assets/dsurround/shaders
       src/main/resources/assets/dsurround/shaders/aurora_cinematic.frag
Each replacement is checked to have happened once, so a change upstream fails loudly instead of slipping through.
"""
import sys

src_dir, out = sys.argv[1], sys.argv[2]
with open(src_dir + "/core/aurora_cinematic.fsh", encoding="utf-8") as f:
    s = f.read()


def swap(old, new, count=1):
    global s
    n = s.count(old)
    assert n == count, (old, n)
    s = s.replace(old, new)


header_old = s[:s.index("// Dynamic Surroundings cinematic aurora (CinematicAuroraRenderer)")]
swap(header_old, """#version 120
// This file is part of Dynamic Surroundings, licensed under the MIT License (MIT). Copyright (c) OreCruncher
// Made from the 26.x line's shaders/core/aurora_cinematic.fsh by scripts in its port: GLSL 1.20, uniforms in place of
// its uniform block, the noise function inlined, ints clamped as floats.

""")
swap("#include <dsurround:aurora_cinematic.glsl>\n", """// What the shader is given (CinematicAuroraRenderer, CinematicAuroraFrame). Distances are in kilometers; the ground's
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
""")
swap("layout(location = 0) in vec2 screenPos;\n", "varying vec2 screenPos;\n")
swap("layout(location = 0) out vec4 fragColor;\n\n", "")
swap("#include <dsurround:noise.glsl>\n", """// Smooth value noise, 0 to 1, a cell to a texel of the noise texture, repeating every NOISE_SIZE cells: the S-curve
// within the cell, then one read between texels
float noise(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return texture2D(NoiseSampler, (cell + f + 0.5) / NOISE_SIZE).r;
}
""")
# GLSL 1.20 has no integer min or clamp
swap("int ribbons = min(int(Frame.w + 0.5), MAX_RIBBONS);",
     "int ribbons = int(min(floor(Frame.w + 0.5), float(MAX_RIBBONS)));")
swap("int curlPieces = clamp(int(ceil(ground / (CURL_PIECE * curl.z))), SUBSTEPS, CURL_SUBSTEPS);",
     "int curlPieces = int(clamp(ceil(ground / (CURL_PIECE * curl.z)), float(SUBSTEPS), float(CURL_SUBSTEPS)));")
# A bool shadowing the float of the same name in the loop: allowed, but renamed for older compilers
swap("bool curled = curl.w != 0.0", "bool curledStep = curl.w != 0.0")
for old in ["if (!curled && g0 * g1", "if (!curled && sheet.x", "} else if (curled || sheet.x",
            "if (!curled && aura.x", "int pieces = curled ? curlPieces", "vec2 partAura = curled ? sheet_path"]:
    swap(old, old.replace("curled", "curledStep"))
swap("fragColor = vec4(color, 0.0);", "gl_FragColor = vec4(color, 0.0);")
# The 26.x line draws with reverse-Z (depth cleared to 0), so its near point is at z 1.0; here depth is OpenGL's own,
# z -1.0 the near plane and 1.0 the far one: taken at 1.0, the direction pointed back at the camera, and every pixel
# looking up was discarded
swap("vec4 near = InvViewProj * vec4(screenPos, 1.0, 1.0);", "vec4 near = InvViewProj * vec4(screenPos, -1.0, 1.0);")
swap("    // The pixel's direction: from one point along its line of sight to another further along it, not from the camera,",
     "    // The pixel's direction: from one point along its line of sight (the near plane, z -1 here) to another further\n"
     "    // along it, not from the camera,")
assert "texture(" not in s.replace("texture2D(", ""), "a texture() call left"
assert "layout(" not in s and "#include" not in s and "fragColor =" not in s.replace("gl_FragColor =", "")
with open(out, "w", encoding="utf-8", newline="\r\n") as f:
    f.write(s)
print("wrote", out, s.count("\n"), "lines")
