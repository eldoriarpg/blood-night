/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs.movement;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import de.eldoria.bloodnight.api.internal.MovementPreset;
import de.eldoria.bloodnight.mob.meta.Movement;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

/**
 * One mob's movement as a Paper goal. Each tick it looks around (see {@link Course.Sight}), picks targets for the
 * presets that hunt or guard, and runs while its {@link Course} is anything but {@link Course#NONE}. Presets that must
 * keep the mob from attacking while they steer (hit and run, stalker, coward, guard) hold the move flag, which pauses
 * the vanilla melee goals; the others steer beside the vanilla goals, so a skeleton keeping its distance still shoots.
 */
final class MovementGoal implements Goal<Mob> {
    static final GoalKey<Mob> KEY = GoalKey.of(Mob.class, new NamespacedKey("bloodnight", "movement"));
    private static final EnumSet<MovementPreset> HOLDING =
            EnumSet.of(MovementPreset.HIT_AND_RUN, MovementPreset.STALKER, MovementPreset.COWARD, MovementPreset.GUARD);
    private static final int REPATH_TICKS = 10;
    private static final int HEAL_TICKS = 20;
    private static final double RETREAT_STEP = 8;
    private static final double RETREAT_SPEED = 1.25;
    private static final double WATCHED = 0.5;

    private final Mob mob;
    private final Movement movement;
    private final Location home;
    private Course course = Course.NONE;
    private long backOffUntil;
    private boolean fleeing;
    private int ticks;

    MovementGoal(Mob mob, Movement movement) {
        this.mob = mob;
        this.movement = movement;
        this.home = mob.getLocation();
    }

    /**
     * Starts the hit-and-run mob's back off after it hit something.
     */
    void hit() {
        backOffUntil = mob.getWorld().getGameTime() + Math.round(movement.seconds() * 20);
    }

    @Override
    public boolean shouldActivate() {
        return think() != Course.NONE;
    }

    @Override
    public boolean shouldStayActive() {
        return think() != Course.NONE;
    }

    @Override
    public void stop() {
        ticks = 0;
    }

    @Override
    public void tick() {
        if (ticks++ % REPATH_TICKS != 0) return;
        LivingEntity target = mob.getTarget();
        switch (course) {
            case APPROACH -> {
                if (target != null) mob.getPathfinder().moveTo(target);
            }
            case RETREAT -> {
                if (target != null) mob.getPathfinder().moveTo(awayFrom(target), RETREAT_SPEED);
            }
            case HOLD -> mob.getPathfinder().stopPathfinding();
            case HOME -> mob.getPathfinder().moveTo(home);
            case NONE -> {}
        }
    }

    @Override
    public GoalKey<Mob> getKey() {
        return KEY;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return HOLDING.contains(movement.preset()) ? EnumSet.of(GoalType.MOVE) : EnumSet.noneOf(GoalType.class);
    }

    private Course think() {
        choose();
        Course.Sight sight = sight();
        if (movement.preset() == MovementPreset.COWARD) {
            fleeing = mob.getTarget() != null && Course.flees(movement, sight);
            if (fleeing && mob.getTicksLived() % HEAL_TICKS == 0) heal();
        }
        course = Course.of(movement, sight);
        return course;
    }

    private void choose() {
        LivingEntity target = mob.getTarget();
        switch (movement.preset()) {
            case AGGRESSIVE -> {
                if (target == null) mob.setTarget(nearestPlayer());
            }
            case PACK -> {
                if (target == null) mob.setTarget(packTarget());
            }
            case GUARD -> {
                if (target != null && target.getLocation().distance(home) > movement.range() * 2) mob.setTarget(null);
            }
            default -> {}
        }
    }

    private Course.Sight sight() {
        LivingEntity target = mob.getTarget();
        boolean sameWorld = target != null && target.getWorld().equals(mob.getWorld());
        return new Course.Sight(
                sameWorld ? target.getLocation().distance(mob.getLocation()) : Double.NaN,
                share(mob),
                sameWorld ? share(target) : 1,
                sameWorld && watched(target),
                mob.getWorld().getGameTime() < backOffUntil,
                fleeing,
                home.getWorld().equals(mob.getWorld()) ? home.distance(mob.getLocation()) : 0);
    }

    private boolean watched(LivingEntity target) {
        Vector towardsMob =
                mob.getLocation().toVector().subtract(target.getEyeLocation().toVector());
        if (towardsMob.lengthSquared() == 0) return true;
        return target.getEyeLocation().getDirection().dot(towardsMob.normalize()) > WATCHED;
    }

    private Location awayFrom(Entity target) {
        Vector away = mob.getLocation()
                .toVector()
                .subtract(target.getLocation().toVector())
                .setY(0);
        if (away.lengthSquared() == 0) away = new Vector(1, 0, 0);
        return mob.getLocation().add(away.normalize().multiply(RETREAT_STEP));
    }

    private void heal() {
        mob.setHealth(Math.min(maxHealth(mob), mob.getHealth() + 1));
    }

    private @Nullable Player nearestPlayer() {
        Player nearest = null;
        double best = movement.range() * movement.range();
        for (Player player : mob.getWorld().getPlayers()) {
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) continue;
            double distance = player.getLocation().distanceSquared(mob.getLocation());
            if (distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private @Nullable LivingEntity packTarget() {
        Optional<String> kind = MobTags.getCustomMobId(mob);
        if (kind.isEmpty()) return null;
        double range = movement.range();
        for (Entity other : mob.getNearbyEntities(range, range, range)) {
            if (other instanceof Mob member
                    && member.getTarget() != null
                    && kind.equals(MobTags.getCustomMobId(member))) {
                return member.getTarget();
            }
        }
        return null;
    }

    private static double share(LivingEntity entity) {
        double max = maxHealth(entity);
        return max <= 0 ? 1 : entity.getHealth() / max;
    }

    private static double maxHealth(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        return attribute == null ? entity.getHealth() : attribute.getValue();
    }
}
