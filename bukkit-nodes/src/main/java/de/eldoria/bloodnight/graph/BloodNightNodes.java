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
    String EVENT_PLAYER_KILL = "event.player_kill";
    String EVENT_ENTITY_KILL = "event.entity_kill";
    String EVENT_DEATH = "event.death";
    String EVENT_TELEPORT = "event.teleport";
    String EVENT_TARGET_CHANGE = "event.target_change";
    String EVENT_PROJECTILE_SHOOT = "event.projectile_shoot";
    String EVENT_PROJECTILE_HIT = "event.projectile_hit";
    String EVENT_EXPLOSION_PRIME = "event.explosion_prime";
    String EVENT_EXPLODE = "event.explode";
    String EVENT_EXTENSION_DAMAGE = "event.extension_damage";
    String EVENT_EXTENSION_DEATH = "event.extension_death";

    String CAST_ENTITY = "cast.entity";
    String CAST_LIVING_ENTITY = "cast.living_entity";
    String CAST_MOB = "cast.mob";
    String CAST_CREEPER = "cast.creeper";
    String CAST_PLAYER = "cast.player";
    String CAST_LOCATION = "cast.location";
    String CAST_VECTOR = "cast.vector";
    String CAST_WORLD = "cast.world";
    String CAST_ITEM = "cast.item";

    String MOB_SELF = "mob.self";
    String MOB_EXTENSION = "mob.extension";

    String VALUE_ITEM = "value.item";
    String VALUE_DAMAGE_CAUSE = "value.damage_cause";
    String VALUE_ENTITY_TYPE = "value.entity_type";
    String VALUE_ITEM_TYPE = "value.item_type";
    String VALUE_BLOCK_TYPE = "value.block_type";
    String VALUE_POTION_EFFECT_TYPE = "value.potion_effect_type";

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

    String ENTITY_GET = "entity.get";
    String LIVING_ENTITY_GET = "living_entity.get";
    String MOB_GET = "mob.get";
    String CREEPER_GET = "creeper.get";
    String PLAYER_GET = "player.get";
    String PROJECTILE_GET = "projectile.get";
    String LOCATION_GET = "location.get";
    String VECTOR_GET = "vector.get";
    String WORLD_GET = "world.get";
    String BLOCK_GET = "block.get";
    String ITEM_GET = "item.get";
    String ITEM_TYPE_GET = "item_type.get";
    String EQUIPMENT_GET = "equipment.get";
    String ATTRIBUTE_GET = "attribute.get";
    String POTION_EFFECT_GET = "potion_effect.get";
    String COLOR_GET = "color.get";
    String DUST_GET = "dust.get";
    String CANCELLABLE_GET = "cancellable.get";

    String ENTITY_REMOVE = "entity.remove";
    String ENTITY_TELEPORT = "entity.teleport";
    String ENTITY_NEARBY_ENTITIES = "entity.nearby_entities";
    String LIVING_ENTITY_ADD_POTION_EFFECT = "living_entity.add_potion_effect";
    String LIVING_ENTITY_REMOVE_POTION_EFFECT = "living_entity.remove_potion_effect";
    String LIVING_ENTITY_DAMAGE = "living_entity.damage";
    String LIVING_ENTITY_DAMAGE_BY = "living_entity.damage_by";
    String LIVING_ENTITY_ATTRIBUTE = "living_entity.attribute";
    String PLAYER_PLAY_SOUND = "player.play_sound";
    String CREEPER_EXPLODE = "creeper.explode";
    String CREEPER_IGNITE = "creeper.ignite";
    String ITEM_TYPE_CREATE_ITEM_STACK = "item_type.create_item_stack";
    String WORLD_BLOCK_AT = "world.block_at";
    String WORLD_SPAWN_PARTICLE = "world.spawn_particle";
    String WORLD_SPAWN_PARTICLE_WITH_DATA = "world.spawn_particle_with_data";
    String WORLD_STRIKE_LIGHTNING = "world.strike_lightning";
    String WORLD_STRIKE_LIGHTNING_EFFECT = "world.strike_lightning_effect";
    String LOCATION_DISTANCE = "location.distance";
    String POTION_EFFECT_CREATE = "potion_effect.create";
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

    String CANCELLABLE_SET_CANCELLED = "cancellable.set_cancelled";
    String ENTITY_SET_INVISIBLE = "entity.set_invisible";
    String ENTITY_SET_GLOWING = "entity.set_glowing";
    String ENTITY_SET_VELOCITY = "entity.set_velocity";
    String ENTITY_SET_FIRE_TICKS = "entity.set_fire_ticks";
    String ENTITY_SET_SILENT = "entity.set_silent";
    String LIVING_ENTITY_SET_INVISIBLE = "living_entity.set_invisible";
    String MOB_SET_TARGET = "mob.set_target";
    String CREEPER_SET_MAX_FUSE_TICKS = "creeper.set_max_fuse_ticks";
    String CREEPER_SET_FUSE_TICKS = "creeper.set_fuse_ticks";
    String CREEPER_SET_EXPLOSION_RADIUS = "creeper.set_explosion_radius";
    String CREEPER_SET_POWERED = "creeper.set_powered";
    String EQUIPMENT_SET_HELMET = "equipment.set_helmet";
    String EQUIPMENT_SET_CHESTPLATE = "equipment.set_chestplate";
    String EQUIPMENT_SET_LEGGINGS = "equipment.set_leggings";
    String EQUIPMENT_SET_BOOTS = "equipment.set_boots";
    String EQUIPMENT_SET_ITEM_IN_MAIN_HAND = "equipment.set_item_in_main_hand";
    String EQUIPMENT_SET_ITEM_IN_OFF_HAND = "equipment.set_item_in_off_hand";
    String EQUIPMENT_SET_HELMET_DROP_CHANCE = "equipment.set_helmet_drop_chance";
    String EQUIPMENT_SET_CHESTPLATE_DROP_CHANCE = "equipment.set_chestplate_drop_chance";
    String EQUIPMENT_SET_LEGGINGS_DROP_CHANCE = "equipment.set_leggings_drop_chance";
    String EQUIPMENT_SET_BOOTS_DROP_CHANCE = "equipment.set_boots_drop_chance";
    String EQUIPMENT_SET_ITEM_IN_MAIN_HAND_DROP_CHANCE = "equipment.set_item_in_main_hand_drop_chance";
    String ATTRIBUTE_SET_BASE_VALUE = "attribute.set_base_value";
    String EVENT_DATA_EXPLOSION_PRIME_SET_FIRE = "event_data.explosion_prime.set_fire";
    String EVENT_DATA_EXPLOSION_PRIME_SET_RADIUS = "event_data.explosion_prime.set_radius";
    String EVENT_DATA_ENTITY_DAMAGE_SET_DAMAGE = "event_data.entity_damage.set_damage";
}
