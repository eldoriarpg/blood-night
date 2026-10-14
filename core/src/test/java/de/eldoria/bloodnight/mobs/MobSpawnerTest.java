/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.AttributeChange;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightSelector;
import de.eldoria.bloodnight.night.NightTimes;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.List;
import java.util.SplittableRandom;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobSpawnerTest {
    private static final NightTimes TIMES = new NightTimes(13_000, 23_000);
    private ServerMock server;
    private World world;
    private final MobRegistry mobs = new MobRegistry();
    private final WorldSettings settings = new WorldSettings();
    private MobCoordinator coordinator;
    private BloodNights nights;
    private MobSpawner spawner;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        CustomMob strong = Mobs.zombie("strong", Graph.builder().build());
        strong = new CustomMob(
                strong.id(),
                strong.equipment(),
                new Attributes(
                        "<red>Strong",
                        EntityType.ZOMBIE,
                        false,
                        ValueModifier.VALUE,
                        50,
                        ValueModifier.MULTIPLY,
                        3,
                        List.of(new AttributeChange("minecraft:movement_speed", Operation.ADD_SCALAR, 1))),
                strong.behaviour(),
                strong.mobDrops(),
                strong.extension(),
                strong.spawn());
        mobs.add(strong);
        ItemRegistry items = new ItemRegistry();
        Tasks tasks = new Tasks(MockBukkit.createMockPlugin());
        coordinator = new MobCoordinator(
                new MobOutfitter(() -> items, ignored -> settings.mobSettings()),
                () -> items,
                mobs::get,
                world -> nights.running(world),
                tasks,
                Logger.getLogger("test"));
        NightSelection every = JsonMapper.builder()
                .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build()
                .readValue(
                        "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}",
                        NightSelection.class);
        nights = new BloodNights(
                new NightSelector(ignored -> every, ignored -> TIMES, Clock.systemUTC(), () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));
        spawner = new MobSpawner(mobs::all, ignored -> settings, coordinator, nights, tasks, new SplittableRandom(1));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void at(long fullTime) {
        world.setFullTime(fullTime);
        nights.check(List.of(world));
    }

    private LivingEntity zombie() {
        return (LivingEntity) world.spawnEntity(world.getSpawnLocation(), EntityType.ZOMBIE);
    }

    private LivingEntity spawned() {
        LivingEntity zombie = zombie();
        for (int i = 0; i < 100 && !MobTags.isCustomMob(zombie); i++) spawner.handleSpawnedMob(zombie);
        return zombie;
    }

    @Test
    void mobsSpawnDuringBloodNightsOnly() {
        assertFalse(MobTags.isCustomMob(spawned()));
        at(13_000);
        LivingEntity mob = spawned();
        assertTrue(MobTags.isCustomMob(mob));
        assertEquals(0, MobTags.getNight(mob).orElseThrow());
    }

    @Test
    void wrappingAppliesNameAndAttributes() {
        at(13_000);
        LivingEntity mob = spawned();
        assertTrue(mob.isCustomNameVisible());
        assertEquals(50, mob.getAttribute(Attribute.MAX_HEALTH).getValue());
        assertEquals(50, mob.getHealth());
        assertEquals(
                2 * mob.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue(),
                mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue(),
                1e-9);
    }

    @Test
    void customMobsAreNotWrappedAgain() {
        at(13_000);
        LivingEntity mob = spawned();
        double health = mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        spawner.handleSpawnedMob(mob);
        assertEquals(health, mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
    }

    @Test
    void mobsGoWhenTheirNightEnds() {
        at(13_000);
        LivingEntity mob = spawned();
        server.getScheduler().performTicks(40);
        assertTrue(mob.isValid());
        at(24_000);
        server.getScheduler().performTicks(20);
        assertFalse(mob.isValid());
    }
}
