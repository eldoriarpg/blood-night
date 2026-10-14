/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.configuration.elements.ResourcePackSettings;
import de.eldoria.bloodnight.tasks.Tasks;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Offers the BloodNight resource pack to players when they join. It holds the screen effect blood nights switch on; it
 * is loaded once and stays, so switching the effect on and off never reloads the client's resources.
 * <p>
 * The client checks the pack against its SHA-1 hash, which is computed by downloading the pack once when the plugin
 * enables or reloads, off the server's threads. Until it is known, or when the download fails, nobody is offered the
 * pack.
 */
public final class BloodPack implements Listener {
    private static final UUID ID = UUID.nameUUIDFromBytes("bloodnight:resource_pack".getBytes(StandardCharsets.UTF_8));
    private final Supplier<ResourcePackSettings> settings;
    private final Tasks tasks;
    private final Logger logger;
    private final BooleanSupplier merged;
    private volatile CompletableFuture<ResourcePackInfo> pack =
            CompletableFuture.failedFuture(new IllegalStateException());

    /**
     * @param settings the server's resource pack settings
     * @param tasks    sends the offer on the player's thread
     * @param logger   told when the pack cannot be downloaded
     * @param merged   whether a pack plugin sends the pack with its own, so BloodNight offers nothing itself
     */
    public BloodPack(Supplier<ResourcePackSettings> settings, Tasks tasks, Logger logger, BooleanSupplier merged) {
        this.settings = settings;
        this.tasks = tasks;
        this.logger = logger;
        this.merged = merged;
    }

    /**
     * Downloads the pack from its configured address to learn its hash.
     */
    public void refresh() {
        ResourcePackSettings current = settings.get();
        if (!current.offer() || merged.getAsBoolean()) {
            pack = CompletableFuture.failedFuture(new IllegalStateException("Not offered"));
            return;
        }
        pack = ResourcePackInfo.resourcePackInfo()
                .id(ID)
                .uri(URI.create(current.url()))
                .computeHashAndBuild();
        pack.whenComplete((info, error) -> {
            if (error != null) {
                logger.log(Level.WARNING, "The resource pack at %s cannot be offered".formatted(current.url()), error);
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        pack.thenAccept(info -> tasks.entity(player, () -> offer(player, info)));
    }

    private void offer(Player player, ResourcePackInfo info) {
        ResourcePackSettings current = settings.get();
        player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                .packs(info)
                .required(current.required())
                .replace(false)
                .prompt(MiniMessage.miniMessage().deserialize(current.prompt())));
    }
}
