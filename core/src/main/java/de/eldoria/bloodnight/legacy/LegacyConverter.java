/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.legacy;

import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.elements.ConfigurationScope;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.mobsettings.AttributeOverride;
import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.mob.meta.VanillaLoot;
import de.eldoria.bloodnight.store.MobStore;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Turns the old plugin's settings into this plugin's, on top of the current ones: only what the old plugin had a value
 * for is replaced. Each setting is written into the tree of the current settings and read back with the web editor's
 * mapper, so it is checked like an editor change. Everything that cannot be taken over is reported.
 * <p>
 * Old mobs are named by their class ({@code SpeedCreeper}); in snake case they are the ids of the store's mobs, which
 * are installed untouched so they keep getting updates. A world where an old mob was off gives it weight 0; a world
 * with its own name, health or damage for it gets an {@link AttributeOverride}. Drops become registered items.
 */
public final class LegacyConverter {
    private static final ObjectMapper MAPPER = EditorPayload.MAPPER;
    private static final List<String> MOON_PHASES = List.of(
            "FULL_MOON",
            "WANING_GIBBOUS",
            "LAST_QUARTER",
            "WANING_CRESCENT",
            "NEW_MOON",
            "WAXING_CRESCENT",
            "FIRST_QUARTER",
            "WAXING_GIBBOUS");

    private final Legacy legacy;
    private final MobStore store;
    private final Function<Object, Optional<ItemStack>> items;
    private final Function<String, Optional<String>> sounds;
    private final List<String> notes = new ArrayList<>();
    private ItemRegistry registry;

    /**
     * @param legacy the old settings
     * @param store  the store the old mobs are installed from
     * @param items  builds an item from the old plugin's map of it
     * @param sounds the key of a sound by its old name
     */
    public LegacyConverter(
            Legacy legacy,
            MobStore store,
            Function<Object, Optional<ItemStack>> items,
            Function<String, Optional<String>> sounds) {
        this.legacy = legacy;
        this.store = store;
        this.items = items;
        this.sounds = sounds;
        this.registry = new ItemRegistry();
    }

    /**
     * @return a converter reading items and sounds from the running server
     */
    public static LegacyConverter onServer(Legacy legacy, MobStore store) {
        return new LegacyConverter(legacy, store, LegacyNames::item, LegacyNames::sound);
    }

    /**
     * @param main   the current server settings
     * @param worlds the current settings of the loaded worlds, by world name
     * @param mobs   the current mobs
     * @param items  the current registered items
     * @return the converted settings and what could not be taken over
     */
    public Result convert(
            ConfigFile main, Map<String, WorldSettings> worlds, Collection<CustomMob> mobs, ItemRegistry items) {
        notes.clear();
        registry =
                new ItemRegistry(new LinkedHashMap<>(items.registrations()), new LinkedHashMap<>(items.definitions()));
        Map<String, Object> general = legacy.general();
        ConfigFile convertedMain = server(main, general);
        Map<String, CustomMob> installed = installMobs(mobs);
        Map<String, WorldSettings> convertedWorlds = new LinkedHashMap<>();
        legacy.worlds().forEach((name, old) -> {
            WorldSettings current = worlds.get(name);
            if (current == null) {
                notes.add("World %s is not loaded, its settings were skipped".formatted(name));
                return;
            }
            convertedWorlds.put(name, world(current, old, general, installed));
        });
        mobDrops(installed);
        return new Result(
                convertedMain, convertedWorlds, List.copyOf(installed.values()), registry, List.copyOf(notes));
    }

    private ConfigFile server(ConfigFile main, Map<String, Object> general) {
        ObjectNode tree = MAPPER.valueToTree(main);
        copy(general, "prefix", tree, "announcements.prefix");
        copy(general, "broadcastLevel", tree, "announcements.reach");
        copy(general, "broadcastMethod", tree, "announcements.method");
        copy(general, "messageMethod", tree, "announcements.personalMethod");
        copy(general, "language", tree, "announcements.fallbackLocale");
        copy(general, "updateReminder", tree, "updates.notice");
        for (String gone : List.of("mobTick", "beeFix", "autoUpdater")) {
            if (general.containsKey(gone)) notes.add("%s has no use any more and was left out".formatted(gone));
        }
        return MAPPER.treeToValue(tree, ConfigFile.class);
    }

    private WorldSettings world(
            WorldSettings current,
            Map<String, Object> old,
            Map<String, Object> general,
            Map<String, CustomMob> installed) {
        ObjectNode tree = MAPPER.valueToTree(current);
        copy(old, "enabled", tree, "enabled");
        copy(old, "creeperBlockDamage", tree, "mobSettings.creepers.blockDamage");
        copy(old, "alwaysManageCreepers", tree, "mobSettings.creepers.always");
        copy(general, "blindness", tree, "announcement.effect.enabled");
        copy(general, "joinWorldWarning", tree, "announcement.joinWarning");
        copy(general, "blockedCommands", tree, "nightSettings.blockedCommands");
        spawners(tree, general);

        Map<String, Object> bossBar = Legacy.map(old.get("bossBar"));
        copy(bossBar, "enabled", tree, "announcement.bossBar.enabled");
        copy(bossBar, "title", tree, "announcement.bossBar.title");
        copy(bossBar, "color", tree, "announcement.bossBar.color");
        copy(bossBar, "effects", tree, "announcement.bossBar.flags");

        nightSelection(tree, Legacy.map(old.get("nightSelection")));
        Map<String, Object> night = Legacy.map(old.get("nightSettings"));
        copy(night, "skippable", tree, "nightSettings.skippable");
        copy(night, "nightBegin", tree, "nightSettings.begin");
        copy(night, "nightEnd", tree, "nightSettings.end");
        copy(night, "nightDurationMode", tree, "nightSettings.length.mode");
        copy(night, "nightDuration", tree, "nightSettings.length.seconds");
        copy(night, "maxNightDuration", tree, "nightSettings.length.maxSeconds");
        copy(night, "startCommands", tree, "nightSettings.beginCommands");
        copy(night, "endCommands", tree, "nightSettings.endCommands");

        Map<String, Object> mobs = Legacy.map(old.get("mobSettings"));
        copy(mobs, "displayMobNames", tree, "mobSettings.displayNames");
        copy(mobs, "damageMultiplier", tree, "mobSettings.damageModifier");
        copy(mobs, "healthMultiplier", tree, "mobSettings.healthMultiplier");
        copy(mobs, "forcePhantoms", tree, "mobSettings.forcePhantoms");
        copy(mobs, "spawnPercentage", tree, "mobSettings.spawning.spawnPercentage");
        vanilla(tree, mobs, String.valueOf(old.get("world")));
        worldDrops(tree, mobs);
        sounds(tree, Legacy.map(old.get("soundSettings")));
        deathActions(tree, Legacy.map(old.get("deathActionSettings")));
        mobTypes(tree, mobs, installed);
        return MAPPER.treeToValue(tree, WorldSettings.class);
    }

    private static void spawners(ObjectNode tree, Map<String, Object> general) {
        if (Boolean.TRUE.equals(general.get("ignoreSpawnerMobs"))) {
            union(tree, "mobSettings.spawning.ignoredReasons", List.of("SPAWNER", "TRIAL_SPAWNER"));
        }
        if (Boolean.TRUE.equals(general.get("spawnerDropSuppression"))) {
            union(tree, "worldDrops.vanilla.excludedReasons", List.of("SPAWNER", "TRIAL_SPAWNER"));
        }
        List<String> noIncrease = Legacy.list(general.get("noVanillaDropIncrease")).stream()
                .map(String::valueOf)
                .toList();
        union(tree, "worldDrops.vanilla.excludedTypes", noIncrease);
    }

    private void nightSelection(ObjectNode tree, Map<String, Object> old) {
        copy(old, "nightSelectionType", tree, "nightSelection.type");
        copy(old, "probability", tree, "nightSelection.random.probability");
        copy(old, "minCurveVal", tree, "nightSelection.curve.min");
        copy(old, "maxCurveVal", tree, "nightSelection.curve.max");
        copy(old, "period", tree, "nightSelection.curve.period");
        copy(old, "interval", tree, "nightSelection.interval.interval");
        copy(old, "intervalProbability", tree, "nightSelection.interval.probability");
        Map<Integer, Integer> moon = phases(old.get("phases"));
        if (!moon.isEmpty()) {
            ObjectNode probabilities = MAPPER.createObjectNode();
            moon.forEach((phase, chance) -> {
                if (phase >= 0 && phase < MOON_PHASES.size()) probabilities.put(MOON_PHASES.get(phase), chance);
            });
            set(tree, "nightSelection.moonPhase.probabilities", probabilities);
        }
        Map<Integer, Integer> custom = phases(old.get("phasesCustom"));
        if (!custom.isEmpty()) {
            ArrayNode probabilities = MAPPER.createArrayNode();
            custom.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> probabilities.add(entry.getValue()));
            set(tree, "nightSelection.phase.probabilities", probabilities);
        }
    }

    /**
     * @return {@code "phase:chance"} entries by phase; entries that are no such pair are left out
     */
    private static Map<Integer, Integer> phases(@Nullable Object entries) {
        Map<Integer, Integer> phases = new LinkedHashMap<>();
        for (Object entry : Legacy.list(entries)) {
            String[] parts = String.valueOf(entry).split(":");
            if (parts.length != 2) continue;
            try {
                phases.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException ignored) {
                continue;
            }
        }
        return phases;
    }

    private void vanilla(ObjectNode tree, Map<String, Object> mobs, String world) {
        copy(mobs, "experienceMultiplier", tree, "worldDrops.vanilla.xpValue");
        if (mobs.containsKey("experienceMultiplier")) set(tree, "worldDrops.vanilla.xpModifier", text("MULTIPLY"));
        Map<String, Object> vanilla = Legacy.map(mobs.get("vanillaMobSettings"));
        copy(vanilla, "dropMultiplier", tree, "worldDrops.vanilla.lootMultiplier");
        switch (String.valueOf(vanilla.get("vanillaDropMode"))) {
            case "COMBINE" ->
                set(
                        tree,
                        "worldDrops.vanilla.extraDrops",
                        MAPPER.getNodeFactory().booleanNode(true));
            case "CUSTOM" -> {
                set(
                        tree,
                        "worldDrops.vanilla.lootMultiplier",
                        MAPPER.getNodeFactory().numberNode(0));
                set(
                        tree,
                        "worldDrops.vanilla.extraDrops",
                        MAPPER.getNodeFactory().booleanNode(true));
            }
            default -> {}
        }
        for (String multiplier : List.of("damageMultiplier", "healthMultiplier")) {
            Object own = vanilla.get(multiplier);
            if (own != null && !own.equals(mobs.get(multiplier))) {
                notes.add("%s: ordinary monsters had their own %s (%s); they now share the special mobs' value"
                        .formatted(world, multiplier, own));
            }
        }
    }

    private void worldDrops(ObjectNode tree, Map<String, Object> mobs) {
        if (mobs.containsKey("experienceMultiplier")) {
            set(tree, "worldDrops.xpScope", text("WORLD"));
            set(tree, "worldDrops.xpModifier", text("MULTIPLY"));
            copy(mobs, "experienceMultiplier", tree, "worldDrops.xpValue");
        }
        List<Drop> drops = drops(mobs.get("drops"));
        if (drops.isEmpty()) return;
        set(tree, "worldDrops.dropList", MAPPER.valueToTree(drops));
        set(tree, "worldDrops.weightsScope", text("WORLD"));
        set(tree, "worldDrops.weights", MAPPER.valueToTree(weights((int) number(mobs.get("dropAmount"), 1))));
    }

    private void sounds(ObjectNode tree, Map<String, Object> old) {
        if (old.isEmpty()) return;
        Object channel = old.get("channel");
        if (channel != null) {
            String source = String.valueOf(channel).equals("RECORDS") ? "RECORD" : String.valueOf(channel);
            set(tree, "soundSettings.channel", text(source));
        }
        copy(old, "minInterval", tree, "soundSettings.minInterval");
        copy(old, "maxInterval", tree, "soundSettings.maxInterval");
        set(tree, "soundSettings.begin", soundEntries(old.get("startSounds")));
        set(tree, "soundSettings.end", soundEntries(old.get("endSounds")));
        set(tree, "soundSettings.ambience", soundEntries(old.get("randomSounds")));
    }

    private ArrayNode soundEntries(@Nullable Object entries) {
        ArrayNode out = MAPPER.createArrayNode();
        for (Object entry : Legacy.list(entries)) {
            Map<String, Object> sound = Legacy.map(entry);
            String name = String.valueOf(sound.get("sound"));
            Optional<String> key = sounds.apply(name);
            if (key.isEmpty()) {
                notes.add("Sound %s does not exist any more and was left out".formatted(name));
                continue;
            }
            ObjectNode converted = out.addObject();
            converted.put("sound", key.get());
            converted.set("pitch", range(sound.get("pitch")));
            converted.set("volume", range(sound.get("volume")));
        }
        return out;
    }

    /**
     * The old plugin picked one of a list of values; the range runs from the lowest of them to the highest.
     */
    private static ObjectNode range(Object values) {
        List<Double> numbers = values instanceof List<?> list
                ? list.stream()
                        .filter(Number.class::isInstance)
                        .map(value -> ((Number) value).doubleValue())
                        .toList()
                : List.of();
        ObjectNode range = MAPPER.createObjectNode();
        range.put("min", numbers.stream().mapToDouble(Double::doubleValue).min().orElse(1.0));
        range.put("max", numbers.stream().mapToDouble(Double::doubleValue).max().orElse(1.0));
        return range;
    }

    private void deathActions(ObjectNode tree, Map<String, Object> old) {
        deathAction(tree, "deathActions.mob", Legacy.map(old.get("mobDeathActions")));
        Map<String, Object> player = Legacy.map(old.get("playerDeathActions"));
        deathAction(tree, "deathActions.player", player);
        copy(player, "deathCommands", tree, "deathActions.player.deathCommands");
        copy(player, "respawnCommands", tree, "deathActions.player.respawnCommands");
        copy(player, "loseInvProbability", tree, "deathActions.player.loseInventoryChance");
        copy(player, "loseExpProbability", tree, "deathActions.player.loseExperienceChance");
        if (player.containsKey("respawnEffects")) {
            set(tree, "deathActions.player.respawnEffects", effects(player.get("respawnEffects")));
        }
    }

    private void deathAction(ObjectNode tree, String path, Map<String, Object> old) {
        Map<String, Object> lightning = Legacy.map(old.get("lightningSettings"));
        copy(lightning, "doLightning", tree, path + ".lightning.lightning");
        copy(lightning, "lightning", tree, path + ".lightning.lightningChance");
        copy(lightning, "doThunder", tree, path + ".lightning.thunder");
        copy(lightning, "thunder", tree, path + ".lightning.thunderChance");
        Map<String, Object> shockwave = Legacy.map(old.get("shockwaveSettings"));
        copy(shockwave, "shockwaveProbability", tree, path + ".shockwave.chance");
        copy(shockwave, "shockwavePower", tree, path + ".shockwave.power");
        copy(shockwave, "shockwaveRange", tree, path + ".shockwave.range");
        copy(shockwave, "minDuration", tree, path + ".shockwave.minSeconds");
        if (shockwave.containsKey("shockwaveEffects")) {
            set(tree, path + ".shockwave.effects", effects(shockwave.get("shockwaveEffects")));
        }
    }

    /**
     * @param entries the old plugin's map entries of effect name to {@code {effectType, duration}}
     */
    private static ArrayNode effects(@Nullable Object entries) {
        ArrayNode out = MAPPER.createArrayNode();
        for (Object entry : Legacy.list(entries)) {
            Map<String, Object> effect = Legacy.map(Legacy.map(entry).get("object"));
            Object type = effect.get("effectType");
            if (type == null) continue;
            ObjectNode converted = out.addObject();
            converted.put("effect", LegacyNames.effect(String.valueOf(type)));
            converted.put("seconds", number(effect.get("duration"), 5));
        }
        return out;
    }

    /**
     * Installs every old mob that was on in at least one world from the store, unless the server has a mob of its id.
     *
     * @return the server's mobs afterwards, by id
     */
    private Map<String, CustomMob> installMobs(Collection<CustomMob> mobs) {
        Set<String> active = new LinkedHashSet<>();
        Set<String> known = new LinkedHashSet<>();
        for (Map<String, Object> world : legacy.worlds().values()) {
            for (Map<String, Object> mob : oldMobs(Legacy.map(world.get("mobSettings")))) {
                String id = id(String.valueOf(mob.get("mobName")));
                known.add(id);
                if (!Boolean.FALSE.equals(mob.get("active"))) active.add(id);
            }
        }
        Collection<CustomMob> current = mobs;
        for (String id : active) {
            if (store.store().entry(id).isEmpty()) {
                notes.add("The old mob %s is not in the store and was left out".formatted(id));
                continue;
            }
            current = store.install(current, id).mobs();
        }
        known.removeAll(active);
        known.forEach(id -> notes.add("The old mob %s was off in every world and was not installed".formatted(id)));
        Map<String, CustomMob> byId = new LinkedHashMap<>();
        current.forEach(mob -> byId.put(mob.id(), mob));
        return byId;
    }

    private void mobTypes(ObjectNode tree, Map<String, Object> mobs, Map<String, CustomMob> installed) {
        ObjectNode weights = objectAt(tree, "mobSettings.spawning.mobs");
        ObjectNode overrides = objectAt(tree, "mobSettings.overrides");
        for (Map<String, Object> old : oldMobs(mobs)) {
            String id = id(String.valueOf(old.get("mobName")));
            CustomMob mob = installed.get(id);
            if (mob == null) continue;
            if (Boolean.FALSE.equals(old.get("active"))) weights.put(id, 0);
            override(old, mob.attributes()).ifPresent(override -> overrides.set(id, MAPPER.valueToTree(override)));
        }
    }

    private static Optional<AttributeOverride> override(Map<String, Object> old, Attributes attributes) {
        String name = String.valueOf(old.getOrDefault("displayName", ""));
        ValueModifier healthModifier = modifier(old.get("healthModifier"));
        ValueModifier damageModifier = modifier(old.get("damageModifier"));
        double health = number(old.get("health"), attributes.health());
        double damage = number(old.get("damage"), attributes.damage());
        boolean healthSame = healthModifier == attributes.healthModifier() && health == attributes.health();
        boolean damageSame = damageModifier == attributes.damageModifier() && damage == attributes.damage();
        if (name.isBlank() && healthSame && damageSame) return Optional.empty();
        return Optional.of(new AttributeOverride(
                name.isBlank() ? null : name,
                healthSame ? null : healthModifier,
                healthSame ? null : health,
                damageSame ? null : damageModifier,
                damageSame ? null : damage));
    }

    /**
     * Gives installed mobs the drops the first world with any set for them; old worlds could differ, new mobs cannot.
     */
    private void mobDrops(Map<String, CustomMob> installed) {
        Set<String> done = new LinkedHashSet<>();
        Map<String, Boolean> natural = new LinkedHashMap<>();
        legacy.worlds().forEach((world, settings) -> {
            Map<String, Object> mobs = Legacy.map(settings.get("mobSettings"));
            natural.put(world, !Boolean.FALSE.equals(mobs.get("naturalDrops")));
            for (Map<String, Object> old : oldMobs(mobs)) {
                String id = id(String.valueOf(old.get("mobName")));
                CustomMob mob = installed.get(id);
                List<Drop> drops = drops(old.get("drops"));
                if (mob == null || drops.isEmpty()) continue;
                if (!done.add(id)) {
                    notes.add("%s: the drops of %s were left out, another world's are used".formatted(world, id));
                    continue;
                }
                int amount = (int) number(old.get("dropAmount"), -1);
                boolean own = Boolean.TRUE.equals(old.get("overrideDefaultDrops"));
                MobDrops current = mob.mobDrops();
                installed.put(
                        id,
                        new CustomMob(
                                mob.id(),
                                mob.equipment(),
                                mob.attributes(),
                                mob.behaviour(),
                                new MobDrops(
                                        amount > 0 ? ConfigurationScope.MOB : ConfigurationScope.WORLD,
                                        amount > 0 ? weights(amount) : current.weights(),
                                        own
                                                ? List.of(ConfigurationScope.MOB)
                                                : List.of(
                                                        ConfigurationScope.GLOBAL,
                                                        ConfigurationScope.WORLD,
                                                        ConfigurationScope.MOB),
                                        drops,
                                        current.xpScope(),
                                        current.xpModifier(),
                                        current.xpValue(),
                                        current.vanillaLoot()),
                                mob.extension(),
                                mob.spawn(),
                                mob.origin()));
            }
        });
        if (!natural.isEmpty() && !natural.containsValue(true)) {
            installed.replaceAll((id, mob) -> withVanillaLoot(mob, VanillaLoot.CLEAR));
        } else if (natural.containsValue(false)) {
            notes.add(
                    "Some worlds had no vanilla loot for special mobs; special mobs keep their vanilla loot everywhere");
        }
    }

    private static CustomMob withVanillaLoot(CustomMob mob, VanillaLoot loot) {
        MobDrops drops = mob.mobDrops();
        return new CustomMob(
                mob.id(),
                mob.equipment(),
                mob.attributes(),
                mob.behaviour(),
                new MobDrops(
                        drops.weightsScope(),
                        drops.weights(),
                        drops.dropTables(),
                        drops.dropList(),
                        drops.xpScope(),
                        drops.xpModifier(),
                        drops.xpValue(),
                        loot),
                mob.extension(),
                mob.spawn(),
                mob.origin());
    }

    private List<Drop> drops(@Nullable Object entries) {
        List<Drop> drops = new ArrayList<>();
        for (Object entry : Legacy.list(entries)) {
            Map<String, Object> drop = Legacy.map(entry);
            Optional<ItemStack> item = items.apply(drop.get("item"));
            if (item.isEmpty()) {
                notes.add("A drop item could not be read and was left out");
                continue;
            }
            String id = registry.register(item.get());
            drops.add(new Drop(id, item.get().getAmount(), (int) number(drop.get("weight"), 1)));
        }
        return drops;
    }

    private static List<Map<String, Object>> oldMobs(Map<String, Object> mobs) {
        List<Map<String, Object>> found = new ArrayList<>();
        Legacy.map(mobs.get("mobTypes")).forEach((type, list) -> {
            for (Object mob : Legacy.list(list)) found.add(Legacy.map(mob));
        });
        return found;
    }

    /**
     * @param mobName an old mob's class name, e.g. {@code SpeedCreeper}
     * @return its id, e.g. {@code speed_creeper}
     */
    static String id(String mobName) {
        return mobName.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }

    private static Map<Integer, Integer> weights(int amount) {
        Map<Integer, Integer> weights = new LinkedHashMap<>();
        for (int count = 1; count <= Math.max(amount, 1); count++) weights.put(count, 1);
        return weights;
    }

    private static ValueModifier modifier(@Nullable Object value) {
        try {
            return ValueModifier.valueOf(String.valueOf(value));
        } catch (IllegalArgumentException e) {
            return ValueModifier.DEFAULT;
        }
    }

    private static double number(@Nullable Object value, double fallback) {
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private static JsonNode text(String value) {
        return MAPPER.getNodeFactory().stringNode(value);
    }

    private static void copy(Map<String, Object> from, String key, ObjectNode tree, String path) {
        if (from.containsKey(key)) set(tree, path, MAPPER.valueToTree(from.get(key)));
    }

    private static void union(ObjectNode tree, String path, List<String> values) {
        if (values.isEmpty()) return;
        Set<String> merged = new LinkedHashSet<>();
        JsonNode current = at(tree, path);
        if (current != null) current.forEach(value -> merged.add(value.asString()));
        merged.addAll(values);
        set(tree, path, MAPPER.valueToTree(merged));
    }

    private static @Nullable JsonNode at(ObjectNode tree, String path) {
        JsonNode node = tree.at("/" + path.replace('.', '/'));
        return node.isMissingNode() ? null : node;
    }

    private static ObjectNode objectAt(ObjectNode tree, String path) {
        JsonNode node = at(tree, path);
        if (node instanceof ObjectNode object) return object;
        ObjectNode created = MAPPER.createObjectNode();
        set(tree, path, created);
        return created;
    }

    private static void set(ObjectNode tree, String path, JsonNode value) {
        String[] keys = path.split("\\.");
        ObjectNode parent = tree;
        for (int i = 0; i < keys.length - 1; i++) {
            JsonNode child = parent.get(keys[i]);
            ObjectNode next = child instanceof ObjectNode object ? object : MAPPER.createObjectNode();
            if (next != child) parent.set(keys[i], next);
            parent = next;
        }
        parent.set(keys[keys.length - 1], value);
    }

    /**
     * The settings after the conversion.
     *
     * @param main   the server settings
     * @param worlds the settings of the converted worlds, by world name
     * @param mobs   every mob afterwards
     * @param items  every registered item afterwards
     * @param notes  what could not be taken over, or not as it was
     */
    public record Result(
            ConfigFile main,
            Map<String, WorldSettings> worlds,
            List<CustomMob> mobs,
            ItemRegistry items,
            List<String> notes) {}
}
