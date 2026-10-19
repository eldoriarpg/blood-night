/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce.night;

import de.eldoria.bloodnight.announce.Texts;
import de.eldoria.bloodnight.api.event.night.BloodNightBeginEvent;
import de.eldoria.bloodnight.configuration.world.presentation.AnnouncementSettings;
import de.eldoria.bloodnight.night.BloodNights;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Explains blood nights to each player once, on their first one: a dialog saying what happens and what helps. It
 * shows {@value #DELAY} ticks after the night begins or the player enters its world, so the announcement comes first,
 * and only where the world's announcement settings ask for it. Players who saw it are marked {@link #SEEN} in their
 * own data.
 * <p>
 * The dialog is built from the plugin's texts in the player's language; the client gets it ready to show.
 */
public final class IntroDialog implements Listener {
    static final NamespacedKey SEEN = new NamespacedKey("bloodnight", "intro_seen");
    static final long DELAY = 60;
    private static final List<String> PARAGRAPHS = List.of("danger", "help", "reward", "end");
    private final BloodNights nights;
    private final Function<World, AnnouncementSettings> settings;
    private final Later later;
    private final Consumer<Player> show;

    /**
     * @param nights   the running blood nights
     * @param settings the announcement settings of a world
     * @param later    runs work on a player's thread after some ticks
     * @param show     shows the dialog to a player, e.g. {@link #show(Player)}
     */
    public IntroDialog(
            BloodNights nights, Function<World, AnnouncementSettings> settings, Later later, Consumer<Player> show) {
        this.nights = nights;
        this.settings = settings;
        this.later = later;
        this.show = show;
    }

    /**
     * Shows the dialog in the player's language.
     */
    public static void show(Player player) {
        player.showDialog(dialog(player));
    }

    /**
     * @return the dialog with every text in the player's language
     */
    static Dialog dialog(Player player) {
        List<DialogBody> body = PARAGRAPHS.stream()
                .map(paragraph -> (DialogBody) DialogBody.plainMessage(text(paragraph, player)))
                .toList();
        return Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(text("title", player))
                        .body(body)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.notice(
                        ActionButton.builder(text("ready", player)).build())));
    }

    private static Component text(String name, Player player) {
        return Texts.render(player, "bloodnight.intro." + name);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        for (Player player : event.getWorld().getPlayers()) offer(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        offer(event.getPlayer());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        offer(event.getPlayer());
    }

    private void offer(Player player) {
        if (seen(player)) return;
        later.run(player, DELAY, () -> {
            World world = player.getWorld();
            if (!player.isOnline() || seen(player) || nights.running(world).isEmpty()) return;
            if (!settings.apply(world).introDialog()) return;
            player.getPersistentDataContainer().set(SEEN, PersistentDataType.BOOLEAN, true);
            show.accept(player);
        });
    }

    private static boolean seen(Player player) {
        return player.getPersistentDataContainer().has(SEEN);
    }

    /**
     * Runs work on an entity's thread after a delay; nothing runs when the entity is gone by then.
     */
    @FunctionalInterface
    public interface Later {
        void run(Entity entity, long ticks, Runnable work);
    }
}
