/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

import de.eldoria.bloodnight.api.MobContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Warnings drawn with display entities, which every client shows without the resource pack. Displays are tagged
 * {@link #DISPLAY}, never saved with the world, lit at full brightness and taken away when their time is up. A mob
 * has at most {@value #PER_MOB} displays at once, so a behaviour in a loop cannot flood the world; more are not
 * shown. A display meant for one player is hidden from everyone else.
 */
public final class Displays {
    /**
     * Marks BloodNight's displays.
     */
    public static final NamespacedKey DISPLAY = new NamespacedKey("bloodnight", "display");

    /**
     * How many displays one mob may have at once.
     */
    public static final int PER_MOB = 8;

    private static final int GROW_START = 2;
    private static final float HIDDEN = 0.001f;
    private static final float ABOVE = 0.4f;
    private static final Display.Brightness BRIGHT = new Display.Brightness(15, 15);
    private static final Map<UUID, AtomicInteger> ALIVE = new ConcurrentHashMap<>();

    private Displays() {}

    /**
     * Shows a coloured marker that grows from nothing to its size over its time, then disappears.
     *
     * @param size    radius of an area or cross, height of a pillar, in blocks
     * @param seconds how long it shows
     * @param viewer  the only player who sees it, or {@code null} for everyone
     */
    public static void marker(
            MobContext context,
            Location location,
            MarkerShape shape,
            Color color,
            double size,
            double seconds,
            @Nullable Player viewer) {
        long ticks = ticks(seconds);
        Material glass = glass(color);
        for (MarkerShape.Bar bar : shape.bars(size)) {
            spawn(
                    context,
                    location,
                    BlockDisplay.class,
                    viewer,
                    ticks,
                    display -> {
                        display.setBlock(glass.createBlockData());
                        display.setGlowing(true);
                        display.setGlowColorOverride(color);
                        display.setTransformation(box(bar, HIDDEN));
                    },
                    display -> grow(context, display, box(bar, 1), ticks));
        }
    }

    /**
     * Shows floating text that faces whoever looks at it and can be seen through walls.
     *
     * @param text    MiniMessage
     * @param size    the text's scale, 1 for normal
     * @param seconds how long it shows
     * @param viewer  the only player who sees it, or {@code null} for everyone
     */
    public static void text(
            MobContext context, Location location, String text, double size, double seconds, @Nullable Player viewer) {
        spawn(
                context,
                location,
                TextDisplay.class,
                viewer,
                ticks(seconds),
                display -> {
                    display.text(MiniMessage.miniMessage().deserialize(text));
                    display.setBillboard(Display.Billboard.CENTER);
                    display.setSeeThrough(true);
                    display.setTransformation(scaled((float) Math.max(size, 0.1), 0));
                },
                display -> {});
    }

    /**
     * Shows text or an item over an entity, riding it so it follows, e.g. a warning over a creeper about to blow.
     *
     * @param text    MiniMessage, or {@code null} to show the item
     * @param item    the item to show when there is no text
     * @param seconds how long it shows
     */
    public static void above(
            MobContext context, Entity entity, @Nullable String text, @Nullable ItemStack item, double seconds) {
        long ticks = ticks(seconds);
        if (text != null && !text.isBlank()) {
            spawn(
                    context,
                    entity.getLocation(),
                    TextDisplay.class,
                    null,
                    ticks,
                    display -> {
                        display.text(MiniMessage.miniMessage().deserialize(text));
                        display.setBillboard(Display.Billboard.CENTER);
                        display.setSeeThrough(true);
                        display.setTransformation(scaled(1, ABOVE));
                    },
                    entity::addPassenger);
        } else if (item != null) {
            spawn(
                    context,
                    entity.getLocation(),
                    ItemDisplay.class,
                    null,
                    ticks,
                    display -> {
                        display.setItemStack(item);
                        display.setBillboard(Display.Billboard.CENTER);
                        display.setTransformation(scaled(0.6f, ABOVE));
                    },
                    entity::addPassenger);
        }
    }

    /**
     * @return the stained glass whose colour is nearest to the given one
     */
    static Material glass(Color color) {
        DyeColor nearest = Arrays.stream(DyeColor.values())
                .min(Comparator.comparingInt(dye -> distance(dye.getColor(), color)))
                .orElse(DyeColor.RED);
        return Material.valueOf(nearest.name() + "_STAINED_GLASS");
    }

    private static int distance(Color a, Color b) {
        int red = a.getRed() - b.getRed();
        int green = a.getGreen() - b.getGreen();
        int blue = a.getBlue() - b.getBlue();
        return red * red + green * green + blue * blue;
    }

    private static <T extends Display> void spawn(
            MobContext context,
            Location location,
            Class<T> type,
            @Nullable Player viewer,
            long ticks,
            Consumer<T> setup,
            Consumer<T> shown) {
        UUID owner = context.entity().getUniqueId();
        AtomicInteger alive = ALIVE.computeIfAbsent(owner, ignored -> new AtomicInteger());
        if (alive.incrementAndGet() > PER_MOB) {
            release(owner, alive);
            return;
        }
        context.at(location, () -> {
            T display = location.getWorld().spawn(location, type, spawned -> {
                spawned.setPersistent(false);
                spawned.setBrightness(BRIGHT);
                spawned.getPersistentDataContainer().set(DISPLAY, PersistentDataType.BOOLEAN, true);
                if (viewer != null) spawned.setVisibleByDefault(false);
                setup.accept(spawned);
            });
            if (viewer != null) context.reveal(display, viewer);
            shown.accept(display);
            context.later(
                    display,
                    ticks,
                    () -> {
                        display.remove();
                        release(owner, alive);
                    },
                    () -> release(owner, alive));
        });
    }

    private static void grow(MobContext context, Display display, Transformation full, long ticks) {
        Runnable gone = () -> {};
        context.later(
                display,
                GROW_START,
                () -> {
                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration((int) Math.max(1, ticks - GROW_START));
                    display.setTransformation(full);
                },
                gone);
    }

    private static void release(UUID owner, AtomicInteger alive) {
        if (alive.decrementAndGet() <= 0) ALIVE.remove(owner, alive);
    }

    private static Transformation box(MarkerShape.Bar bar, float scale) {
        return new Transformation(
                new Vector3f((float) bar.x() * scale, (float) bar.y(), (float) bar.z() * scale),
                new AxisAngle4f(),
                new Vector3f((float) bar.width() * scale, (float) bar.height() * scale, (float) bar.depth() * scale),
                new AxisAngle4f());
    }

    private static Transformation scaled(float scale, float lift) {
        return new Transformation(
                new Vector3f(0, lift, 0), new AxisAngle4f(), new Vector3f(scale, scale, scale), new AxisAngle4f());
    }

    private static long ticks(double seconds) {
        return Math.max(1, Math.round(Math.max(seconds, 0) * 20));
    }
}
