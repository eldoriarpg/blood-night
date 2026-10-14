/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.bloodnight.api.MobContext;
import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.reflect.Documentation;
import de.eldoria.graph.reflect.Names;
import de.eldoria.graph.reflect.Param;
import de.eldoria.graph.reflect.ReflectedEntry;
import de.eldoria.graph.reflect.ReflectedType;
import de.eldoria.graph.reflect.ReflectionRules;
import de.eldoria.graph.std.StandardNodes;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import tools.jackson.databind.node.StringNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the node platform of blood-night: standard nodes, Bukkit nodes, nodes reflected from Bukkit types, and
 * one entry point per trigger.
 * <p>
 * Event classes contribute setters only, since entries already expose their getters. {@code set_cancelled} exists
 * once, on {@link Cancellable}, instead of once per event. The read and change nodes of every reflected type show as
 * one "Mutate" node in the editor ({@link ReflectedType#mutate()}), turned into the variant of whatever is wired in.
 * Equipment is changed by slot through one node with a slot input instead of one setter per slot.
 */
public final class BloodNightPlatform {
    private BloodNightPlatform() {}

    /**
     * @param classpath jars and directories generated code compiles against
     * @return the platform
     */
    public static GraphPlatform create(List<Path> classpath) {
        return create(classpath, Documentation.NONE);
    }

    /**
     * @param classpath     jars and directories generated code compiles against
     * @param documentation descriptions for reflected nodes, e.g. from the API's javadoc
     * @return the platform
     */
    public static GraphPlatform create(List<Path> classpath, Documentation documentation) {
        return GraphPlatform.builder(CompiledBehaviour.class)
                .context(MobContext.class)
                .clock("onTick")
                .types(BukkitTypes.mapping())
                .binding(MobBindings.SELF, MobBindings.self())
                .binding(MobBindings.EXTENSION, MobBindings.extension())
                .define(nodes(documentation))
                .rule(BloodNightNodes.WORLD_SPAWN_PARTICLE, ParticleData.RULE)
                .classpath(classpath)
                .build();
    }

    /**
     * @return every node definition of the platform, without descriptions for reflected nodes
     */
    public static List<NodeDefinition> nodes() {
        return nodes(Documentation.NONE);
    }

    /**
     * @param documentation descriptions for reflected nodes
     * @return every node definition of the platform
     */
    public static List<NodeDefinition> nodes(Documentation documentation) {
        ReflectionRules rules = BukkitRules.rules(documentation);
        List<NodeDefinition> nodes = new ArrayList<>(StandardNodes.all());
        nodes.addAll(BukkitNodes.all());
        nodes.add(ReflectedType.mutate());
        nodes.addAll(StandardNodes.typed(BukkitTypes.mapping()));
        nodes.addAll(ReflectedType.of(Entity.class, "entity")
                .action("remove", "remove")
                .title("remove", "Remove Entity")
                .build(rules));
        nodes.addAll(ReflectedType.of(LivingEntity.class, "living_entity")
                .name("Living Entity")
                .action("remove_potion_effect", "removePotionEffect", Param.of("type", PotionEffectType.class))
                .query("attribute", "getAttribute", Param.of("attribute", Attribute.class))
                .title("attribute", "Attribute Of Entity")
                .build(rules));
        nodes.addAll(ReflectedType.of(Player.class, "player")
                .action(
                        "play_sound",
                        "playSound",
                        Param.of("location", Location.class),
                        Param.of("sound", Sound.class),
                        Param.of("volume", float.class),
                        Param.of("pitch", float.class))
                .build(rules));
        nodes.addAll(ReflectedType.of(Mob.class, "mob").category("entity").build(rules));
        nodes.addAll(ReflectedType.of(Creeper.class, "creeper")
                .category("entity")
                .action("explode", "explode")
                .action("ignite", "ignite")
                .build(rules));
        nodes.addAll(ReflectedType.of(EntityEquipment.class, "equipment")
                .category("entity")
                .name("Equipment")
                .setters(false)
                .action("equip", "setItem", Param.of("slot", EquipmentSlot.class), Param.of("item", ItemStack.class))
                .action(
                        "set_slot_drop_chance",
                        "setDropChance",
                        Param.of("slot", EquipmentSlot.class),
                        Param.of("chance", float.class))
                .query("item_in_slot", "getItem", Param.of("slot", EquipmentSlot.class))
                .build(rules));
        nodes.addAll(ReflectedType.of(ItemType.class, "item_type")
                .category("item")
                .name("Item Type")
                .query("create_item_stack", "createItemStack")
                .build(rules));
        nodes.addAll(ReflectedType.of(AttributeInstance.class, "attribute")
                .category("entity")
                .name("Attribute")
                .build(rules));
        nodes.addAll(ReflectedType.of(Particle.DustOptions.class, "dust")
                .category("world")
                .name("Dust")
                .constructor(
                        "create",
                        Param.of("color", Color.class, "The colour of the dust"),
                        Param.of("size", float.class, "How big each particle is, 1 for normal"))
                .build(rules));
        nodes.addAll(ReflectedType.of(Particle.DustTransition.class, "dust_transition")
                .category("world")
                .name("Dust Transition")
                .constructor(
                        "create",
                        Param.of("from", Color.class, "The colour it starts with"),
                        Param.of("to", Color.class, "The colour it fades to"),
                        Param.of("size", float.class, "How big each particle is, 1 for normal"))
                .build(rules));
        nodes.addAll(ReflectedType.of(Particle.Spell.class, "spell")
                .category("world")
                .name("Spell")
                .constructor(
                        "create",
                        Param.of("color", Color.class, "The colour of the spell"),
                        Param.of("power", float.class, "How strongly it shows"))
                .build(rules));
        nodes.addAll(ReflectedType.of(Particle.Trail.class, "trail")
                .category("world")
                .name("Trail")
                .constructor(
                        "create",
                        Param.of("target", Location.class, "Where the trail leads"),
                        Param.of("color", Color.class, "The colour of the trail"),
                        Param.of("duration", int.class, "How long it travels, in ticks"))
                .build(rules));
        nodes.addAll(ReflectedType.of(BlockType.class, "block_type")
                .category("world")
                .name("Block Type")
                .getter(false)
                .setters(false)
                .query("create_block_data", "createBlockData")
                .build(rules));
        nodes.addAll(ReflectedType.of(Color.class, "color")
                .category("value")
                .factory(
                        "rgb",
                        "fromRGB",
                        Param.of("red", int.class),
                        Param.of("green", int.class),
                        Param.of("blue", int.class))
                .build(rules));

        nodes.addAll(ReflectedType.of(Projectile.class, "projectile").build(rules));
        nodes.addAll(ReflectedType.of(Location.class, "location")
                .query("distance", "distance", Param.of("other", Location.class))
                .title("distance", "Distance Between Locations")
                .build(rules));
        nodes.addAll(ReflectedType.of(Vector.class, "vector")
                .constructor(
                        "create", Param.of("x", double.class), Param.of("y", double.class), Param.of("z", double.class))
                .build(rules));
        nodes.addAll(
                ReflectedType.of(World.class, "world").guard(FoliaGuards.WORLD).build(rules));
        nodes.addAll(ReflectedType.of(ItemStack.class, "item").name("Item").build(rules));
        nodes.addAll(
                ReflectedType.of(Block.class, "block").guard(FoliaGuards.BLOCK).build(rules));
        nodes.addAll(ReflectedType.of(Cancellable.class, "cancellable")
                .name("Event")
                .category("event")
                .build(rules));
        for (Class<?> event : List.of(
                EntityDamageEvent.class,
                EntityDeathEvent.class,
                EntityTeleportEvent.class,
                EntityTargetEvent.class,
                ExplosionPrimeEvent.class,
                EntityExplodeEvent.class)) {
            String name = event.getSimpleName().replace("Event", "");
            nodes.addAll(ReflectedType.of(event, "event_data." + Names.snakeCase(name))
                    .name(name)
                    .category("event")
                    .getter(false)
                    .exclude("cancelled")
                    .build(rules));
        }
        for (Trigger<?> trigger : Triggers.EVENTS) {
            ReflectedEntry.Builder entry = ReflectedEntry.of(CompiledBehaviour.class, trigger.method(), trigger.id())
                    .parameterNames("event")
                    .name(trigger.name())
                    .description(trigger.description())
                    .category("event");
            if (trigger.id().equals(BloodNightNodes.EVENT_KILL)) {
                entry.config(KillVictim.FIELD, KillVictim.class, StringNode.valueOf(KillVictim.ANY.name()))
                        .guard(KillVictim::guard);
            }
            if (Triggers.WITH_ATTACKER.contains(trigger.id())) {
                entry.computed(
                        "attacker",
                        "Attacker",
                        PortType.of(Entity.class),
                        "The entity behind it: a player who shot an arrow counts, not the arrow; missing when none",
                        CodeBlock.of("$T.attacker(event)", BukkitRuntime.class));
            }
            nodes.add(entry.build(rules));
        }
        nodes.add(ReflectedEntry.of(CompiledBehaviour.class, "onSpawn", BloodNightNodes.MOB_SPAWN)
                .name("On Spawn")
                .description("Runs once when the mob has spawned, after its equipment and extension are set up")
                .build(rules));
        nodes.add(ReflectedEntry.of(CompiledBehaviour.class, "onNightEnd", BloodNightNodes.MOB_NIGHT_END)
                .name("On Night End")
                .description("Runs once when the night ends in the mob's world")
                .build(rules));
        nodes.add(ReflectedEntry.of(CompiledBehaviour.class, "onTick", BloodNightNodes.MOB_TICK)
                .name("On Tick")
                .description("Runs every server tick")
                .build(rules));
        nodes.add(ReflectedEntry.of(CompiledBehaviour.class, "onDebug", "debug.trigger")
                .name("Debug Trigger")
                .description("Runs when fired by command")
                .build(rules));
        return nodes;
    }
}
