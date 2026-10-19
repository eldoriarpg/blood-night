/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules.explosion;

import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import de.eldoria.bloodnight.rules.RulesTest;
import org.bukkit.ExplosionResult;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarmlessExplosionsTest extends RulesTest {
    private List<Block> explode(boolean harmless) {
        Location at = new Location(world, 0, 64, 0);
        TNTPrimed tnt = world.spawn(at, TNTPrimed.class);
        if (harmless) tnt.getPersistentDataContainer().set(BukkitRuntime.HARMLESS, PersistentDataType.BOOLEAN, true);
        EntityExplodeEvent event =
                new EntityExplodeEvent(tnt, at, new ArrayList<>(List.of(at.getBlock())), 1, ExplosionResult.DESTROY);
        new HarmlessExplosions().onExplode(event);
        return event.blockList();
    }

    @Test
    void markedExplosionsBreakNoBlocks() {
        assertTrue(explode(true).isEmpty());
        assertEquals(1, explode(false).size());
    }
}
