/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.api.event.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.elements.world.NightRuleSettings;
import de.eldoria.bloodnight.configuration.elements.world.rules.WeatherChange;
import de.eldoria.bloodnight.night.BloodNights;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Changes a world while its blood night runs, as its {@link NightRuleSettings} say: difficulty, monster spawning,
 * weather and game rules; and keeps natural phantoms away outside of blood nights where asked.
 * <p>
 * What a world had before is kept in the world's own data, so it comes back when the night ends, when the plugin
 * stops, or on the next start after a crash. A value someone changed during the night stays as they set it. Worlds
 * are only changed on the global region; elsewhere (Folia stops plugins off it) the next start gives the values back.
 */
public final class WorldRules implements Listener {
    static final NamespacedKey SNAPSHOT = new NamespacedKey("bloodnight", "night_rules");
    private static final NamespacedKey PREVIOUS = new NamespacedKey("bloodnight", "previous");
    private static final NamespacedKey APPLIED = new NamespacedKey("bloodnight", "applied");
    private static final String GAME_RULE = "rule/";
    private static final int WEATHER_TICKS = 24_000;
    private final BloodNights nights;
    private final Function<World, NightRuleSettings> settings;
    private final Function<String, @Nullable GameRule<?>> gameRules;
    private final BooleanSupplier global;
    private final Logger logger;

    /**
     * @param nights    the running blood nights
     * @param settings  the night rules of a world
     * @param gameRules the game rule of a key such as {@code minecraft:spawn_phantoms}, if there is one
     * @param global    whether the current thread may change worlds
     * @param logger    where invalid game rules are reported
     */
    public WorldRules(
            BloodNights nights,
            Function<World, NightRuleSettings> settings,
            Function<String, @Nullable GameRule<?>> gameRules,
            BooleanSupplier global,
            Logger logger) {
        this.nights = nights;
        this.settings = settings;
        this.gameRules = gameRules;
        this.global = global;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        World world = event.getWorld();
        if (world.getPersistentDataContainer().has(SNAPSHOT)) return;
        Map<String, String> previous = new LinkedHashMap<>();
        Map<String, String> applied = new LinkedHashMap<>();
        targets(world, settings.apply(world))
                .forEach((name, value) -> knob(world, name).ifPresent(knob -> {
                    previous.put(name, knob.read().get());
                    knob.write().accept(value);
                    applied.put(name, knob.read().get());
                }));
        if (!applied.isEmpty()) save(world, previous, applied);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        if (global.getAsBoolean()) restore(event.getWorld());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.PHANTOM || event.getSpawnReason() != SpawnReason.NATURAL) return;
        World world = event.getEntity().getWorld();
        if (settings.apply(world).phantomsOnlyAtBloodNights()
                && nights.running(world).isEmpty()) {
            event.setCancelled(true);
        }
    }

    /**
     * Gives back what worlds without a running blood night still have changed, e.g. after a crash. Runs on the global
     * region when the plugin enables.
     */
    public void restoreLeftovers(Collection<? extends World> worlds) {
        for (World world : worlds) {
            if (nights.running(world).isEmpty()) restore(world);
        }
    }

    private Map<String, String> targets(World world, NightRuleSettings rules) {
        Map<String, String> targets = new LinkedHashMap<>();
        Difficulty difficulty = rules.difficulty().difficulty();
        if (difficulty != null) targets.put("difficulty", difficulty.name());
        if (rules.monsterSpawnLimit() >= 0) targets.put("monster_limit", String.valueOf(rules.monsterSpawnLimit()));
        if (rules.monsterTicksPerSpawn() >= 0) {
            targets.put("monster_ticks", String.valueOf(rules.monsterTicksPerSpawn()));
        }
        if (rules.weather() != WeatherChange.UNCHANGED)
            targets.put("weather", rules.weather().name());
        rules.gameRules().forEach((key, value) -> {
            if (gameRules.apply(key) == null) {
                logger.warning("Unknown game rule %s in the night rules of %s".formatted(key, world.getName()));
            } else {
                targets.put(GAME_RULE + key.replace(':', '/'), value);
            }
        });
        return targets;
    }

    private void restore(World world) {
        PersistentDataContainer data = world.getPersistentDataContainer();
        PersistentDataContainer snapshot = data.get(SNAPSHOT, PersistentDataType.TAG_CONTAINER);
        if (snapshot == null) return;
        Map<String, String> previous = read(snapshot, PREVIOUS);
        Map<String, String> applied = read(snapshot, APPLIED);
        previous.forEach((name, value) -> knob(world, name).ifPresent(knob -> {
            if (knob.read().get().equals(applied.get(name))) knob.write().accept(value);
        }));
        data.remove(SNAPSHOT);
    }

    private Optional<Knob> knob(World world, String name) {
        return switch (name) {
            case "difficulty" ->
                Optional.of(new Knob(
                        () -> world.getDifficulty().name(), value -> world.setDifficulty(Difficulty.valueOf(value))));
            case "monster_limit" ->
                Optional.of(new Knob(
                        () -> String.valueOf(world.getSpawnLimit(SpawnCategory.MONSTER)),
                        value -> world.setSpawnLimit(SpawnCategory.MONSTER, Integer.parseInt(value))));
            case "monster_ticks" ->
                Optional.of(new Knob(
                        () -> String.valueOf(world.getTicksPerSpawns(SpawnCategory.MONSTER)),
                        value -> world.setTicksPerSpawns(SpawnCategory.MONSTER, Integer.parseInt(value))));
            case "weather" -> Optional.of(new Knob(() -> weather(world).name(), value -> weather(world, value)));
            default -> gameRuleKnob(world, name);
        };
    }

    private Optional<Knob> gameRuleKnob(World world, String name) {
        if (!name.startsWith(GAME_RULE)) return Optional.empty();
        String key = name.substring(GAME_RULE.length()).replaceFirst("/", ":");
        GameRule<?> rule = gameRules.apply(key);
        return rule == null ? Optional.empty() : Optional.of(gameRuleKnob(world, rule, key));
    }

    private <T> Knob gameRuleKnob(World world, GameRule<T> rule, String key) {
        return new Knob(() -> String.valueOf(world.getGameRuleValue(rule)), value -> {
            T parsed = parse(rule, value);
            if (parsed == null) {
                logger.warning("Game rule %s does not take %s".formatted(key, value));
            } else {
                world.setGameRule(rule, parsed);
            }
        });
    }

    private static <T> @Nullable T parse(GameRule<T> rule, String value) {
        Class<T> type = rule.getType();
        if (type == Boolean.class && (value.equals("true") || value.equals("false"))) {
            return type.cast(Boolean.valueOf(value));
        }
        if (type == Integer.class) {
            try {
                return type.cast(Integer.valueOf(value));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static WeatherChange weather(World world) {
        if (world.isThundering()) return WeatherChange.THUNDER;
        return world.hasStorm() ? WeatherChange.RAIN : WeatherChange.CLEAR;
    }

    private static void weather(World world, String value) {
        WeatherChange weather = WeatherChange.valueOf(value);
        world.setStorm(weather != WeatherChange.CLEAR);
        world.setThundering(weather == WeatherChange.THUNDER);
        if (weather == WeatherChange.CLEAR) return;
        world.setWeatherDuration(WEATHER_TICKS);
        if (weather == WeatherChange.THUNDER) world.setThunderDuration(WEATHER_TICKS);
    }

    private static void save(World world, Map<String, String> previous, Map<String, String> applied) {
        PersistentDataContainer data = world.getPersistentDataContainer();
        PersistentDataContainer snapshot = data.getAdapterContext().newPersistentDataContainer();
        snapshot.set(PREVIOUS, PersistentDataType.TAG_CONTAINER, write(data, previous));
        snapshot.set(APPLIED, PersistentDataType.TAG_CONTAINER, write(data, applied));
        data.set(SNAPSHOT, PersistentDataType.TAG_CONTAINER, snapshot);
    }

    private static PersistentDataContainer write(PersistentDataContainer data, Map<String, String> values) {
        PersistentDataContainer container = data.getAdapterContext().newPersistentDataContainer();
        values.forEach((name, value) ->
                container.set(new NamespacedKey("bloodnight", name), PersistentDataType.STRING, value));
        return container;
    }

    private static Map<String, String> read(PersistentDataContainer snapshot, NamespacedKey key) {
        Map<String, String> values = new LinkedHashMap<>();
        PersistentDataContainer container = snapshot.get(key, PersistentDataType.TAG_CONTAINER);
        if (container == null) return values;
        for (NamespacedKey name : container.getKeys()) {
            String value = container.get(name, PersistentDataType.STRING);
            if (value != null) values.put(name.getKey(), value);
        }
        return values;
    }

    /**
     * One world value, read and written as text.
     */
    private record Knob(Supplier<String> read, Consumer<String> write) {}
}
