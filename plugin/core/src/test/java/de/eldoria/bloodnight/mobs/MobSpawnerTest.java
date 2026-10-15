/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.api.event.CustomMobNightEndEvent;
import de.eldoria.bloodnight.api.event.CustomMobSpawnEvent;
import de.eldoria.bloodnight.api.event.NightEndAction;
import de.eldoria.bloodnight.configuration.elements.ForeignMobSettings;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightSelection;
import de.eldoria.bloodnight.configuration.elements.world.mobsettings.AttributeOverride;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.AttributeChange;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.MobName;
import de.eldoria.bloodnight.mob.meta.NightEnd;
import de.eldoria.bloodnight.mob.meta.SpawnConditions;
import de.eldoria.bloodnight.mob.meta.SpawnRules;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.mobs.movement.Steering;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightSelector;
import de.eldoria.bloodnight.night.NightTimes;
import de.eldoria.bloodnight.rules.Settings;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.model.Graph;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobSpawnerTest {
    private static final NightTimes TIMES = new NightTimes(13_000, 23_000);
    private ServerMock server;
    private World world;
    private final MobRegistry mobs = new MobRegistry();
    private final WorldSettings settings = Settings.enabled();
    private MobCoordinator coordinator;
    private BloodNights nights;
    private MobSpawner spawner;
    private ForeignMobSettings foreign = new ForeignMobSettings();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        settings.mobSettings().spawning().ignoredReasons().clear();
        CustomMob strong = Mobs.zombie("strong", Graph.builder().build());
        strong = new CustomMob(
                strong.id(),
                strong.equipment(),
                new Attributes(
                        new MobName("<red>", Map.of("en", "Strong")),
                        EntityType.ZOMBIE,
                        false,
                        ValueModifier.VALUE,
                        50,
                        ValueModifier.MULTIPLY,
                        3,
                        List.of(new AttributeChange("minecraft:movement_speed", Operation.ADD_SCALAR, 1))),
                strong.behaviour(),
                strong.mobDrops(),
                strong.extension(),
                strong.spawn());
        mobs.add(strong);
        ItemRegistry items = new ItemRegistry();
        Tasks tasks = new Tasks(MockBukkit.createMockPlugin());
        coordinator = new MobCoordinator(
                new MobOutfitter(() -> items, ignored -> settings.mobSettings()),
                () -> items,
                mobs::get,
                world -> nights.running(world),
                tasks,
                Steering.NONE,
                Logger.getLogger("test"),
                server.getPluginManager()::callEvent);
        NightSelection every = JsonMapper.builder()
                .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build()
                .readValue(
                        "{\"type\":\"INTERVAL\",\"interval\":{\"interval\":1,\"probability\":100}}",
                        NightSelection.class);
        nights = new BloodNights(
                new NightSelector(ignored -> every, ignored -> TIMES, Clock.systemUTC(), () -> 20),
                ignored -> true,
                server.getPluginManager(),
                Logger.getLogger("test"));
        spawner = new MobSpawner(
                mobs::all,
                ignored -> settings,
                coordinator,
                nights,
                tasks,
                server.getPluginManager(),
                new ForeignMobs(() -> foreign, () -> null),
                new SplittableRandom(1),
                RegionRules.NONE);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void at(long fullTime) {
        world.setFullTime(fullTime);
        nights.check(List.of(world));
    }

    private LivingEntity zombie() {
        return (LivingEntity) world.spawnEntity(world.getSpawnLocation(), EntityType.ZOMBIE);
    }

    private LivingEntity spawned() {
        LivingEntity zombie = zombie();
        for (int i = 0; i < 100 && !MobTags.isCustomMob(zombie); i++) spawner.handleSpawnedMob(zombie);
        return zombie;
    }

    @Test
    void mobsSpawnDuringBloodNightsOnly() {
        assertFalse(MobTags.isCustomMob(spawned()));
        at(13_000);
        LivingEntity mob = spawned();
        assertTrue(MobTags.isCustomMob(mob));
        assertEquals(0, MobTags.getNight(mob).orElseThrow());
    }

    @Test
    void wrappingAppliesNameAndAttributes() {
        at(13_000);
        LivingEntity mob = spawned();
        assertNotNull(mob.customName());
        assertFalse(mob.isCustomNameVisible());
        assertEquals(50, mob.getAttribute(Attribute.MAX_HEALTH).getValue());
        assertEquals(50, mob.getHealth());
        assertEquals(
                2 * mob.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue(),
                mob.getAttribute(Attribute.MOVEMENT_SPEED).getValue(),
                1e-9);
    }

    @Test
    void worldsCanOverrideNameAndHealth() {
        settings.mobSettings()
                .overrides()
                .put(
                        "strong",
                        new AttributeOverride(
                                new MobName("<green>", Map.of("en", "Tame")), ValueModifier.VALUE, 80.0, null, null));
        at(13_000);
        LivingEntity mob = spawned();

        assertEquals(80, mob.getAttribute(Attribute.MAX_HEALTH).getValue());
        String name = MiniMessage.miniMessage().serialize(Objects.requireNonNull(mob.customName()));
        assertTrue(name.startsWith("<green>"), name);
        assertTrue(name.contains("bloodnight.mob.strong." + mob.getWorld().getUID()), name);
        assertTrue(name.contains("Tame"), name);
    }

    @Test
    void customMobsAreNotWrappedAgain() {
        at(13_000);
        LivingEntity mob = spawned();
        double health = mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        spawner.handleSpawnedMob(mob);
        assertEquals(health, mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
    }

    @Test
    void mobsGoWhenTheirNightEnds() {
        at(13_000);
        LivingEntity mob = spawned();
        server.getScheduler().performTicks(40);
        assertTrue(mob.isValid());
        at(24_000);
        server.getScheduler().performTicks(20);
        assertFalse(mob.isValid());
    }

    @Test
    void otherPluginsCanKeepAMobPastItsNight() {
        List<NightEndAction> planned = new ArrayList<>();
        server.getPluginManager()
                .registerEvents(
                        new Listener() {
                            @EventHandler
                            void keep(CustomMobNightEndEvent event) {
                                planned.add(event.action());
                                event.action(NightEndAction.KEEP);
                            }
                        },
                        MockBukkit.createMockPlugin());
        at(13_000);
        LivingEntity mob = spawned();
        server.getScheduler().performTicks(40);
        at(24_000);
        server.getScheduler().performTicks(100);
        assertTrue(mob.isValid());
        assertEquals(List.of(NightEndAction.REMOVE), planned);
    }

    @Test
    void mobsOtherPluginsSpawnStayAsTheyAre() {
        settings.mobSettings().spawning().ignoredReasons().add(CreatureSpawnEvent.SpawnReason.CUSTOM);
        at(13_000);
        LivingEntity spawned = zombie();
        assertEquals(CreatureSpawnEvent.SpawnReason.CUSTOM, spawned.getEntitySpawnReason());
        for (int i = 0; i < 100; i++) spawner.handleSpawnedMob(spawned);
        assertFalse(MobTags.isCustomMob(spawned));
    }

    @Test
    void revertedMobsStayAsOrdinaryMobs() {
        CustomMob strong = mobs.get("strong");
        mobs.add(new CustomMob(
                "fading",
                strong.equipment(),
                strong.attributes(),
                strong.behaviour(),
                strong.mobDrops(),
                strong.extension(),
                new SpawnRules(1_000_000, true, NightEnd.REVERT, Set.of(), SpawnConditions.any())));
        at(13_000);
        LivingEntity mob = spawned();
        assertEquals("fading", MobTags.getCustomMobId(mob).orElseThrow());
        at(24_000);
        server.getScheduler().performTicks(20);

        assertTrue(mob.isValid());
        assertFalse(MobTags.isCustomMob(mob));
        assertNull(mob.customName());
        AttributeInstance health = mob.getAttribute(Attribute.MAX_HEALTH);
        assertEquals(health.getBaseValue(), health.getValue());
        AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);
        assertEquals(speed.getBaseValue(), speed.getValue());
    }

    @Test
    void mythicMobsNeedMythicMobs() {
        CustomMob strong = mobs.get("strong");
        Attributes base = strong.attributes();
        mobs.add(new CustomMob(
                "knight",
                strong.equipment(),
                new Attributes(
                        base.name(),
                        base.entityType(),
                        base.exactType(),
                        base.healthModifier(),
                        base.health(),
                        base.damageModifier(),
                        base.damage(),
                        base.changes(),
                        "SkeletalKnight",
                        3),
                strong.behaviour(),
                strong.mobDrops(),
                strong.extension(),
                new SpawnRules(1_000_000, true, NightEnd.REMOVE, Set.of(), SpawnConditions.any())));
        at(13_000);
        LivingEntity zombie = zombie();
        for (int i = 0; i < 100; i++) spawner.handleSpawnedMob(zombie);

        assertTrue(zombie.isValid());
        assertNotEquals(Optional.of("knight"), MobTags.getCustomMobId(zombie));
    }

    @Test
    void mythicMobsOnlyGetTagged() {
        CustomMob strong = mobs.get("strong");
        Attributes base = strong.attributes();
        CustomMob knight = new CustomMob(
                "knight",
                strong.equipment(),
                new Attributes(
                        base.name(),
                        base.entityType(),
                        base.exactType(),
                        base.healthModifier(),
                        base.health(),
                        base.damageModifier(),
                        base.damage(),
                        base.changes(),
                        "SkeletalKnight",
                        3),
                strong.behaviour(),
                strong.mobDrops(),
                strong.extension(),
                strong.spawn());
        LivingEntity zombie = zombie();
        double health = zombie.getAttribute(Attribute.MAX_HEALTH).getValue();

        coordinator.register(zombie, knight);

        assertEquals("knight", MobTags.getCustomMobId(zombie).orElseThrow());
        assertNull(zombie.customName());
        assertEquals(health, zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        assertFalse(new ForeignMobs(() -> foreign, () -> null).isForeign(zombie));
    }

    @Test
    void npcsStayAsTheyAre() {
        at(13_000);
        LivingEntity npc = zombie();
        npc.setMetadata("NPC", new FixedMetadataValue(MockBukkit.createMockPlugin(), true));
        for (int i = 0; i < 100; i++) spawner.handleSpawnedMob(npc);
        assertFalse(MobTags.isCustomMob(npc));
    }

    @Test
    void markedMobsStayAsTheyAreUnlessAllowed() {
        foreign = foreignSettings("{\"markerKeys\":[\"levelledmobs:level\"]}");
        at(13_000);
        LivingEntity levelled = zombie();
        levelled.getPersistentDataContainer()
                .set(NamespacedKey.fromString("levelledmobs:level"), PersistentDataType.INTEGER, 5);
        for (int i = 0; i < 100; i++) spawner.handleSpawnedMob(levelled);
        assertFalse(MobTags.isCustomMob(levelled));

        foreign = foreignSettings("{\"wrap\":true,\"markerKeys\":[\"levelledmobs:level\"]}");
        for (int i = 0; i < 100 && !MobTags.isCustomMob(levelled); i++) spawner.handleSpawnedMob(levelled);
        assertTrue(MobTags.isCustomMob(levelled));
    }

    private static ForeignMobSettings foreignSettings(String json) {
        return JsonMapper.builder()
                .changeDefaultVisibility(checker -> checker.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build()
                .readValue(json, ForeignMobSettings.class);
    }

    @Test
    void otherPluginsCanKeepAnEntityVanilla() {
        server.getPluginManager()
                .registerEvents(
                        new Listener() {
                            @EventHandler
                            void keep(CustomMobSpawnEvent event) {
                                event.setCancelled(true);
                            }
                        },
                        MockBukkit.createMockPlugin());
        at(13_000);

        assertFalse(MobTags.isCustomMob(spawned()));
    }
}
