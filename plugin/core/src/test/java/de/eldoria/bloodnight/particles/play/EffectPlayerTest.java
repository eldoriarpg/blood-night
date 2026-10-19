/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import de.eldoria.bloodnight.configuration.server.ParticleSettings;
import de.eldoria.bloodnight.particles.EffectLibrary;
import de.eldoria.bloodnight.particles.ParticleEffects;
import de.eldoria.bloodnight.tasks.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.entity.ZombieMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectPlayerTest {
    private ServerMock server;
    private WorldMock world;
    private Viewers viewers;
    private EffectPlayer effects;
    private PlayerMock player;
    private final List<Runnable> sent = new ArrayList<>();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        viewers = new Viewers();
        Stage stage = new Stage(
                new Tasks(plugin), viewers, new Budget(() -> 2000), ParticleSettings::new, id -> null, work -> {
                    sent.add(work);
                    work.run();
                });
        effects = new EffectPlayer(new EffectLibrary(ParticleEffects::new), stage, plugin);
        player = server.addPlayer();
        player.teleport(new Location(world, 5, 64, 0));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * A zombie whose body faces where it looks; MockBukkit has no body yaw.
     */
    private Zombie zombie() {
        ZombieMock zombie = new ZombieMock(server, UUID.randomUUID()) {
            @Override
            public float getBodyYaw() {
                return getLocation().getYaw();
            }
        };
        zombie.setLocation(spot());
        server.registerEntity(zombie);
        return zombie;
    }

    private Location spot() {
        return new Location(world, 0, 64, 0);
    }

    @Test
    void nearPlayersGetTheParticles() {
        assertEquals(0, effects.play("bloodnight:shock_wave", spot(), null, null, null));
        server.getScheduler().performTicks(3);
        assertFalse(world.getSpawnedParticles().isEmpty());
        assertTrue(world.getSpawnedParticles().getFirst().receivers().contains(player));
    }

    @Test
    void particlesLeaveThroughTheSender() {
        effects.play("bloodnight:shock_wave", spot(), null, null, null);
        server.getScheduler().performTicks(3);
        assertFalse(sent.isEmpty());
    }

    @Test
    void playersWhoSwitchedThemOffAndFarPlayersGetNone() {
        viewers.set(player, false);
        PlayerMock far = server.addPlayer();
        far.teleport(new Location(world, 100, 64, 0));
        effects.play("bloodnight:shock_wave", spot(), null, null, null);
        server.getScheduler().performTicks(3);
        assertTrue(world.getSpawnedParticles().isEmpty());
    }

    @Test
    void unknownEffectsDoNotPlay() {
        assertEquals(-1, effects.play("nothing", spot(), null, null, null));
    }

    @Test
    void aMobPlaysOnlyAsManyEffectsAsAllowed() {
        UUID owner = UUID.randomUUID();
        for (int index = 0; index < 4; index++) {
            assertEquals(0, effects.play("bloodnight:storm_charge", spot(), null, null, owner));
        }
        assertEquals(-1, effects.play("bloodnight:storm_charge", spot(), null, null, owner));
    }

    @Test
    void aSpearFliesToItsTargetAndGoes() {
        Zombie zombie = zombie();
        Zombie target = zombie();
        target.teleport(new Location(world, 4, 64, 0));
        assertEquals(8, effects.play("bloodnight:blood_spear", zombie, target, null, zombie.getUniqueId()));
        server.getScheduler().performTicks(2);
        assertEquals(1, world.getEntitiesByClass(BlockDisplay.class).size());
        server.getScheduler().performTicks(60);
        assertTrue(world.getEntitiesByClass(Display.class).isEmpty());
    }

    @Test
    void theWornEffectPlaysWhileAPlayerIsNear() {
        Zombie zombie = zombie();
        for (int tick = 0; tick < 5; tick++) effects.ticked(zombie, "bloodnight:blood_aura");
        assertFalse(world.getSpawnedParticles().isEmpty());
        world.clearSpawnedParticles();
        player.teleport(new Location(world, 200, 64, 0));
        for (int tick = 0; tick < 5; tick++) effects.ticked(zombie, "bloodnight:blood_aura");
        assertTrue(world.getSpawnedParticles().isEmpty());
    }
}
