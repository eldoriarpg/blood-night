/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.data;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The kinds of data particles take, by Paper's data class, with the fields of {@link ParticleDataSpec} each uses.
 * Vibrations and geysers need a destination block or water and are not offered for effects.
 */
public enum DataKind {
    /** Takes no data. */
    NONE(Void.class, List.of()),
    /** Dust: a colour and a size. */
    DUST(Particle.DustOptions.class, List.of("color", "size")),
    /** Dust changing from one colour to another. */
    DUST_TRANSITION(Particle.DustTransition.class, List.of("color", "to", "size")),
    /** Spell swirls: a colour and a strength. */
    SPELL(Particle.Spell.class, List.of("color", "power")),
    /** Only a colour. */
    COLOR(Color.class, List.of("color")),
    /** Only a strength. */
    POWER(Float.class, List.of("power")),
    /** Only a delay in ticks. */
    DELAY(Integer.class, List.of("delay")),
    /** A block. */
    BLOCK(BlockData.class, List.of("block")),
    /** An item. */
    ITEM(ItemStack.class, List.of("item")),
    /** A trail flying somewhere: colour, ticks and where to. */
    TRAIL(Particle.Trail.class, List.of("color", "ticks", "toward")),
    /** Data effects do not offer, such as vibrations and geysers. */
    UNSUPPORTED(Object.class, List.of());

    private final Class<?> type;
    private final List<String> fields;

    DataKind(Class<?> type, List<String> fields) {
        this.type = type;
        this.fields = fields;
    }

    /**
     * @return the fields of {@link ParticleDataSpec} this kind uses
     */
    public List<String> fields() {
        return fields;
    }

    /**
     * @param particle a particle
     * @return the kind of data it takes
     */
    public static DataKind of(Particle particle) {
        return of(particle.getDataType());
    }

    /**
     * @param type Paper's data class of a particle
     * @return the kind of data it stands for
     */
    public static DataKind of(Class<?> type) {
        if (type == Particle.DustTransition.class) return DUST_TRANSITION;
        for (DataKind kind : values()) {
            if (kind != UNSUPPORTED && kind.type == type) return kind;
        }
        return UNSUPPORTED;
    }
}
