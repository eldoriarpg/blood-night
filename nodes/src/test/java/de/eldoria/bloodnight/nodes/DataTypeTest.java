package de.eldoria.bloodnight.nodes;

import de.eldoria.bloodnight.nodes.meta.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataTypeTest {
    @Test
    public void test() {
        Assertions.assertArrayEquals(DataType.ANY.subtypes(), DataType.values());
    }

}
