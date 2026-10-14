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
const vec3 Blood = vec3(0.42, 0.02, 0.02);

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    float light = dot(color, Luminance);
    vec3 tinted = mix(color, vec3(light * 1.35, light * 0.45, light * 0.4), Tint);
    vec2 aspect = vec2(OutSize.x / OutSize.y, 1.0);
    float edge = smoothstep(0.35, 0.95, length((texCoord - 0.5) * aspect));
    fragColor = vec4(mix(tinted, Blood * light + Blood * 0.15, edge * Vignette), 1.0);
}
