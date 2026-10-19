/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.data;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Turns a {@link ParticleDataSpec} into the data Paper draws a particle with, and says what is wrong with one.
 */
public final class ParticleDatas {
    private static final Pattern COLOR = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Color WHITE = Color.WHITE;

    private ParticleDatas() {}

    /**
     * @param particle the particle
     * @param spec     its data as written
     * @param trailEnd where a trail flies to, worked out by the caller from {@link ParticleDataSpec#toward()}
     * @param items    the item registry's items by id
     * @return the data, or {@code null} for particles that take none
     * @throws IllegalArgumentException for data effects do not offer
     */
    public static @Nullable Object build(
            Particle particle,
            ParticleDataSpec spec,
            @Nullable Location trailEnd,
            Function<String, @Nullable ItemStack> items) {
        return switch (DataKind.of(particle)) {
            case NONE -> null;
            case DUST -> new Particle.DustOptions(color(spec.color()), size(spec));
            case DUST_TRANSITION -> new Particle.DustTransition(color(spec.color()), color(spec.to()), size(spec));
            case SPELL -> new Particle.Spell(color(spec.color()), (float) power(spec));
            case COLOR -> color(spec.color());
            case POWER -> (float) power(spec);
            case DELAY -> Math.max(0, Objects.requireNonNullElse(spec.delay(), 0));
            case BLOCK ->
                Objects.requireNonNullElse(material(spec.block(), null), Material.STONE)
                        .createBlockData();
            case ITEM -> item(spec.item(), items);
            case TRAIL ->
                new Particle.Trail(
                        Objects.requireNonNull(trailEnd, "A trail needs where it flies to"),
                        color(spec.color()),
                        Math.max(1, Objects.requireNonNullElse(spec.ticks(), 20)));
            case UNSUPPORTED ->
                throw new IllegalArgumentException("%s takes data effects do not offer".formatted(particle.key()));
        };
    }

    /**
     * @param kind  the kind of data the particle takes
     * @param spec  the data as written
     * @param items whether an item id is known
     * @return what is wrong with the data, empty when it fits
     */
    public static List<String> problems(DataKind kind, ParticleDataSpec spec, Function<String, Boolean> items) {
        List<String> problems = new ArrayList<>();
        if (kind == DataKind.UNSUPPORTED) {
            problems.add("takes data effects do not offer");
            return problems;
        }
        List<String> fields = kind.fields();
        if (fields.contains("color") && !validColor(spec.color())) problems.add("needs a colour as #rrggbb");
        if (fields.contains("to") && !validColor(spec.to())) problems.add("needs an end colour as #rrggbb");
        if (fields.contains("block") && material(spec.block(), null) == null) {
            problems.add("needs a block, not %s".formatted(spec.block()));
        }
        if (fields.contains("item") && (spec.item() == null || !items.apply(spec.item()))) {
            problems.add("needs a known item, not %s".formatted(spec.item()));
        }
        return problems;
    }

    /**
     * @return the colour, white when it is not written as {@code #rrggbb}
     */
    public static Color color(@Nullable String color) {
        if (color == null || !validColor(color)) return WHITE;
        return Color.fromRGB(Integer.parseInt(color.substring(1), 16));
    }

    private static boolean validColor(@Nullable String color) {
        return color != null && COLOR.matcher(color).matches();
    }

    private static float size(ParticleDataSpec spec) {
        return (float) Math.clamp(Objects.requireNonNullElse(spec.size(), 1.0), 0.01, 4);
    }

    private static double power(ParticleDataSpec spec) {
        return Objects.requireNonNullElse(spec.power(), 1.0);
    }

    private static @Nullable Material material(@Nullable String key, @Nullable Material fallback) {
        if (key == null) return fallback;
        Material material = Material.matchMaterial(key.toLowerCase(Locale.ROOT));
        return material == null || !material.isBlock() ? fallback : material;
    }

    private static ItemStack item(@Nullable String id, Function<String, @Nullable ItemStack> items) {
        if (id == null) return new ItemStack(Material.STONE);
        ItemStack item = items.apply(id);
        if (item != null) return item;
        Material material = Material.matchMaterial(id);
        return new ItemStack(material == null || !material.isItem() ? Material.STONE : material);
    }
}
