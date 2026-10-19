/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.money;

import de.eldoria.bloodnight.graph.runtime.Integrations;
import de.eldoria.bloodnight.tasks.Tasks;
import org.bukkit.entity.Player;

/**
 * Vault's economy, paid on the global region. Without Vault or an economy plugin it is not available and takes
 * nothing.
 */
public final class VaultWallet implements Wallet {
    private final Tasks tasks;

    /**
     * @param tasks runs the payments on the global region
     */
    public VaultWallet(Tasks tasks) {
        this.tasks = tasks;
    }

    @Override
    public boolean available() {
        return Integrations.hasEconomy();
    }

    @Override
    public void pay(Player player, double amount) {
        tasks.globalNow(() -> Integrations.depositNow(player, amount));
    }

    @Override
    public String format(double amount) {
        return Integrations.formatMoney(amount);
    }
}
