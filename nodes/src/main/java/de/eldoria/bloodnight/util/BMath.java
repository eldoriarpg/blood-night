package de.eldoria.bloodnight.util;

public class BMath {
    public static int clamp(int value, int lower, int upper) {
        return Math.max(lower, Math.min(upper, value));
    }

    public static double clamp(double value, double lower, double upper) {
        return Math.max(lower, Math.min(upper, value));
    }
}
