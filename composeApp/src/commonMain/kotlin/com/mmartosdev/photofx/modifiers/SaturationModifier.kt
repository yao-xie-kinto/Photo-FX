package com.mmartosdev.photofx.modifiers

import androidx.compose.ui.Modifier

private val shader = """
uniform float2 resolution;
uniform shader content;
uniform float saturation;

float max3(float a, float b, float c) {
    return max(max(a, b), c);
}

float min3(float a, float b, float c) {
    return min(min(a, b), c);
}

vec4 adjustSaturation(vec4 color, float sat) {
    vec3 rgb = color.rgb;
    float a = color.a;
    
    float max_val = max3(rgb.r, rgb.g, rgb.b);
    float min_val = min3(rgb.r, rgb.g, rgb.b);
    float l = (max_val + min_val) / 2.0;
    
    if (max_val != min_val) {
        float d = max_val - min_val;
        float s = l > 0.5 ? d / (2.0 - max_val - min_val) : d / (max_val + min_val);
        float newS;
        
        if (sat <= 100.0) {
            newS = s * (sat / 100.0);
        } else {
            float t = (sat - 100.0) / 100.0;
            newS = s + (1.0 - s) * t;
        }
        
        float factor = newS / s;
        
        rgb = clamp(vec3(l) + (rgb - vec3(l)) * factor, 0.0, 1.0);
    }
    
    return vec4(rgb, a);
}

vec4 simulateJSRounding(vec4 color) {
    return floor(color * 255.0 + 0.5) / 255.0;
}

vec4 main(vec2 fragCoord) {
    vec4 color = content.eval(fragCoord);
    vec4 adjustedColor = adjustSaturation(color, saturation);
    vec4 finalColor = simulateJSRounding(adjustedColor);
    return finalColor;
}
""".trimIndent()




fun Modifier.saturationShader(saturation: Float): Modifier {
    return this then runtimeShader(shader) {
        try {
            uniform("saturation", saturation)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}