/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Location;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * BloodNight's {@link RegionFlag}s as WorldGuard state flags. Only loaded when WorldGuard is installed. The flags have
 * to be registered while plugins load, before WorldGuard enables.
 */
public final class WorldGuardRules implements RegionRules {
    private final Map<RegionFlag, StateFlag> flags;

    private WorldGuardRules(Map<RegionFlag, StateFlag> flags) {
        this.flags = flags;
    }

    /**
     * Registers the flags with WorldGuard, or takes over flags of the same name another plugin registered.
     *
     * @return the rules; a flag WorldGuard does not take always allows
     */
    public static WorldGuardRules register(Logger logger) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        Map<RegionFlag, StateFlag> flags = new EnumMap<>(RegionFlag.class);
        for (RegionFlag flag : RegionFlag.values()) {
            StateFlag state = new StateFlag(flag.flagName(), true);
            try {
                registry.register(state);
                flags.put(flag, state);
            } catch (FlagConflictException | IllegalStateException e) {
                Flag<?> existing = registry.get(flag.flagName());
                if (existing instanceof StateFlag taken) {
                    flags.put(flag, taken);
                } else {
                    logger.warning("WorldGuard flag %s could not be registered: %s".formatted(flag.flagName(), e));
                }
            }
        }
        return new WorldGuardRules(flags);
    }

    @Override
    public boolean allows(Location location, RegionFlag flag) {
        StateFlag state = flags.get(flag);
        if (state == null) return true;
        return WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .createQuery()
                .testState(BukkitAdapter.adapt(location), null, state);
    }
}
