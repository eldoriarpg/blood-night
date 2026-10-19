/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules.explosion;

import de.eldoria.bloodnight.rules.RulesTest;
import de.eldoria.bloodnight.rules.Settings;
import org.bukkit.ExplosionResult;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreeperGuardTest extends RulesTest {
    @BeforeEach
    void setUp() {
        server.getPluginManager().registerEvents(new CreeperGuard(nights, ignored -> settings), plugin);
    }

    private int blocksBroken() {
        Creeper creeper = (Creeper) world.spawnEntity(world.getSpawnLocation(), EntityType.CREEPER);
        List<Block> blocks = new ArrayList<>(List.of(world.getBlockAt(0, 60, 0), world.getBlockAt(1, 60, 0)));
        var event = new EntityExplodeEvent(creeper, creeper.getLocation(), blocks, 1, ExplosionResult.DESTROY);
        server.getPluginManager().callEvent(event);
        return event.blockList().size();
    }

    @Test
    void creepersKeepBlocksDuringABloodNight() {
        assertEquals(2, blocksBroken());
        bloodNight();
        assertEquals(0, blocksBroken());
    }

    @Test
    void protectionCanApplyAlways() {
        settings = Settings.world("{\"mobSettings\":{\"creepers\":{\"always\":true}}}");
        assertEquals(0, blocksBroken());
    }

    @Test
    void blockDamageCanStayOn() {
        settings = Settings.world("{\"mobSettings\":{\"creepers\":{\"blockDamage\":true}}}");
        bloodNight();
        assertTrue(blocksBroken() > 0);
    }
}
