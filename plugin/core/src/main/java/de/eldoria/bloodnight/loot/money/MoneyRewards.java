/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.money;

import de.eldoria.bloodnight.announce.Delivery;
import de.eldoria.bloodnight.configuration.server.Announcements;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Pays the money of a kill. The amount is split among the players who damaged the mob, by the damage each dealt (see
 * {@link DamageShares}); players offline when it dies get nothing, and a share below one cent is not paid. When no
 * damage was counted, the killer gets it all. Each paid player is told their share the way
 * {@link Announcements#personalMethod()} says, unless {@link Announcements#money()} switches that off. Without an
 * economy nothing is paid.
 */
public final class MoneyRewards {
    private final Wallet wallet;
    private final DamageShares damage;
    private final Function<UUID, @Nullable Player> players;
    private final Supplier<Announcements> announcements;

    /**
     * @param wallet        where the money goes
     * @param damage        the damage players dealt
     * @param players       an online player by id
     * @param announcements how players are told
     */
    public MoneyRewards(
            Wallet wallet,
            DamageShares damage,
            Function<UUID, @Nullable Player> players,
            Supplier<Announcements> announcements) {
        this.wallet = wallet;
        this.damage = damage;
        this.players = players;
        this.announcements = announcements;
    }

    /**
     * Pays the money of a mob a player killed.
     *
     * @param mob    the mob that died
     * @param killer the player who killed it
     * @param amount the money of the kill, split among those who damaged it
     */
    public void pay(LivingEntity mob, Player killer, double amount) {
        Map<UUID, Double> dealt = damage.take(mob);
        if (amount <= 0 || !wallet.available()) return;
        Announcements config = announcements.get();
        shares(dealt, killer, amount).forEach((player, share) -> {
            wallet.pay(player, share);
            if (config.money()) {
                Delivery.send(
                        player,
                        config.personalMethod(),
                        "bloodnight.money.paid",
                        Placeholder.unparsed("amount", wallet.format(share)));
            }
        });
    }

    private Map<Player, Double> shares(Map<UUID, Double> dealt, Player killer, double amount) {
        Map<Player, Double> online = new LinkedHashMap<>();
        dealt.forEach((id, damage) -> {
            Player player = players.apply(id);
            if (player != null) online.put(player, damage);
        });
        double total = online.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) return Map.of(killer, amount);
        Map<Player, Double> shares = new LinkedHashMap<>();
        online.forEach((player, damage) -> {
            double share = Math.floor(amount * damage / total * 100) / 100.0;
            if (share >= 0.01) shares.put(player, share);
        });
        return shares;
    }
}
