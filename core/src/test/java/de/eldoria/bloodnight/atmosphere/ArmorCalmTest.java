/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArmorCalmTest {
    private static final Map<EquipmentSlot, Double> INTACT =
            Map.of(EquipmentSlot.HEAD, 1.0, EquipmentSlot.CHEST, 1.0, EquipmentSlot.LEGS, 1.0, EquipmentSlot.FEET, 1.0);
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void fullStrongIntactArmorIsFullyCalm() {
        assertEquals(1, ArmorCalm.of(20, INTACT, Set.of()), 1e-9);
        assertEquals(0.35, ArmorCalm.of(7, INTACT, Set.of()), 1e-9);
        assertEquals(0, ArmorCalm.of(0, Map.of(), Set.of()), 1e-9);
    }

    @Test
    void damagedPiecesLoseTheirShareDownToNothing() {
        assertEquals(
                0.8,
                ArmorCalm.of(
                        20,
                        Map.of(
                                EquipmentSlot.HEAD,
                                1.0,
                                EquipmentSlot.CHEST,
                                0.5,
                                EquipmentSlot.LEGS,
                                1.0,
                                EquipmentSlot.FEET,
                                1.0),
                        Set.of()),
                1e-9);
        assertEquals(
                0.6,
                ArmorCalm.of(
                        20,
                        Map.of(
                                EquipmentSlot.HEAD,
                                1.0,
                                EquipmentSlot.CHEST,
                                0.0,
                                EquipmentSlot.LEGS,
                                1.0,
                                EquipmentSlot.FEET,
                                1.0),
                        Set.of()),
                1e-9);
    }

    @Test
    void durabilityIsReadFromThePiece() {
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chest.editMeta(Damageable.class, meta -> meta.setDamage(Material.DIAMOND_CHESTPLATE.getMaxDurability() / 2));

        assertEquals(0.5, ArmorCalm.durability(chest), 0.01);
        assertEquals(1, ArmorCalm.durability(new ItemStack(Material.CARVED_PUMPKIN)), 1e-9);
    }

    @Test
    void playersWithoutArmorAreNotCalm() {
        assertEquals(0, ArmorCalm.of(server.addPlayer()), 1e-9);
    }

    @Test
    void nightWardPiecesCountDouble() {
        Map<EquipmentSlot, Double> chestOnly = Map.of(EquipmentSlot.CHEST, 1.0);

        assertEquals(0.4, ArmorCalm.of(20, chestOnly, Set.of()), 1e-9);
        assertEquals(0.8, ArmorCalm.of(20, chestOnly, Set.of(EquipmentSlot.CHEST)), 1e-9);
        assertEquals(1, ArmorCalm.of(20, INTACT, Set.of(EquipmentSlot.CHEST)), 1e-9);
    }
}
