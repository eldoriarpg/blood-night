/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.defaults;

import de.eldoria.bloodnight.DamageEvents;
import de.eldoria.bloodnight.behaviour.BehaviourService;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.behaviour.BytecodeCache;
import de.eldoria.bloodnight.configuration.elements.world.MobSettings;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.graph.runtime.world.MarkerShape;
import de.eldoria.bloodnight.graph.trigger.TriggerDispatcher;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.MobOutfitter;
import de.eldoria.bloodnight.mobs.movement.Steering;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import de.eldoria.graph.platform.GraphPlatform;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.EntityMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
class DefaultMobsRunTest {
    private final Map<World, Long> running = new HashMap<>();
    private static GraphPlatform platform;
    private static Map<String, CustomMob> mobs;
    private final List<Throwable> errors = new ArrayList<>();
    private ServerMock server;
    private World world;
    private MobCoordinator coordinator;
    private BehaviourService service;
    private TriggerDispatcher dispatcher;

    @TempDir
    Path cacheDir;

    @BeforeAll
    static void build() {
        platform = BloodNightPlatform.create(CompileClasspath.discover(DefaultMobsRunTest.class.getClassLoader()));
        mobs = DefaultMobs.create(platform).stream().collect(Collectors.toMap(CustomMob::id, Function.identity()));
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("world");
        ItemRegistry items = new ItemRegistry();
        coordinator = new MobCoordinator(
                new MobOutfitter(() -> items, ignored -> new MobSettings()),
                () -> items,
                mobs::get,
                world -> running.containsKey(world) ? OptionalLong.of(running.get(world)) : OptionalLong.empty(),
                new Tasks(plugin),
                Steering.NONE,
                Logger.getLogger("test"));
        service = new BehaviourService(
                platform, getClass().getClassLoader(), new BytecodeCache(cacheDir), "test", Logger.getLogger("test"));
        Behaviours behaviours = service.compileNow(mobs.values(), List.of());
        coordinator.rebind(behaviours);
        dispatcher = new TriggerDispatcher(
                coordinator::behaviour, coordinator::extensionBehaviour, (trigger, entity, error) -> errors.add(error));
        dispatcher.register(plugin, behaviours.usedTriggers());
    }

    @AfterEach
    void tearDown() {
        dispatcher.unregister();
        service.close();
        MockBukkit.unmock();
        assertEquals(List.of(), errors);
    }

    private <T extends LivingEntity> T spawn(String mob, Class<T> type) {
        T entity = world.spawn(new Location(world, 0, 64, 0), type);
        coordinator.register(entity, mobs.get(mob));
        return entity;
    }

    @Test
    void armoredZombieWearsDiamondArmour() {
        Zombie zombie = spawn("armored_zombie", Zombie.class);

        assertEquals(Material.DIAMOND_HELMET, zombie.getEquipment().getHelmet().getType());
        assertEquals(Material.DIAMOND_BOOTS, zombie.getEquipment().getBoots().getType());
    }

    @Test
    void speedZombieLungesAtANearbyTargetInsteadOfRunningAllTheTime() {
        Zombie zombie = spawn("speed_zombie", Zombie.class);
        server.getScheduler().performOneTick();
        assertFalse(zombie.hasPotionEffect(PotionEffectType.SPEED));

        PlayerMock player = server.addPlayer();
        player.teleport(new Location(world, 4, 64, 0));
        zombie.setTarget(player);
        server.getScheduler().performOneTick();
        waitAndTick(600);

        PotionEffect speed = zombie.getPotionEffect(PotionEffectType.SPEED);
        assertNotNull(speed);
        assertEquals(3, speed.getAmplifier());
    }

    @Test
    void invisibleZombieShowsItselfWhenHit() {
        Zombie zombie = spawn("invisible_zombie", Zombie.class);
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
        server.getScheduler().performOneTick();
        PlayerMock player = server.addPlayer();

        server.getPluginManager().callEvent(hit(player, zombie));
        server.getScheduler().performOneTick();

        assertFalse(zombie.hasPotionEffect(PotionEffectType.INVISIBILITY));
    }

    @Test
    void unstableCreeperBlastsAsFarAsItsRingShows() {
        Creeper creeper = spawn("unstable_creeper", Creeper.class);

        assertTrue(creeper.isPowered());
        assertEquals(5, creeper.getExplosionRadius());
        assertEquals(50, creeper.getMaxFuseTicks());
    }

    @Test
    void fearfulPhantomSlowsPlayersItHits() {
        Phantom phantom = spawn("fearful_phantom", Phantom.class);
        PlayerMock player = server.addPlayer();

        server.getPluginManager().callEvent(hit(phantom, player));

        PotionEffect slowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
        assertNotNull(slowness);
        assertEquals(2, slowness.getAmplifier());
    }

    @Test
    void unstableCreeperLightsItsFuseAndWarnsWhenAPlayerHitsIt() {
        Creeper creeper = spawn("unstable_creeper", Creeper.class);
        PlayerMock player = server.addPlayer();

        server.getPluginManager().callEvent(hit(player, creeper));
        server.getScheduler().performTicks(3);

        assertTrue(creeper.isIgnited());
        assertFalse(creeper.isDead());
        assertEquals(
                MarkerShape.CIRCLE.bars(5).size(),
                world.getEntitiesByClass(BlockDisplay.class).size());
    }

    @Test
    void assassinSpiderHopsBackFromBelowATargetUpHigh() {
        Spider spider = spawn("assassin_spider", Spider.class);
        ((EntityMock) spider).setOnGround(true);
        PlayerMock player = server.addPlayer();
        player.teleport(new Location(world, 1, 74, 0));
        spider.setTarget(player);

        server.getScheduler().performOneTick();

        assertTrue(spider.getVelocity().getX() < 0, spider.getVelocity().toString());
        assertTrue(spider.getVelocity().getY() > 0, spider.getVelocity().toString());
    }

    @Test
    void assassinSpiderLeapsAtATargetUpHighFromTheSide() {
        Spider spider = spawn("assassin_spider", Spider.class);
        ((EntityMock) spider).setOnGround(true);
        PlayerMock player = server.addPlayer();
        player.teleport(new Location(world, 6, 74, 0));
        spider.setTarget(player);

        server.getScheduler().performOneTick();

        assertTrue(spider.getVelocity().getX() > 0, spider.getVelocity().toString());
        assertTrue(spider.getVelocity().getY() > 1, spider.getVelocity().toString());
    }

    @Test
    void assassinSpiderTakesNoFallDamage() {
        Spider spider = spawn("assassin_spider", Spider.class);
        EntityDamageEvent fall = DamageEvents.of(
                spider, DamageCause.FALL, DamageSource.builder(DamageType.FALL).build(), 4);

        server.getPluginManager().callEvent(fall);

        assertTrue(fall.isCancelled());
    }

    @Test
    void speedCreeperIgnitesWhenTheNightEndsInItsWorld() {
        Creeper creeper = spawn("speed_creeper", Creeper.class);
        World other = server.addSimpleWorld("other");
        Creeper elsewhere = other.spawn(new Location(other, 0, 64, 0), Creeper.class);
        coordinator.register(elsewhere, mobs.get("speed_creeper"));

        running.put(world, 1L);
        running.put(other, 1L);
        server.getScheduler().performOneTick();
        running.remove(world);
        server.getScheduler().performOneTick();

        assertTrue(creeper.isIgnited());
        assertFalse(elsewhere.isIgnited());
    }

    @Test
    void ridersGoWithTheirExtensionWhenTheNightEnds() {
        running.put(world, 1L);
        Spider spider = world.spawn(new Location(world, 0, 64, 0), Spider.class);
        MobTags.markNight(spider, 1);
        coordinator.register(spider, mobs.get("blaze_rider"));
        Entity blaze = coordinator.mob(spider).orElseThrow().extension();
        server.getScheduler().performOneTick();

        running.remove(world);
        server.getScheduler().performTicks(21);

        assertFalse(spider.isValid());
        assertNotNull(blaze);
        assertFalse(blaze.isValid());
    }

    @Test
    void ridersKeepTheirPassengerFromSuffocating() {
        Spider spider = spawn("blaze_rider", Spider.class);
        Entity blaze = coordinator.mob(spider).orElseThrow().extension();
        assertNotNull(blaze);

        EntityDamageEvent suffocation = DamageEvents.of(
                blaze,
                EntityDamageEvent.DamageCause.SUFFOCATION,
                DamageSource.builder(DamageType.IN_WALL).build(),
                1);
        server.getPluginManager().callEvent(suffocation);

        assertTrue(suffocation.isCancelled());
    }

    @Test
    void ridersDieWithTheirPassenger() {
        Spider spider = spawn("blaze_rider", Spider.class);
        LivingEntity blaze =
                (LivingEntity) coordinator.mob(spider).orElseThrow().extension();
        assertNotNull(blaze);

        server.getPluginManager()
                .callEvent(new EntityDeathEvent(
                        blaze, DamageSource.builder(DamageType.GENERIC).build(), List.of()));

        assertTrue(spider.isDead());
    }

    @Test
    void invisibleSkeletonShowsItselfAndItsArrowWhenItShoots() {
        Skeleton skeleton = spawn("invisible_skeleton", Skeleton.class);
        waitAndTick(1100);
        assertTrue(skeleton.hasPotionEffect(PotionEffectType.INVISIBILITY));

        Arrow arrow = world.spawn(skeleton.getLocation(), Arrow.class);
        arrow.setShooter(skeleton);
        server.getPluginManager().callEvent(new ProjectileLaunchEvent(arrow));

        assertFalse(skeleton.hasPotionEffect(PotionEffectType.INVISIBILITY));
        assertTrue(arrow.isGlowing());
    }

    @Test
    void magicSkeletonsArrowsBringTheirEffectsInOrder() {
        Skeleton skeleton = spawn("magic_skeleton", Skeleton.class);
        PlayerMock player = server.addPlayer();
        List<PotionEffectType> expected = List.of(
                PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.POISON, PotionEffectType.NAUSEA);

        for (PotionEffectType type : expected) {
            Arrow arrow = world.spawn(skeleton.getLocation(), Arrow.class);
            arrow.setShooter(skeleton);
            server.getPluginManager().callEvent(new ProjectileHitEvent(arrow, player));
            assertTrue(player.hasPotionEffect(type), type.toString());
        }
    }

    @Test
    void knightZombieBlocksHitsFromTheFrontWhileItsShieldIsUp() {
        Zombie zombie = spawn("knight_zombie", Zombie.class);
        PlayerMock player = server.addPlayer();
        player.teleport(new Location(world, 0, 64, 4));
        zombie.teleport(new Location(world, 0, 64, 0, 0, 0));
        zombie.setTarget(player);
        server.getScheduler().performOneTick();
        assertEquals(Material.SHIELD, zombie.getEquipment().getItemInOffHand().getType());

        EntityDamageByEntityEvent front = hit(player, zombie);
        server.getPluginManager().callEvent(front);
        assertTrue(front.isCancelled());

        waitAndTick(2100);
        EntityDamageByEntityEvent lowered = hit(player, zombie);
        server.getPluginManager().callEvent(lowered);
        assertFalse(lowered.isCancelled());
    }

    @Test
    void fearfulPhantomKnocksGlidersOutOfTheAir() {
        Phantom phantom = spawn("fearful_phantom", Phantom.class);
        PlayerMock player = server.addPlayer();
        player.setGliding(true);

        server.getPluginManager().callEvent(hit(phantom, player));

        assertFalse(player.isGliding());
    }

    @Test
    void toxicSlimesHitsPoison() {
        Slime slime = spawn("toxic_slime", Slime.class);
        PlayerMock player = server.addPlayer();

        server.getPluginManager().callEvent(hit(slime, player));

        assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
    }

    /**
     * Lets real time pass, then ticks: graph delays, timers and cooldowns count milliseconds, not ticks.
     */
    private void waitAndTick(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        server.getScheduler().performOneTick();
    }

    private static EntityDamageByEntityEvent hit(org.bukkit.entity.Entity damager, org.bukkit.entity.Entity victim) {
        return DamageEvents.byEntity(
                damager,
                victim,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                DamageSource.builder(DamageType.GENERIC).build(),
                3);
    }
}
