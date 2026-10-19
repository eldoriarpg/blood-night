/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.piece;

import de.eldoria.bloodnight.graph.runtime.Displays;
import de.eldoria.bloodnight.particles.data.ParticleDatas;
import de.eldoria.bloodnight.particles.effect.Anchor;
import de.eldoria.bloodnight.particles.effect.Motion;
import de.eldoria.bloodnight.particles.effect.Piece;
import de.eldoria.bloodnight.particles.geometry.Frame;
import de.eldoria.bloodnight.particles.geometry.Vec3;
import de.eldoria.bloodnight.particles.play.End;
import de.eldoria.bloodnight.particles.play.Play;
import de.eldoria.bloodnight.particles.play.Stage;
import de.eldoria.bloodnight.tasks.Tasks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * One display piece of a running play: spawned on its source's region, moved and turned every tick by its own entity
 * scheduler, then shrunk and taken away. Like warnings, it is tagged and never saved. Players who switched effects
 * off do not see it; with a viewer, only the viewer does.
 * <p>
 * A piece with motion flies to the other end and on past it, stays there for its {@code stick} ticks, then goes; one
 * without stays at its anchor for the play's duration. A span stretches from its end to the other one the whole time.
 */
public final class PieceRun {
    private static final int FADE = 5;
    private static final int FLIGHT_SMOOTHING = 2;
    private static final Display.Brightness BRIGHT = new Display.Brightness(15, 15);
    private final Piece piece;
    private final int index;
    private final Play play;
    private final Stage stage;
    private final Plugin plugin;
    private final Runnable gone;
    private final End own;
    private final @Nullable End other;
    private long tick;
    private long end;
    private @Nullable Transformation shown;

    private PieceRun(Piece piece, int index, Play play, Stage stage, Plugin plugin, Runnable gone) {
        this.piece = piece;
        this.index = index;
        this.play = play;
        this.stage = stage;
        this.plugin = plugin;
        this.gone = gone;
        boolean onTarget = piece.anchor().on() == Anchor.EffectEnd.TARGET;
        End target = play.target();
        this.own = onTarget && target != null ? target : play.source();
        this.other = own == play.source() ? target : play.source();
    }

    /**
     * Starts every piece of a play.
     *
     * @param owner the mob whose display slots the pieces take, or {@code null} for no limit
     * @return whether they started; false when the mob has no display slot left
     */
    public static boolean start(Play play, Stage stage, Plugin plugin, @Nullable UUID owner) {
        List<Piece> pieces = play.effect().pieces();
        if (pieces.isEmpty()) return true;
        Runnable gone = owner == null ? PieceRun::nothing : Displays.reserve(owner, pieces.size());
        if (gone == null) return false;
        for (int index = 0; index < pieces.size(); index++) {
            new PieceRun(pieces.get(index), index, play, stage, plugin, gone).spawn();
        }
        return true;
    }

    private static void nothing() {}

    /**
     * @return ticks until a piece with motion reaches the other end, 0 without motion or another end
     */
    public static long arrival(Piece piece) {
        Motion motion = piece.motion();
        return motion == null ? 0 : motion.ticks();
    }

    private void spawn() {
        Vec3 start = position();
        World world = own.world();
        Location location = new Location(world, start.x(), start.y(), start.z());
        Vec3 far = farPoint();
        Motion motion = piece.motion();
        end = motion != null && far != null
                ? motion.length(far.minus(start).length()) + piece.stick()
                : Math.max(1, play.effect().duration());
        stage.tasks().at(location, () -> {
            Display display = world.spawn(location, type(), spawned -> {
                spawned.setPersistent(false);
                spawned.setBrightness(BRIGHT);
                spawned.getPersistentDataContainer().set(Displays.DISPLAY, PersistentDataType.BOOLEAN, true);
                if (play.viewer() != null) spawned.setVisibleByDefault(false);
                spawned.setTeleportDuration(FLIGHT_SMOOTHING);
                dress(spawned);
                shown = transformation(start, far);
                spawned.setTransformation(shown);
            });
            show(display);
            stage.tasks().entityLoop(display, () -> step(display), this::finish);
        });
    }

    /**
     * Shows the display only to the play's viewer, if it has one; otherwise hides it from the players who switched
     * effects off and from those the effect skips.
     */
    private void show(Display display) {
        Player viewer = play.viewer();
        if (viewer != null) {
            stage.tasks().entityNow(viewer, () -> viewer.showEntity(plugin, display));
            return;
        }
        List<Player> hidden = new ArrayList<>(play.skip());
        for (UUID id : stage.viewers().off()) {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) hidden.add(player);
        }
        for (Player player : hidden) stage.tasks().entityNow(player, () -> player.hideEntity(plugin, display));
    }

    private boolean step(Display display) {
        tick++;
        if (tick >= end) {
            fade(display);
            return false;
        }
        Vec3 now = position();
        Vec3 far = farPoint();
        play.piece(index, now);
        Location location = display.getLocation();
        if (Math.abs(location.getX() - now.x())
                        + Math.abs(location.getY() - now.y())
                        + Math.abs(location.getZ() - now.z())
                > 0.01) {
            move(display, new Location(location.getWorld(), now.x(), now.y(), now.z()));
        }
        shown = transformation(now, far);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(1);
        display.setTransformation(shown);
        return true;
    }

    /**
     * Moves the display; Folia only moves entities asynchronously, Paper moves it right away.
     */
    private static void move(Display display, Location to) {
        if (Tasks.regionised()) display.teleportAsync(to);
        else display.teleport(to);
    }

    private void fade(Display display) {
        play.piece(index, null);
        if (shown != null) {
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(FADE);
            display.setTransformation(PieceShapes.shrunk(shown));
        }
        stage.tasks()
                .entityLater(
                        display,
                        FADE,
                        () -> {
                            display.remove();
                            gone.run();
                        },
                        gone);
    }

    private void finish() {
        play.piece(index, null);
        gone.run();
    }

    private Vec3 position() {
        Vec3 anchor = own.point(piece.anchor().at());
        Vec3 far = farPoint();
        Frame frame = Frame.of(anchor, own.forward(piece.facing(), anchor, far));
        Vec3 start = frame.world(piece.offset());
        Motion motion = piece.motion();
        if (motion == null || far == null || piece.kind() == Piece.PieceKind.SPAN) return start;
        return motion.position(start, far, tick);
    }

    private @Nullable Vec3 farPoint() {
        return other == null ? null : other.point(piece.anchor().at());
    }

    private Transformation transformation(Vec3 at, @Nullable Vec3 far) {
        Frame frame = Frame.of(at, own.forward(piece.facing(), at, far));
        if (piece.kind() != Piece.PieceKind.SPAN) {
            return PieceShapes.centred(
                    PieceShapes.turn(frame, piece.rotation()),
                    piece.scale().floatValue(),
                    piece.kind() == Piece.PieceKind.BLOCK);
        }
        double length = far == null ? 0 : far.minus(at).length();
        double reach = piece.grow() == 0 ? 1 : Math.min(1, (double) tick / piece.grow());
        Frame along = far == null ? frame : Frame.of(at, far.minus(at));
        return PieceShapes.span(
                PieceShapes.turn(along, Piece.Rotation.NONE), piece.width().floatValue(), (float) (length * reach));
    }

    private Class<? extends Display> type() {
        return piece.kind() == Piece.PieceKind.ITEM ? ItemDisplay.class : BlockDisplay.class;
    }

    private void dress(Display display) {
        if (display instanceof ItemDisplay item) item.setItemStack(item());
        if (display instanceof BlockDisplay block) block.setBlock(block().createBlockData());
        if (piece.glow()) {
            display.setGlowing(true);
            if (piece.color() != null) display.setGlowColorOverride(ParticleDatas.color(piece.color()));
        }
    }

    private ItemStack item() {
        String id = piece.item();
        if (id == null) return new ItemStack(Material.STONE);
        ItemStack known = stage.items().apply(id);
        if (known != null) return known;
        Material material = Material.matchMaterial(id.toLowerCase(Locale.ROOT));
        return new ItemStack(material == null || !material.isItem() ? Material.STONE : material);
    }

    private Material block() {
        String key = piece.block();
        Material material = key == null ? null : Material.matchMaterial(key.toLowerCase(Locale.ROOT));
        return material == null || !material.isBlock() ? Material.STONE : material;
    }
}
