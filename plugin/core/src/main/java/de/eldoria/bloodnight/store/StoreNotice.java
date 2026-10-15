/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.announce.Texts;
import de.eldoria.bloodnight.mob.CustomMob;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Tells admins with {@value #PERMISSION} on join when store mobs they changed have a newer version, which only
 * {@code /bloodnight store update} takes.
 */
public final class StoreNotice implements Listener {
    /**
     * The permission to hear about waiting updates.
     */
    public static final String PERMISSION = "bloodnight.command.store";

    static final String WAITING = "bloodnight.command.store.waiting";
    private final MobStore store;
    private final Supplier<Collection<CustomMob>> mobs;

    /**
     * @param store the store
     * @param mobs  the server's mobs
     */
    public StoreNotice(MobStore store, Supplier<Collection<CustomMob>> mobs) {
        this.store = store;
        this.mobs = mobs;
    }

    /**
     * @return the ids of changed mobs with a newer version in the store
     */
    public List<String> waiting() {
        return mobs.get().stream()
                .filter(mob -> store.state(mob) == StoreState.CHANGED_OUTDATED)
                .map(CustomMob::id)
                .sorted()
                .toList();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PERMISSION)) return;
        List<String> waiting = waiting();
        if (waiting.isEmpty()) return;
        Texts.sender().sendMessage(player, WAITING, Placeholder.unparsed("mobs", String.join(", ", waiting)));
    }
}
