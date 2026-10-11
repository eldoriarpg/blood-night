package de.eldoria.bloodnight.behaviour;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mobs.MobRegistry;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.BooleanNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BehaviourServiceTest {
    private static GraphPlatform platform;

    @TempDir
    Path cacheDir;

    @BeforeAll
    static void platform() {
        platform = BloodNightPlatform.create(CompileClasspath.discover(BehaviourServiceTest.class.getClassLoader()));
    }

    private BehaviourService service(String version) {
        return new BehaviourService(platform, getClass().getClassLoader(), new BytecodeCache(cacheDir), version, Logger.getLogger("test"));
    }

    private static Graph cancelDamage() {
        return Graph.builder()
                .node("damage", NodeInstance.of("event.damage").chain("next", "cancel"))
                .node("cancel", NodeInstance.of("cancellable.set_cancelled").input("target", "damage", "event").literal("value", BooleanNode.TRUE))
                .build();
    }

    private static Graph broken() {
        return Graph.builder()
                .node("tick", NodeInstance.of("mob.tick").chain("next", "print"))
                .node("print", NodeInstance.of("debug.print"))
                .build();
    }

    private long cacheFiles() throws IOException {
        try (Stream<Path> files = Files.list(cacheDir)) {
            return files.count();
        }
    }

    @Test
    void compilesEachMobOnItsOwn() {
        Behaviours result = service("1").compileNow(List.of(Mobs.zombie("good", cancelDamage()), Mobs.zombie("bad", broken())), List.of());

        assertTrue(result.of("good").isPresent());
        assertTrue(result.of("bad").isEmpty());
        assertTrue(result.problems().get("bad").stream().anyMatch(problem -> "print".equals(problem.node()) && "value".equals(problem.port())));
        assertEquals(Set.of("event.damage"), result.usedTriggers());
    }

    @Test
    void unchangedGraphsComeFromTheCache() throws IOException {
        CustomMob mob = Mobs.zombie("good", cancelDamage());
        Behaviours first = service("1").compileNow(List.of(mob), List.of());

        Behaviours second = service("1").compileNow(List.of(mob), List.of());

        assertEquals(0, first.fromCache());
        assertEquals(1, second.fromCache());
        assertEquals(first.of("good").orElseThrow().source(), second.of("good").orElseThrow().source());
        assertEquals(1, cacheFiles());
    }

    @Test
    void newVersionsAndRemovedMobsInvalidateTheCache() throws IOException {
        CustomMob mob = Mobs.zombie("good", cancelDamage());
        service("1").compileNow(List.of(mob), List.of());

        Behaviours upgraded = service("2").compileNow(List.of(mob), List.of());
        assertEquals(0, upgraded.fromCache());
        assertEquals(1, cacheFiles());

        service("2").compileNow(List.of(), List.of());
        assertEquals(0, cacheFiles());
    }

    @Test
    void mobRegistryReadsWhatItWrites() {
        MobRegistry registry = new MobRegistry();
        registry.add(Mobs.zombie("good", cancelDamage()));
        JsonMapper mapper = JsonMapper.builder().build();

        MobRegistry read = mapper.readValue(mapper.writeValueAsString(registry), MobRegistry.class);

        assertEquals(registry.get("good"), read.get("good"));
    }

    @Test
    void oldMobConfigsAreMigrated() {
        String json = """
                {"id": "legacy",
                 "equipment": {},
                 "attributes": {"displayName": "Legacy", "entityType": "ZOMBIE", "exactType": false,
                                "healthModifier": "DEFAULT", "health": 20, "damageModifier": "DEFAULT", "damage": 1},
                 "extension": {"type": "NONE"},
                 "nodes": {"nodes": {
                   "0": {"nodeClass": "de.eldoria.bloodnight.nodes.value.impl.IntegerNode", "input": {}, "value": 5, "id": 0},
                   "1": {"nodeClass": "de.eldoria.bloodnight.nodes.action.impl.PrintNode", "input": {"value": {"node": 0, "name": "value"}}, "id": 1},
                   "2": {"nodeClass": "de.eldoria.bloodnight.nodes.trigger.impl.TickNode", "input": {}, "executions": {"nextNodes": {"next": [1]}}, "id": 2}
                 }}}
                """;

        CustomMob mob = JsonMapper.builder().build().readValue(json, CustomMob.class);
        Behaviours result = service("1").compileNow(List.of(mob), List.of());

        assertEquals("mob.tick", mob.behaviour().nodes().get("2").type());
        assertTrue(mob.migrationNotes().isEmpty());
        assertTrue(result.of("legacy").isPresent());
        String written = JsonMapper.builder().build().writeValueAsString(mob);
        assertTrue(written.contains("\"behaviour\""), written);
        assertTrue(!written.contains("\"nodes\":{\"nodes\"") && !written.contains("migrationNotes"), written);
    }
}
