/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.graph.std.StdNodes;

/**
 * Type ids of the BloodNight nodes, for building graphs in code without spelling ids by hand.
 * <p>
 * Holds the entries, triggers, hand-written nodes, declared methods and the getters and setters in common use. The
 * reflected setters of every type are not all listed; their ids follow {@code <type>.set_<property>}. Standard nodes
 * are inherited from {@link StdNodes}.
 */
public interface BloodNightNodes extends StdNodes {
    String MOB_TICK = "mob.tick";
    String MOB_SPAWN = "mob.spawn";
    String MOB_NIGHT_END = "mob.night_end";

    String EVENT_DAMAGE = "event.damage";
    String EVENT_DAMAGE_BY_ENTITY = "event.damage_by_entity";
    String EVENT_DAMAGE_BY_PLAYER = "event.damage_by_player";
    String EVENT_HIT = "event.hit";
    String EVENT_KILL = "event.kill";
    String EVENT_DEATH = "event.death";
    String EVENT_TELEPORT = "event.teleport";
    String EVENT_TARGET_CHANGE = "event.target_change";
    String EVENT_PROJECTILE_SHOOT = "event.projectile_shoot";
    String EVENT_PROJECTILE_HIT = "event.projectile_hit";
    String EVENT_EXPLOSION_PRIME = "event.explosion_prime";
    String EVENT_EXPLODE = "event.explode";
    String EVENT_EXTENSION_DAMAGE = "event.extension_damage";
    String EVENT_EXTENSION_DEATH = "event.extension_death";

    String MOB_SELF = "mob.self";
    String MOB_EXTENSION = "mob.extension";

    String VALUE_ITEM = "value.item";

    String LOCATION_CREATE = "location.create";
    String VECTOR_NORMALIZE = "vector.normalize";
    String VECTOR_DIRECTION = "vector.direction";
    String VECTOR_DISTANCE = "vector.distance";
    String VECTOR_DISTANCE_SQUARED = "vector.distance_squared";
    String WORLD_NEARBY_ENTITIES = "world.nearby_entities";
    String LIVING_ENTITY_APPLY_EFFECT = "living_entity.apply_effect";
    String LIVING_ENTITY_LAUNCH_PROJECTILE = "living_entity.launch_projectile";
    String WORLD_SPAWN_POTION_CLOUD = "world.spawn_potion_cloud";
    String WORLD_NEAREST_PLAYER = "world.nearest_player";

    String ENTITY_READ = "entity.read";
    String LIVING_ENTITY_READ = "living_entity.read";
    String LIVING_ENTITY_HAS_LINE_OF_SIGHT = "living_entity.has_line_of_sight";
    String MOB_READ = "mob.read";
    String CREEPER_READ = "creeper.read";
    String PLAYER_READ = "player.read";
    String PROJECTILE_READ = "projectile.read";
    String LOCATION_READ = "location.read";
    String VECTOR_READ = "vector.read";
    String WORLD_READ = "world.read";
    String BLOCK_READ = "block.read";
    String ITEM_READ = "item.read";
    String ITEM_TYPE_READ = "item_type.read";
    String EQUIPMENT_READ = "equipment.read";
    String EQUIPMENT_EQUIP = "equipment.equip";
    String EQUIPMENT_SET_SLOT_DROP_CHANCE = "equipment.set_slot_drop_chance";
    String EQUIPMENT_ITEM_IN_SLOT = "equipment.item_in_slot";
    String ATTRIBUTE_READ = "attribute.read";
    String COLOR_READ = "color.read";
    String DUST_READ = "dust.read";
    String CANCELLABLE_READ = "cancellable.read";

    String ENTITY_REMOVE = "entity.remove";
    String ENTITY_TELEPORT = "entity.teleport";
    String ENTITY_NEARBY_ENTITIES = "entity.nearby_entities";
    String LIVING_ENTITY_REMOVE_POTION_EFFECT = "living_entity.remove_potion_effect";
    String LIVING_ENTITY_DAMAGE = "living_entity.damage";
    String LIVING_ENTITY_ATTRIBUTE = "living_entity.attribute";
    String PLAYER_PLAY_SOUND = "player.play_sound";
    String CREEPER_EXPLODE = "creeper.explode";
    String CREEPER_IGNITE = "creeper.ignite";
    String ITEM_TYPE_CREATE_ITEM_STACK = "item_type.create_item_stack";
    String WORLD_BLOCK_AT = "world.block_at";
    String WORLD_SPAWN_PARTICLE = "world.spawn_particle";
    String WORLD_STRIKE_LIGHTNING = "world.strike_lightning";
    String SERVER_RUN_COMMAND = "server.run_command";
    String WORLD_SPAWN_TNT = "world.spawn_tnt";
    String WORLD_SHOW_MARKER = "world.show_marker";
    String WORLD_SHOW_TEXT = "world.show_text";
    String ENTITY_SHOW_ABOVE = "entity.show_above";
    String MOB_MOVE_TO = "mob.move_to";
    String MOB_STOP_MOVING = "mob.stop_moving";
    String MOB_HAS_PATH = "mob.has_path";
    String MOB_MOVEMENT = "mob.movement";
    String SERVER_BROADCAST = "server.broadcast";
    String PLAYER_RUN_COMMAND = "player.run_command";
    String PLACEHOLDER_PARSE = "placeholder.parse";
    String ECONOMY_DEPOSIT = "economy.deposit";
    String ECONOMY_WITHDRAW = "economy.withdraw";
    String ECONOMY_BALANCE = "economy.balance";
    String MYTHIC_CAST_SKILL = "mythic.cast_skill";
    String MYTHIC_SPAWN = "mythic.spawn";
    String WORLD_LINE_TRACE = "world.line_trace";
    String LOCATION_DISTANCE = "location.distance";
    String VECTOR_CREATE = "vector.create";
    String DUST_CREATE = "dust.create";
    String DUST_TRANSITION_CREATE = "dust_transition.create";
    String SPELL_CREATE = "spell.create";
    String TRAIL_CREATE = "trail.create";
    String VIBRATION_CREATE = "vibration.create";
    String PARTICLE_POWER = "particle.power";
    String PARTICLE_DELAY = "particle.delay";
    String BLOCK_TYPE_CREATE_BLOCK_DATA = "block_type.create_block_data";
    String COLOR_RGB = "color.rgb";

    String ENTITY_MUTATE = "entity.mutate";
    String LIVING_ENTITY_MUTATE = "living_entity.mutate";
    String MOB_MUTATE = "mob.mutate";
    String CREEPER_MUTATE = "creeper.mutate";
    String CANCELLABLE_MUTATE = "cancellable.mutate";
    String ATTRIBUTE_MUTATE = "attribute.mutate";
    String EVENT_DATA_EXPLOSION_PRIME_MUTATE = "event_data.explosion_prime.mutate";
    String EVENT_DATA_ENTITY_DAMAGE_MUTATE = "event_data.entity_damage.mutate";
}
