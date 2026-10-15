#ifndef BLOODNIGHT_MIST_GLSL
#define BLOODNIGHT_MIST_GLSL

#include <minecraft:fog.glsl>
#include <minecraft:globals.glsl>

const float MistPeriod = 64.0;
const vec3 MistScaleA = vec3(1.0 / 9.0, 1.0 / 5.0, 1.0 / 9.0);
const vec3 MistScaleB = vec3(1.0 / 6.0, 1.0 / 4.0, 1.0 / 6.0);
const vec3 MistScaleBlend = vec3(1.0 / 40.0, 1.0 / 20.0, 1.0 / 40.0);
const vec3 MistDriftA = vec3(MistPeriod, 0.0, MistPeriod);
const vec3 MistDriftB = vec3(-MistPeriod, 0.0, MistPeriod);
const vec3 MistDriftBlend = vec3(0.0, 0.0, MistPeriod);
const float MistThick = 0.45;
const float MistThin = 1.4;
const float MistGlowStrength = 0.55;
const vec3 MistGlow = vec3(0.8, 0.16, 0.13);
const vec3 BloodWater = vec3(1.8, 0.12, 0.1);
const vec3 Luminance = vec3(0.3, 0.59, 0.11);

float blood_fog() {
    return clamp((FogColor.r - 3.0 * max(FogColor.g, FogColor.b)) * 10.0, 0.0, 1.0);
}

float mist_hash(vec3 cell) {
    vec3 wrapped = mod(cell, MistPeriod);
    return fract(sin(dot(wrapped, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

float mist_noise(vec3 point) {
    vec3 cell = floor(point);
    vec3 within = fract(point);
    vec3 smoothed = within * within * (3.0 - 2.0 * within);
    float bottom = mix(
        mix(mist_hash(cell), mist_hash(cell + vec3(1.0, 0.0, 0.0)), smoothed.x),
        mix(mist_hash(cell + vec3(0.0, 1.0, 0.0)), mist_hash(cell + vec3(1.0, 1.0, 0.0)), smoothed.x),
        smoothed.y);
    float top = mix(
        mix(mist_hash(cell + vec3(0.0, 0.0, 1.0)), mist_hash(cell + vec3(1.0, 0.0, 1.0)), smoothed.x),
        mix(mist_hash(cell + vec3(0.0, 1.0, 1.0)), mist_hash(cell + vec3(1.0, 1.0, 1.0)), smoothed.x),
        smoothed.y);
    return mix(bottom, top, smoothed.z);
}

float mist_layer(vec3 point) {
    return 0.65 * mist_noise(point) + 0.35 * mist_noise(point * 2.0 + 17.0);
}

float mist_patches(vec3 world) {
    float first = mist_layer(world * MistScaleA + MistDriftA * GameTime);
    float second = mist_layer(world * MistScaleB + MistDriftB * GameTime + 31.0);
    float blend = mist_noise(world * MistScaleBlend + MistDriftBlend * GameTime + 53.0);
    return mix(first, second, smoothstep(0.3, 0.7, blend));
}

float blood_mist_density(vec3 relativePosition) {
    float blood = blood_fog();
    if (blood <= 0.0) {
        return -1.0;
    }
    vec3 world = relativePosition + vec3(CameraBlockPos) - CameraOffset;
    return blood * smoothstep(0.4, 0.62, mist_patches(world));
}

float mist_fog_distance(float distance, float density) {
    if (density < 0.0) {
        return distance;
    }
    return distance / mix(MistThin, MistThick, density);
}

vec4 apply_blood_mist(vec4 color, vec3 relativePosition, float density) {
    if (density <= 0.0) {
        return color;
    }
    float near = smoothstep(2.0, 12.0, length(relativePosition));
    return vec4(mix(color.rgb, MistGlow, density * near * MistGlowStrength), color.a);
}

vec3 blood_water(vec3 color) {
    float light = dot(color, Luminance);
    return mix(color, light * BloodWater, blood_fog());
}

bool is_water_blue(vec4 texel) {
    return texel.b > texel.r + 0.2 && texel.b > texel.g + 0.15;
}

#endif
