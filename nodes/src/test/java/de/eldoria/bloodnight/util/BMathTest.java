package de.eldoria.bloodnight.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BMathTest {

    @Test
    void clampIntTest() {
        Assertions.assertEquals(1, BMath.clamp(10, -10, 1));
        Assertions.assertEquals(1, BMath.clamp(0, 1, 10));
    }

    @Test
    void clampDoubleTest() {
        Assertions.assertEquals(1.0, BMath.clamp(10.0, -10.0, 1.0));
        Assertions.assertEquals(1.0, BMath.clamp(0, 1, 10));
    }
}
