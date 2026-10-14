/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.placeholders;

import de.eldoria.bloodnight.api.BloodNightService;
import de.eldoria.bloodnight.api.NightForecast;
import de.eldoria.bloodnight.api.RunningNight;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * The values of BloodNight's placeholders, for any placeholder plugin. Each placeholder takes an optional world name
 * as its last argument and uses the player's world without one.
 * <p>
 * {@code active}, {@code probability} (with an optional number of nights ahead first), {@code next_nights},
 * {@code next_time}, {@code time_left}, {@code seconds_left} and {@code percent_left}. Times are real time, from the
 * server's tick rate. {@code next_*} stay empty unless the next blood night was forced or the server reveals them.
 */
public final class Placeholders {
    /**
     * Every placeholder name, longest first, so a name never hides a longer one when parsing.
     */
    public static final List<String> NAMES =
            List.of("seconds_left", "percent_left", "next_nights", "probability", "time_left", "next_time", "active");

    private static final int HORIZON = 1000;
    private final BloodNightService service;
    private final Function<String, @Nullable World> worlds;
    private final DoubleSupplier tickRate;
    private final BooleanSupplier revealNext;

    /**
     * @param service    asks BloodNight
     * @param worlds     a world by name, or {@code null}
     * @param tickRate   the server's ticks per second
     * @param revealNext whether the next blood night may be told
     */
    public Placeholders(
            BloodNightService service,
            Function<String, @Nullable World> worlds,
            DoubleSupplier tickRate,
            BooleanSupplier revealNext) {
        this.service = service;
        this.worlds = worlds;
        this.tickRate = tickRate;
        this.revealNext = revealNext;
    }

    /**
     * Splits a PlaceholderAPI parameter like {@code probability_2_world_nether} into the name and its arguments: the
     * number of nights ahead, if the name takes one, and the world name, which may contain underscores itself.
     *
     * @return the name and its arguments, or {@code null} for an unknown placeholder
     */
    public static @Nullable Request parse(String parameter) {
        String lower = parameter.toLowerCase(Locale.ROOT);
        for (String name : NAMES) {
            if (lower.equals(name)) return new Request(name, List.of());
            if (!lower.startsWith(name + "_")) continue;
            String rest = parameter.substring(name.length() + 1);
            List<String> arguments = new ArrayList<>();
            if (name.equals("probability")) {
                int split = rest.indexOf('_');
                String first = split < 0 ? rest : rest.substring(0, split);
                if (first.chars().allMatch(Character::isDigit) && !first.isEmpty()) {
                    arguments.add(first);
                    rest = split < 0 ? "" : rest.substring(split + 1);
                }
            }
            if (!rest.isEmpty()) arguments.add(rest);
            return new Request(name, arguments);
        }
        return null;
    }

    /**
     * @param request     the placeholder
     * @param playerWorld the world of the player asking, if a player asks
     * @return the value, or {@code null} when it cannot be answered, e.g. no world was named and no player asks
     */
    public @Nullable String value(Request request, @Nullable World playerWorld) {
        List<String> arguments = new ArrayList<>(request.arguments());
        int ahead = 0;
        if (request.name().equals("probability") && !arguments.isEmpty() && isNumber(arguments.getFirst())) {
            ahead = Integer.parseInt(arguments.removeFirst());
        }
        World world = arguments.isEmpty() ? playerWorld : worlds.apply(String.join("_", arguments));
        if (world == null) return null;
        return switch (request.name()) {
            case "active" -> Boolean.toString(service.isRunning(world));
            case "probability" -> Integer.toString(service.probability(world, ahead));
            case "next_nights" ->
                next(world).map(next -> Long.toString(next.nightsAhead())).orElse("");
            case "next_time" ->
                next(world).map(next -> clock(next.ticksUntil())).orElse("");
            case "time_left" ->
                service.running(world).map(night -> clock(night.ticksLeft())).orElse(clock(0));
            case "seconds_left" ->
                service.running(world)
                        .map(night -> Long.toString(seconds(night.ticksLeft())))
                        .orElse("0");
            case "percent_left" ->
                String.format(
                        Locale.ROOT,
                        "%.1f",
                        service.running(world).map(RunningNight::shareLeft).orElse(0.0) * 100);
            default -> null;
        };
    }

    private Optional<NightForecast> next(World world) {
        return service.next(world, HORIZON).filter(next -> next.forced() || revealNext.getAsBoolean());
    }

    private long seconds(long ticks) {
        double rate = tickRate.getAsDouble();
        return Math.round(ticks / (rate <= 0 ? 20 : rate));
    }

    private String clock(long ticks) {
        Duration time = Duration.ofSeconds(seconds(ticks));
        return "%d:%02d:%02d".formatted(time.toHours(), time.toMinutesPart(), time.toSecondsPart());
    }

    private static boolean isNumber(String text) {
        return !text.isEmpty() && text.chars().allMatch(Character::isDigit);
    }

    /**
     * A placeholder and its arguments.
     *
     * @param name      the placeholder's name, e.g. {@code probability}
     * @param arguments its arguments: nights ahead for {@code probability}, then a world name
     */
    public record Request(String name, List<String> arguments) {}
}
