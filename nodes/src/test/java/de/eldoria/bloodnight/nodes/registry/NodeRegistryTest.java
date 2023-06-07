package de.eldoria.bloodnight.nodes.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.eldoria.bloodnight.nodes.base.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NodeRegistryTest {
    @BeforeEach
    void setUp() {
        NodeRegistry.unregisterAll();
    }

    @Test
    void registerAll() {
        for (Class<? extends Node> node : DefaultNodes.defaultNodes()) {
            NodeRegistry.register(node);
        }
    }

    @Test
    void register() {
    }

    @Test
    void unregisterNode() {
    }

    @Test
    void testUnregisterNode() {
    }

    @Test
    void unregisterAll() {
    }

    @Test
    void registrations() {
    }

    @Test
    public void exportSchema() throws JsonProcessingException {
        for (Class<? extends Node> node : DefaultNodes.defaultNodes()) {
            NodeRegistry.register(node);
        }
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(NodeRegistry.registrations()));

    }
}
