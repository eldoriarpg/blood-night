/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.configuration.elements.ForeignMobSettings;
import de.eldoria.bloodnight.integration.placeholders.CommandText;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.mobs.ForeignMobs;
import de.eldoria.bloodnight.tasks.Tasks;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathActionHandlerTest extends RulesTest {
    private final List<String> commands = new ArrayList<>();
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        settings = Settings.world("""
                {"deathActions":{"player":{
                  "loseInventoryChance":100,"loseExperienceChance":100,
                  "deathCommands":["record {player} {world}"],
                  "lightning":{"lightning":false,"thunder":false},
                  "shockwave":{"chance":0}}}}
                """);
        server.getCommandMap().register("test", new Command("record") {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                commands.add(String.join(" ", args));
                return true;
            }
        });
        server.getPluginManager()
                .registerEvents(
                        new DeathActionHandler(
                                server,
                                new Tasks(plugin),
                                nights,
                                ignored -> settings,
                                CommandText.NONE,
                                new ForeignMobs(ForeignMobSettings::new, () -> null),
                                RandomGenerator.getDefault(),
                                RegionRules.NONE),
                        plugin);
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
    }

    private PlayerDeathEvent die() {
        var event = new PlayerDeathEvent(
                player,
                DamageSource.builder(DamageType.GENERIC).build(),
                new ArrayList<>(List.of(new ItemStack(Material.DIAMOND))),
                10,
                "died");
        server.getPluginManager().callEvent(event);
        return event;
    }

    @Test
    void playersLoseTheirDropsDuringABloodNight() {
        bloodNight();
        var event = die();
        assertTrue(event.getDrops().isEmpty());
        assertEquals(0, event.getDroppedExp());
        server.getScheduler().performOneTick();
        assertEquals(List.of(player.getName() + " world"), commands);
    }

    @Test
    void ordinaryNightsKeepTheDrops() {
        var event = die();
        assertEquals(1, event.getDrops().size());
        assertEquals(10, event.getDroppedExp());
    }
}
