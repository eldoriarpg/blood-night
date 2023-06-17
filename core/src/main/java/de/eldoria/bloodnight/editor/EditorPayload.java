package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.meta.Drops;
import de.eldoria.bloodnight.mob.serialization.EditorSerialization;
import de.eldoria.bloodnight.mobs.MobRegistry;
import de.eldoria.bloodnight.nodes.registry.meta.NodeRegistration;
import de.eldoria.jacksonbukkit.JacksonPaper;

import java.util.List;

public class EditorPayload {
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(JacksonPaper.builder().build())
            .registerModule(new EditorSerialization());
    @JsonProperty
    private final MobRegistry mobs;
    @JsonProperty
    private final List<NodeRegistration> nodes;
    @JsonProperty
    private final DataTypes dataTypes;
    @JsonProperty
    private final ItemRegistry registry;
    private final Drops defaultDrops;

    public EditorPayload(MobRegistry mobs, List<NodeRegistration> nodes, DataTypes dataTypes, ItemRegistry registry, Drops defaultDrops) {
        this.mobs = mobs;
        this.nodes = nodes;
        this.dataTypes = dataTypes;
        this.registry = registry;
        this.defaultDrops = defaultDrops;
    }

    public String asJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }
}
