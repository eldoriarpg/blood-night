/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import de.eldoria.bloodnight.particles.effect.Anchor;
import de.eldoria.bloodnight.particles.geometry.Facing;
import de.eldoria.bloodnight.particles.geometry.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;

/**
 * One end of a play: an entity, followed as it moves, or a fixed place.
 * <p>
 * An entity is read from whichever thread runs the play. On Folia that may be another region's thread; Paper reads an
 * entity's position there without a check, as vanilla does when it sends particles, so the value is at most a tick
 * old. The eyes of a body made of parts are the middle of its head; an Ender Dragon's are its mouth.
 */
public final class End {
    private static final double ABOVE = 0.3;
    private static final double MOUTH_LIFT = 0.5;
    private static final double MOUTH_AHEAD = 1;
    private final @Nullable Entity entity;
    private final Location fixed;

    private End(@Nullable Entity entity, Location fixed) {
        this.entity = entity;
        this.fixed = fixed;
    }

    /**
     * @param where an {@link Entity} or a {@link Location}
     * @return the end, or {@code null} for anything else, such as a location without a world
     */
    public static @Nullable End of(@Nullable Object where) {
        if (where instanceof Entity entity) return new End(entity, entity.getLocation());
        if (where instanceof Location location && location.getWorld() != null) return new End(null, location.clone());
        return null;
    }

    /**
     * @return the entity, or {@code null} for a fixed place
     */
    public @Nullable Entity entity() {
        return entity;
    }

    /**
     * @return the player this end is, if it is one
     */
    public @Nullable Player player() {
        return entity instanceof Player player ? player : null;
    }

    /**
     * @return whether the entity is gone; a fixed place never is
     */
    public boolean gone() {
        return entity != null && !entity.isValid();
    }

    /**
     * @return where the end is now; a gone entity stays where it was last
     */
    public Location location() {
        return entity == null || !entity.isValid() ? fixed : entity.getLocation();
    }

    /**
     * @return the world of the end
     */
    public World world() {
        return location().getWorld();
    }

    /**
     * @param at a point on the body
     * @return where that point is in the world
     */
    public Vec3 point(Anchor.BodyPoint at) {
        Location location = location();
        Vec3 feet = new Vec3(location.getX(), location.getY(), location.getZ());
        if (entity == null || !entity.isValid()) return feet;
        if (at == Anchor.BodyPoint.EYES
                && entity instanceof ComplexLivingEntity complex
                && !complex.getParts().isEmpty()) {
            Vec3 head = head(complex.getParts());
            return entity instanceof EnderDragon ? mouth(head, location.getDirection()) : head;
        }
        double height = entity.getHeight();
        double lift =
                switch (at) {
                    case FEET -> 0;
                    case CENTER -> height / 2;
                    case EYES -> entity instanceof LivingEntity living ? living.getEyeHeight() : height * 0.85;
                    case ABOVE -> height + ABOVE;
                };
        return feet.plus(new Vec3(0, lift, 0));
    }

    /**
     * The head of a body made of parts, such as an Ender Dragon's, whose own eye height lies at the top of its large
     * box: its smallest part.
     *
     * @param parts the body's parts
     * @return the middle of the smallest part
     */
    static Vec3 head(Collection<? extends Entity> parts) {
        Entity head = parts.stream()
                .min(Comparator.comparingDouble(part -> part.getWidth() * part.getHeight()))
                .orElseThrow();
        Location at = head.getLocation();
        return new Vec3(at.getX(), at.getY() + head.getHeight() / 2, at.getZ());
    }

    /**
     * An Ender Dragon's mouth, where its own fireballs leave: half a block above the middle of its head and a block
     * ahead of it. The dragon's view points backwards, so ahead is against it.
     *
     * @param head the middle of its head
     * @param view the way its view points
     * @return the mouth
     */
    static Vec3 mouth(Vec3 head, Vector view) {
        return head.plus(new Vec3(-view.getX() * MOUTH_AHEAD, MOUTH_LIFT, -view.getZ() * MOUTH_AHEAD));
    }

    /**
     * @return the way the body faces, level with the ground
     */
    public Vec3 body() {
        float yaw = entity instanceof LivingEntity living
                ? living.getBodyYaw()
                : location().getYaw();
        return Vec3.facing(yaw, 0);
    }

    /**
     * @param facing how a shape or piece at this end is turned
     * @param from   where it is
     * @param other  where the other end is, or {@code null} without one
     * @return the way it faces; along the path falls back to the body without another end
     */
    public Vec3 forward(Facing facing, Vec3 from, @Nullable Vec3 other) {
        return switch (facing) {
            case UP, BODY -> body();
            case LOOK -> look();
            case PATH -> other == null ? body() : other.minus(from).normalized(body());
        };
    }

    /**
     * @return the way the entity looks, up and down included
     */
    public Vec3 look() {
        Location location = location();
        return Vec3.facing(location.getYaw(), location.getPitch());
    }
}
