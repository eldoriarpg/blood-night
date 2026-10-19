/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.editor;

import de.eldoria.bloodnight.editor.payload.EditorPayload;
import de.eldoria.bloodnight.particles.BuiltInEffects;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

/**
 * Plays an effect as the open editor session holds it, before anything is applied: at a spot a few blocks in front of
 * the player, aimed at the player, and seen by them alone.
 */
final class EffectPreview {
    private static final double AHEAD = 4;

    private EffectPreview() {}

    /**
     * @param payload the session's payload
     * @param id      the effect's id
     * @return the session's version of the effect, a built-in one, or {@code null} when neither has the id
     */
    static @Nullable ParticleEffect effect(JsonNode payload, String id) {
        JsonNode own = payload.path("particleEffects").path(id);
        if (own.isObject()) return EditorPayload.MAPPER.treeToValue(own, ParticleEffect.class);
        return BuiltInEffects.all().get(id);
    }

    /**
     * @return where the previewed effect plays: on the ground a few blocks in front of the player
     */
    static Location spot(Player player) {
        Location eye = player.getLocation();
        Vector ahead = eye.getDirection().setY(0);
        if (ahead.lengthSquared() < 1e-6) ahead = new Vector(0, 0, 1);
        return eye.clone().add(ahead.normalize().multiply(AHEAD));
    }
}
