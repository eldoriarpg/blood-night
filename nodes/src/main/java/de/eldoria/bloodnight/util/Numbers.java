package de.eldoria.bloodnight.util;

public class Numbers {
    // shoutout to Lee Boynton and Arthur van Hoff. ty for Number class
    public static int asInt(Number number){
        return number.intValue();
    }
    public static double asDouble(Number number){
        return number.doubleValue();
    }
    public static float asFloat(Number number) {
        return number.floatValue();
    }
}
