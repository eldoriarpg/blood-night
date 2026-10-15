#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BloodConfig {
    float Tint;
    float Vignette;
};

layout(location = 0) out vec4 fragColor;

const vec3 Luminance = vec3(0.3, 0.59, 0.11);
const vec3 Red = vec3(1.6, 0.75, 0.7);
const vec3 Edge = vec3(0.55, 0.12, 0.12);

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    float light = dot(color, Luminance);
    vec3 tinted = mix(color, light * Red, Tint);
    vec2 aspect = vec2(OutSize.x / OutSize.y, 1.0);
    float edge = smoothstep(0.45, 1.0, length((texCoord - 0.5) * aspect));
    fragColor = vec4(mix(tinted, tinted * Edge, edge * Vignette), 1.0);
}
