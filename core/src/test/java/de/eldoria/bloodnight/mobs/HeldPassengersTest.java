/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.graph.runtime.Carrying;
import org.bukkit.Input;
import org.bukkit.World;
import org.bukkit.entity.Phantom;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeldPassengersTest {
    private ServerMock server;
    private PlayerMock player;
    private Phantom phantom;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        World world = server.addSimpleWorld("world");
        server.getPluginManager().registerEvents(new HeldPassengers(), MockBukkit.createMockPlugin());
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        phantom = world.spawn(world.getSpawnLocation(), Phantom.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private boolean dismount() {
        EntityDismountEvent event = new EntityDismountEvent(player, phantom, true);
        server.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    @Test
    void carriedPlayersCannotGetOffOnTheirOwn() {
        assertTrue(Carrying.carry(phantom, player));
        assertTrue(phantom.getPassengers().contains(player));
        assertFalse(dismount());
    }

    @Test
    void droppedPlayersAreFree() {
        Carrying.carry(phantom, player);
        Carrying.drop(phantom);

        assertFalse(phantom.getPassengers().contains(player));
        assertFalse(player.getPersistentDataContainer().has(Carrying.HELD));
        assertTrue(dismount());
    }

    @Test
    void theCarriersDeathLetsGo() {
        Carrying.carry(phantom, player);
        phantom.remove();

        assertTrue(dismount());
        assertFalse(player.getPersistentDataContainer().has(Carrying.HELD));
    }

    private void sneak(boolean sneak) {
        Input input = (Input) Proxy.newProxyInstance(
                Input.class.getClassLoader(),
                new Class<?>[] {Input.class},
                (proxy, method, args) -> method.getName().equals("isSneak") ? sneak : false);
        server.getPluginManager().callEvent(new PlayerInputEvent(player, input));
    }

    private void press(int times) {
        for (int i = 0; i < times; i++) {
            sneak(true);
            sneak(false);
        }
    }

    @Test
    void fivePressesOfSneakBreakLoose() {
        Carrying.carry(phantom, player);
        press(HeldPassengers.BREAK_FREE - 1);
        assertTrue(phantom.getPassengers().contains(player));

        press(1);
        assertFalse(phantom.getPassengers().contains(player));
        assertFalse(player.getPersistentDataContainer().has(Carrying.HELD));
    }

    @Test
    void holdingSneakCountsOnce() {
        Carrying.carry(phantom, player);
        for (int i = 0; i < 20; i++) sneak(true);
        assertTrue(phantom.getPassengers().contains(player));
    }

    @Test
    void aNewGrabStartsTheCountAgain() {
        Carrying.carry(phantom, player);
        press(HeldPassengers.BREAK_FREE - 1);
        Carrying.drop(phantom);
        Carrying.carry(phantom, player);
        press(HeldPassengers.BREAK_FREE - 1);
        assertTrue(phantom.getPassengers().contains(player));
    }

    @Test
    void nobodyCarriesThemselves() {
        assertFalse(Carrying.carry(phantom, phantom));
    }
}
