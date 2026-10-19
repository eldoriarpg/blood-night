/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.DamageEvents;
import de.eldoria.bloodnight.configuration.server.plugins.ForeignMobSettings;
import de.eldoria.bloodnight.living.spawn.ForeignMobs;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MobDamageTest extends RulesTest {
    private boolean denied;
    private PlayerMock player;
    private Entity zombie;
    private Entity cow;

    @BeforeEach
    void setUp() {
        settings = Settings.world("{\"mobSettings\":{\"damageModifier\":3,\"healthMultiplier\":4}}");
        server.getPluginManager()
                .registerEvents(
                        new MobDamage(
                                nights,
                                ignored -> settings.mobSettings(),
                                new ForeignMobs(ForeignMobSettings::new, () -> null),
                                (location, flag) -> !denied),
                        plugin);
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        zombie = world.spawnEntity(world.getSpawnLocation(), EntityType.ZOMBIE);
        cow = world.spawnEntity(world.getSpawnLocation(), EntityType.COW);
    }

    private double hit(Entity attacker, Entity victim) {
        var source = DamageSource.builder(DamageType.MOB_ATTACK)
                .withCausingEntity(attacker)
                .withDirectEntity(attacker)
                .build();
        var event = DamageEvents.byEntity(attacker, victim, DamageCause.ENTITY_ATTACK, source, 4);
        server.getPluginManager().callEvent(event);
        return event.getDamage();
    }

    @Test
    void enemiesHitPlayersHarder() {
        assertEquals(4, hit(zombie, player));
        bloodNight();
        assertEquals(12, hit(zombie, player));
        assertEquals(4, hit(zombie, cow));
    }

    /** Citizens marks its NPCs with Bukkit metadata, which Paper deprecates; the test sets it the way Citizens does. */
    @SuppressWarnings("deprecation")
    @Test
    void npcsKeepTheirDamage() {
        zombie.setMetadata("NPC", new FixedMetadataValue(plugin, true));
        bloodNight();
        assertEquals(4, hit(zombie, player));
        assertEquals(4, hit(player, zombie));
    }

    @Test
    void regionsCanSwitchTheRulesOff() {
        bloodNight();
        denied = true;
        assertEquals(4, hit(zombie, player));
        assertEquals(4, hit(player, zombie));
    }

    @Test
    void ordinaryEnemiesTakeLessDamageFromPlayers() {
        bloodNight();
        assertEquals(1, hit(player, zombie));
        assertEquals(4, hit(player, cow));
    }
}
