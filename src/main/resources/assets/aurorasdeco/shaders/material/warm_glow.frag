#include frex:shaders/api/fragment.glsl
#include frex:shaders/lib/math.glsl

/******************************************************
  aurorasdeco:shaders/material/warm_glow.frag
******************************************************/

bool aurorasdeco_is_near(float value, float epsilon, float target) {
    return target - epsilon < value && target + epsilon > value;
}

// All components are in the range [0…1], including hue.
vec3 aurorasdeco_rgb_to_hsv(vec3 c) {
    vec4 k = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, k.wz), vec4(c.gb, k.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

void frx_materialFragment() {
#ifndef DEPTH_PASS
    float e = frx_luminance(frx_sampleColor.rgb);
    vec3 hsv = aurorasdeco_rgb_to_hsv(frx_sampleColor.rgb); // HSV is a little bit more reliable for that tbh.
    bool lit = hsv.z > 0.5 && !aurorasdeco_is_near(hsv.x, 0.01, 35.7f / 360f);
    frx_fragEmissive = lit ? e : 0.0;
    frx_fragEnableDiffuse = frx_fragEnableDiffuse && !lit;
    frx_fragEnableAo = frx_fragEnableAo && !lit;
#endif
}
