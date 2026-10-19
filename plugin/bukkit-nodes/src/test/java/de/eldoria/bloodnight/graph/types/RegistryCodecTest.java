/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.types;

import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.types.LiteralCodec;
import de.eldoria.graph.types.TypeMapping;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.StringNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryCodecTest {
    private static final TypeMapping TYPES = BukkitTypes.mapping();

    private static LiteralCodec<?> codec(Class<?> type) {
        return TYPES.codec(PortType.of(type)).orElseThrow();
    }

    @BeforeEach
    void noServer() {
        assertNull(Bukkit.getServer(), "These checks are about building the catalog without a server");
    }

    @Test
    void generatedKeyClassesListPureRegistries() {
        assertTrue(codec(PotionEffectType.class).values().contains("minecraft:speed"));
        assertTrue(codec(ItemType.class).values().contains("minecraft:diamond_sword"));
    }

    @Test
    void enumBackedRegistriesUseTheirConstants() {
        assertTrue(codec(EntityType.class).values().contains("minecraft:zombie"));
    }

    @Test
    void valuesAreCheckedWithoutAServer() {
        assertEquals(
                "minecraft:speed",
                codec(PotionEffectType.class).read(StringNode.valueOf("speed")).toString());
        assertThrows(IllegalArgumentException.class, () -> codec(PotionEffectType.class)
                .read(StringNode.valueOf("minecraft:flying_pigs")));
    }
}
