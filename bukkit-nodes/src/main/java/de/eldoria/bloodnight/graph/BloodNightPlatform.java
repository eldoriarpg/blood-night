package de.eldoria.bloodnight.graph;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.bloodnight.api.MobContext;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.reflect.Names;
import de.eldoria.graph.reflect.ReflectedEntry;
import de.eldoria.graph.reflect.ReflectedType;
import de.eldoria.graph.reflect.ReflectionRules;
import de.eldoria.graph.std.StandardNodes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the node platform of blood-night: standard nodes, Bukkit nodes, nodes reflected from Bukkit types, and
 * one entry point per trigger.
 * <p>
 * Event classes contribute setters only, since entries already expose their getters. {@code set_cancelled} exists
 * once, on {@link Cancellable}, instead of once per event.
 */
public final class BloodNightPlatform {
    private BloodNightPlatform() {
    }

    /**
     * @param classpath jars and directories generated code compiles against
     * @return the platform
     */
    public static GraphPlatform create(List<Path> classpath) {
        return GraphPlatform.builder(CompiledBehaviour.class)
                .context(MobContext.class)
                .types(BukkitTypes.mapping())
                .define(nodes())
                .classpath(classpath)
                .build();
    }

    /**
     * @return every node definition of the platform
     */
    public static List<NodeDefinition> nodes() {
        ReflectionRules rules = BukkitRules.rules();
        List<NodeDefinition> nodes = new ArrayList<>(StandardNodes.all());
        nodes.addAll(BukkitNodes.all());
        nodes.addAll(ReflectedType.of(Entity.class, "entity").build(rules));
        nodes.addAll(ReflectedType.of(LivingEntity.class, "living_entity").name("Living Entity").build(rules));
        nodes.addAll(ReflectedType.of(Player.class, "player").build(rules));
        nodes.addAll(ReflectedType.of(Projectile.class, "projectile").build(rules));
        nodes.addAll(ReflectedType.of(Location.class, "location").build(rules));
        nodes.addAll(ReflectedType.of(Vector.class, "vector").build(rules));
        nodes.addAll(ReflectedType.of(World.class, "world").build(rules));
        nodes.addAll(ReflectedType.of(ItemStack.class, "item").name("Item").build(rules));
        nodes.addAll(ReflectedType.of(Block.class, "block").build(rules));
        nodes.addAll(ReflectedType.of(Cancellable.class, "cancellable").name("Event").category("event").build(rules));
        for (Class<?> event : List.of(EntityDamageEvent.class, EntityDeathEvent.class, EntityTeleportEvent.class, EntityTargetEvent.class,
                ExplosionPrimeEvent.class, EntityExplodeEvent.class)) {
            String name = event.getSimpleName().replace("Event", "");
            nodes.addAll(ReflectedType.of(event, "event_data." + Names.snakeCase(name))
                    .name(name)
                    .category("event")
                    .getter(false)
                    .exclude("cancelled")
                    .build(rules));
        }
        for (Trigger<?> trigger : Triggers.EVENTS) {
            nodes.add(ReflectedEntry.of(CompiledBehaviour.class, trigger.method(), trigger.id())
                    .parameterNames("event")
                    .name(trigger.name())
                    .description(trigger.description())
                    .category("event")
                    .build(rules));
        }
        nodes.add(ReflectedEntry.of(CompiledBehaviour.class, "onTick", "mob.tick")
                .name("On Tick").description("Runs every server tick").build(rules));
        nodes.add(ReflectedEntry.of(CompiledBehaviour.class, "onDebug", "debug.trigger")
                .name("Debug Trigger").description("Runs when fired by command").build(rules));
        return nodes;
    }
}
