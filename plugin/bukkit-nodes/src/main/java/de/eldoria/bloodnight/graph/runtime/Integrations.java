/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

import de.eldoria.bloodnight.api.internal.MobContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * What the nodes of other plugins call: PlaceholderAPI, Vault's economy and MythicMobs. Each plugin is reached through
 * reflection on its own class loader, so none of them is needed to build or run. Without the plugin a node does
 * nothing: texts stay as they are, balances are 0, nothing is paid, spawned or cast.
 */
public final class Integrations {
    private Integrations() {}

    /**
     * @return the text with PlaceholderAPI's placeholders filled in for the player, or as it is without PlaceholderAPI
     */
    public static String placeholders(String text, @Nullable Player player) {
        try {
            Class<?> api = pluginClass("PlaceholderAPI", "me.clip.placeholderapi.PlaceholderAPI");
            if (api == null) return text;
            Object filled = api.getMethod("setPlaceholders", OfflinePlayer.class, String.class)
                    .invoke(null, player, text);
            return filled instanceof String result ? result : text;
        } catch (ReflectiveOperationException | LinkageError e) {
            return text;
        }
    }

    /**
     * Pays a player through Vault, on the global region.
     */
    public static void deposit(MobContext context, Player player, double amount) {
        context.global(() -> depositNow(player, amount));
    }

    /**
     * Pays a player through Vault on the current thread, which should be the global region.
     */
    public static void depositNow(Player player, double amount) {
        economy("depositPlayer", player, amount);
    }

    /**
     * @return whether Vault and an economy plugin take payments
     */
    public static boolean hasEconomy() {
        return Boolean.TRUE.equals(invokeEconomy("isEnabled", new Class<?>[0], new Object[0]));
    }

    /**
     * @return the amount written the way the economy shows money, e.g. {@code 12.50 Coins}; plain without one
     */
    public static String formatMoney(double amount) {
        Object formatted = invokeEconomy("format", new Class<?>[] {double.class}, new Object[] {amount});
        return formatted instanceof String text ? text : "%.2f".formatted(amount);
    }

    /**
     * Takes money from a player through Vault, on the global region.
     */
    public static void withdraw(MobContext context, Player player, double amount) {
        context.global(() -> economy("withdrawPlayer", player, amount));
    }

    /**
     * @return the player's balance in Vault's economy, 0 without one
     */
    public static double balance(Player player) {
        Object balance = economy("getBalance", player);
        return balance instanceof Number number ? number.doubleValue() : 0;
    }

    /**
     * Lets an entity cast a MythicMobs skill.
     *
     * @return whether the skill was cast
     */
    public static boolean castSkill(Entity caster, String skill) {
        try {
            Object helper = mythicHelper();
            if (helper == null) return false;
            Object cast = helper.getClass()
                    .getMethod("castSkill", Entity.class, String.class)
                    .invoke(helper, caster, skill);
            return Boolean.TRUE.equals(cast);
        } catch (ReflectiveOperationException | LinkageError e) {
            return false;
        }
    }

    /**
     * Spawns a MythicMobs mob, on the thread owning the location.
     */
    public static void spawnMythicMob(MobContext context, String type, Location location, int level) {
        context.at(location, () -> spawnMythicMob(type, location, level));
    }

    /**
     * Spawns a MythicMobs mob on the current thread, which must own the location.
     *
     * @return the spawned entity, or {@code null} without MythicMobs or for an unknown type
     */
    public static @Nullable Entity spawnMythicMob(String type, Location location, int level) {
        try {
            Object helper = mythicHelper();
            if (helper == null) return null;
            Object spawned = helper.getClass()
                    .getMethod("spawnMythicMob", String.class, Location.class, int.class)
                    .invoke(helper, type, location, level);
            return spawned instanceof Entity entity ? entity : null;
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /**
     * @return the names of every MythicMobs mob, sorted; empty without MythicMobs
     */
    public static List<String> mythicMobTypes() {
        try {
            Class<?> bukkit = pluginClass("MythicMobs", "io.lumine.mythic.bukkit.MythicBukkit");
            if (bukkit == null) return List.of();
            Object manager = call(bukkit.getMethod("inst").invoke(null), "getMobManager");
            Object names = call(manager, "getMobNames");
            if (!(names instanceof Collection<?> collection)) return List.of();
            return collection.stream().map(String::valueOf).sorted().toList();
        } catch (ReflectiveOperationException | LinkageError e) {
            return List.of();
        }
    }

    private static @Nullable Object mythicHelper() throws ReflectiveOperationException {
        Class<?> bukkit = pluginClass("MythicMobs", "io.lumine.mythic.bukkit.MythicBukkit");
        return bukkit == null ? null : call(bukkit.getMethod("inst").invoke(null), "getAPIHelper");
    }

    private static @Nullable Object economy(String method, Player player, Object... more) {
        Class<?>[] parameters = more.length == 0
                ? new Class<?>[] {OfflinePlayer.class}
                : new Class<?>[] {OfflinePlayer.class, double.class};
        Object[] arguments = more.length == 0 ? new Object[] {player} : new Object[] {player, more[0]};
        return invokeEconomy(method, parameters, arguments);
    }

    private static @Nullable Object invokeEconomy(String method, Class<?>[] parameters, Object[] arguments) {
        try {
            Class<?> type = pluginClass("Vault", "net.milkbowl.vault.economy.Economy");
            if (type == null) return null;
            RegisteredServiceProvider<?> registration =
                    Bukkit.getServicesManager().getRegistration(type);
            if (registration == null) return null;
            return type.getMethod(method, parameters).invoke(registration.getProvider(), arguments);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private static @Nullable Class<?> pluginClass(String plugin, String name) throws ClassNotFoundException {
        Plugin found = Bukkit.getPluginManager().getPlugin(plugin);
        if (found == null || !found.isEnabled()) return null;
        return Class.forName(name, true, found.getClass().getClassLoader());
    }

    private static @Nullable Object call(@Nullable Object target, String method) throws ReflectiveOperationException {
        if (target == null) return null;
        Method found = target.getClass().getMethod(method);
        found.setAccessible(true);
        return found.invoke(target);
    }
}
