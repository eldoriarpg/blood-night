package de.eldoria.bloodnight.nodes.value.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.registry.NodeRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringNodeTest {

    @Test
    void serializeData() throws JsonProcessingException {
        StringNode test = new StringNode("Test");
        String asString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(test);
        System.out.println(asString);
        Node node = new ObjectMapper().readValue(asString, Node.class);
        Assertions.assertTrue(node instanceof StringNode);
    }

    @Test
    void serializeSchema() throws JsonProcessingException {
        NodeRegistry.register(StringNode.class);
        String schema = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(NodeRegistry.registrations());
        System.out.println(schema);
    }

    @Test
    void output() {
        var node = new StringNode("Chojo stinkt");
        assertEquals("Chojo stinkt", node.output(new NodeContainer()).value(Fields.VALUE));
    }

    @AfterEach
    void tearDown() {
        NodeRegistry.unregisterAll();
    }
}
