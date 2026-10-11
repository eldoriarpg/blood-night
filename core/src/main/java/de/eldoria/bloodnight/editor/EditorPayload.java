package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.serialization.EditorSerialization;
import de.eldoria.bloodnight.mobs.MobRegistry;
import de.eldoria.bloodnight.nodes.registry.meta.NodeRegistration;
import de.eldoria.jacksonbukkit.JacksonPaper;

import java.util.List;

public class EditorPayload {
    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(JacksonPaper.builder().build())
            .addModule(new EditorSerialization())
            .build();
    @JsonProperty
    private final MobRegistry mobs;
    @JsonProperty
    private final List<NodeRegistration> nodes;
    @JsonProperty
    private final DataTypes dataTypes;
    @JsonProperty
    private final ItemRegistry registry;
    private final MobDrops defaultDrops;

    public EditorPayload(MobRegistry mobs, List<NodeRegistration> nodes, DataTypes dataTypes, ItemRegistry registry, MobDrops defaultDrops) {
        this.mobs = mobs;
        this.nodes = nodes;
        this.dataTypes = dataTypes;
        this.registry = registry;
        this.defaultDrops = defaultDrops;
    }

    public String asJson() throws JacksonException {
        return MAPPER.writeValueAsString(this);
    }
}
