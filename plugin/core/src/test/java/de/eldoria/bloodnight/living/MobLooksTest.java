/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.world.MobSettings;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.living.movement.Steering;
import de.eldoria.bloodnight.living.spawn.MobOutfitter;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobParticles;
import de.eldoria.bloodnight.particles.play.Effects;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.graph.model.Graph;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MobLooksTest {
    private final List<String> played = new ArrayList<>();
    private ServerMock server;
    private World world;
    private MobCoordinator coordinator;

    private final Effects recording = new Effects() {
        @Override
        public long play(
                String id, Object source, @Nullable Object target, @Nullable Player viewer, @Nullable UUID owner) {
            played.add("play " + id);
            return 0;
        }

        @Override
        public void spawned(LivingEntity mob, @Nullable String id) {
            played.add("spawned " + id);
        }

        @Override
        public void ticked(LivingEntity mob, @Nullable String ambient) {
            played.add("ticked " + ambient);
        }

        @Override
        public void died(LivingEntity mob, @Nullable String id) {
            played.add("died " + id);
        }

        @Override
        public void forget(UUID mob) {
            played.add("forget");
        }
    };

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        ItemRegistry items = new ItemRegistry();
        coordinator = new MobCoordinator(
                new MobOutfitter(() -> items, ignored -> new MobSettings()),
                () -> items,
                id -> null,
                ignored -> OptionalLong.empty(),
                new Tasks(plugin),
                Steering.NONE,
                Logger.getLogger("test"),
                event -> {},
                recording);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void aMobWearsItsEffectsFromSpawnToDeath() {
        CustomMob mob = Mobs.zombie("glowing", Graph.builder().build())
                .withParticles(new MobParticles("aura", "rise", "burst"));
        Zombie zombie = world.spawn(new Location(world, 0, 64, 0), Zombie.class);

        coordinator.register(zombie, mob);
        server.getScheduler().performOneTick();
        coordinator.died(zombie);
        coordinator.remove(zombie);

        assertEquals(List.of("spawned rise", "ticked aura", "died burst", "forget"), played);
    }
}
