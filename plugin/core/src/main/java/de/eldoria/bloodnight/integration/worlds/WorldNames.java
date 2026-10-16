/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.worlds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The name players see for a world: its own {@code displayName} (MiniMessage) when set, else its alias in
 * Multiverse-Core 5, else its display name in Hyperverse, else the world's folder name. Aliases of the world plugins
 * may use {@code &} colour codes.
 * <p>
 * Both world plugins are reached through reflection on their own class loaders, so BloodNight needs neither to build
 * or run.
 */
public final class WorldNames {
    private final Function<World, String> displayNames;
    private final Supplier<@Nullable ClassLoader> multiverse;
    private final Supplier<@Nullable ClassLoader> hyperverse;

    /**
     * @param displayNames the {@code displayName} a world sets, empty for none
     * @param multiverse   the class loader of Multiverse-Core while it runs, else {@code null}
     * @param hyperverse   the class loader of Hyperverse while it runs, else {@code null}
     */
    public WorldNames(
            Function<World, String> displayNames,
            Supplier<@Nullable ClassLoader> multiverse,
            Supplier<@Nullable ClassLoader> hyperverse) {
        this.displayNames = displayNames;
        this.multiverse = multiverse;
        this.hyperverse = hyperverse;
    }

    /**
     * @return the world's name for players
     */
    public Component name(World world) {
        String own = displayNames.apply(world);
        if (!own.isBlank()) return MiniMessage.miniMessage().deserialize(own);
        String alias = rawAlias(world);
        if (alias != null) return LegacyComponentSerializer.legacyAmpersand().deserialize(alias);
        return Component.text(world.getName());
    }

    /**
     * @return the world's name for players, without colours
     */
    public String plain(World world) {
        return PlainTextComponentSerializer.plainText().serialize(name(world));
    }

    /**
     * @return the world's alias in Multiverse-Core or Hyperverse without colours, empty when it has none
     */
    public String alias(World world) {
        String alias = rawAlias(world);
        if (alias == null) return "";
        return PlainTextComponentSerializer.plainText()
                .serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(alias));
    }

    private @Nullable String rawAlias(World world) {
        String alias = multiverseAlias(world);
        if (alias == null) alias = hyperverseAlias(world);
        return alias == null || alias.isBlank() ? null : alias;
    }

    /**
     * {@code MultiverseCoreApi.get().getWorldManager().getWorld(name).getOrNull().getAlias()}.
     */
    private @Nullable String multiverseAlias(World world) {
        ClassLoader loader = multiverse.get();
        if (loader == null) return null;
        try {
            Object api = Class.forName("org.mvplugins.multiverse.core.MultiverseCoreApi", true, loader)
                    .getMethod("get")
                    .invoke(null);
            Object manager = call(api, "getWorldManager");
            if (manager == null) return null;
            Method getWorld = manager.getClass().getMethod("getWorld", String.class);
            Object found = call(getWorld.invoke(manager, world.getName()), "getOrNull");
            return found == null ? null : (String) call(found, "getAlias");
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            return null;
        }
    }

    /**
     * {@code Hyperverse.getApi().getWorldManager().getWorld(world).getDisplayName()}.
     */
    private @Nullable String hyperverseAlias(World world) {
        ClassLoader loader = hyperverse.get();
        if (loader == null) return null;
        try {
            Object api = Class.forName("se.hyperver.hyperverse.Hyperverse", true, loader)
                    .getMethod("getApi")
                    .invoke(null);
            Object manager = call(api, "getWorldManager");
            if (manager == null) return null;
            Object found = manager.getClass().getMethod("getWorld", World.class).invoke(manager, world);
            return found == null ? null : (String) call(found, "getDisplayName");
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            return null;
        }
    }

    private static @Nullable Object call(@Nullable Object target, String method) throws ReflectiveOperationException {
        if (target == null) return null;
        Method found = target.getClass().getMethod(method);
        found.setAccessible(true);
        return found.invoke(target);
    }
}
