/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living.spawn;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.world.MobSettings;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.attributes.Attributes;
import de.eldoria.bloodnight.mob.attributes.MobName;
import de.eldoria.bloodnight.mob.attributes.ValueModifier;
import de.eldoria.bloodnight.mob.equipment.Extension;
import de.eldoria.bloodnight.mob.equipment.ExtensionType;
import de.eldoria.graph.model.Graph;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MobOutfitterTest {
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void valuesFollowTheModifier() {
        assertEquals(40, MobOutfitter.value(20, ValueModifier.DEFAULT, 5, 2));
        assertEquals(100, MobOutfitter.value(20, ValueModifier.MULTIPLY, 5, 2));
        assertEquals(5, MobOutfitter.value(20, ValueModifier.VALUE, 5, 2));
        assertEquals(0, MobOutfitter.value(20, ValueModifier.VALUE, -5, 2));
    }

    @Test
    void extensionTakesTheMobsMaximumHealth() {
        ServerMock server = MockBukkit.mock();
        World world = server.addSimpleWorld("world");
        CustomMob plain = Mobs.zombie("rider", Graph.builder().build());
        CustomMob rider = new CustomMob(
                plain.id(),
                plain.equipment(),
                new Attributes(
                        MobName.english("rider"),
                        EntityType.ZOMBIE,
                        false,
                        ValueModifier.VALUE,
                        50,
                        ValueModifier.DEFAULT,
                        1),
                plain.behaviour(),
                plain.loot(),
                new Extension(ExtensionType.RIDER, EntityType.SPIDER, null),
                plain.spawn());
        LivingEntity zombie = (LivingEntity) world.spawnEntity(world.getSpawnLocation(), EntityType.ZOMBIE);

        Entity extension = new MobOutfitter(ItemRegistry::new, ignored -> new MobSettings()).outfit(zombie, rider);

        LivingEntity spider = assertInstanceOf(LivingEntity.class, extension);
        assertEquals(50, spider.getAttribute(Attribute.MAX_HEALTH).getValue());
        assertEquals(50, spider.getHealth());
    }
}
