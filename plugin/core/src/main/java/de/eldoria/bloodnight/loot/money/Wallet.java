/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.money;

import org.bukkit.entity.Player;

/**
 * Where kill money goes: the economy players keep their money in.
 */
public interface Wallet {
    /**
     * @return whether an economy takes payments now
     */
    boolean available();

    /**
     * Pays a player, on whatever thread the economy needs.
     */
    void pay(Player player, double amount);

    /**
     * @return the amount written the way the economy shows money, e.g. {@code 12.50 Coins}
     */
    String format(double amount);
}
