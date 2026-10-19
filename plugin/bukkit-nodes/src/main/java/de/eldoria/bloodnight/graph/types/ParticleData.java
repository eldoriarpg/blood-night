/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.types;

import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.platform.NodeRule;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Optional;

/**
 * Checks that a particle typed into Spawn Particle gets the data it needs: none when the particle takes none, data of
 * the particle's own data type otherwise. Without this, a mismatch only shows on the server, when the particle is
 * spawned.
 * <p>
 * Particles are looked up in the running server's registry; without a server nothing is checked. A particle that is
 * connected rather than typed in is only known at runtime and is not checked either.
 */
public final class ParticleData {
    private static final String PARTICLE = "particle";
    private static final String DATA = "data";
    private static final Map<Class<?>, String> SOURCES = Map.ofEntries(
            Map.entry(Float.class, "Particle Power"),
            Map.entry(Integer.class, "Particle Delay"),
            Map.entry(Particle.DustOptions.class, "Dust"),
            Map.entry(Particle.DustTransition.class, "Dust Transition"),
            Map.entry(Particle.Spell.class, "Spell"),
            Map.entry(Particle.Trail.class, "Trail"),
            Map.entry(Vibration.class, "Vibration"),
            Map.entry(BlockData.class, "Block Type: Create Block Data"),
            Map.entry(ItemStack.class, "an item"),
            Map.entry(Color.class, "Color"),
            Map.entry(Particle.Geyser.class, "Geyser data, which no node makes yet"),
            Map.entry(Particle.GeyserBase.class, "Geyser base data, which no node makes yet"));

    public static final NodeRule RULE = (node, report) -> particle(node).ifPresent(particle -> {
        Class<?> needed = particle.getDataType();
        boolean given = node.literal(DATA).isPresent() || node.connected(DATA).isPresent();
        if (needed == Void.class) {
            if (given) report.error(DATA, "%s takes no data: leave Data empty".formatted(particle.key()));
            return;
        }
        if (!given) {
            report.error(
                    DATA,
                    "%s needs %s data: connect %s".formatted(particle.key(), simpleName(particle), source(needed)));
            return;
        }
        if (node.literal(DATA).isPresent()) {
            report.error(
                    DATA,
                    "%s needs %s data, which cannot be typed in: connect %s"
                            .formatted(particle.key(), simpleName(particle), source(needed)));
            return;
        }
        node.connected(DATA).ifPresent(type -> {
            if (!fits(type, needed)) {
                report.error(
                        DATA,
                        "%s needs %s data, not %s: connect %s"
                                .formatted(particle.key(), simpleName(particle), type.id(), source(needed)));
            }
        });
    });

    private ParticleData() {}

    private static Optional<Particle> particle(NodeRule.Node node) {
        if (Bukkit.getServer() == null) return Optional.empty();
        return node.literal(PARTICLE)
                .filter(JsonNode::isString)
                .map(value -> NamespacedKey.fromString(value.asString()))
                .map(key -> RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.PARTICLE_TYPE)
                        .get(key));
    }

    private static boolean fits(PortType type, Class<?> needed) {
        if (!(type instanceof PortType.Concrete concrete)) return true;
        Class<?> raw = concrete.raw();
        if (raw == float.class) raw = Float.class;
        if (raw == int.class) raw = Integer.class;
        return needed.isAssignableFrom(raw);
    }

    private static String simpleName(Particle particle) {
        return particle.getDataType().getSimpleName();
    }

    private static String source(Class<?> needed) {
        return SOURCES.getOrDefault(needed, "a " + needed.getSimpleName());
    }
}
