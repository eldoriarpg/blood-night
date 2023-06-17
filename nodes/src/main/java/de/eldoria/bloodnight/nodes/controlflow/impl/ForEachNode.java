package de.eldoria.bloodnight.nodes.controlflow.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.meta.DataStruct;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Input;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.controlflow.ControlFlowNode;

import java.util.Collection;
import java.util.Map;

/**
 * An execution node executing other nodes for each entry in the provided list and providing the current element.
 */
@Input(name = Fields.VALUE, type = DataType.LINKED, struct = DataStruct.LIST)
@Output(name = Fields.CURRENT, type = DataType.LINKED, link = Fields.VALUE)
@Meta(name = "For Each", description = "executing other nodes for each entry in the provided list and providing the current element")
public class ForEachNode extends ControlFlowNode<ForEachNode> {

    public ForEachNode() {
    }

    @JsonCreator
    public ForEachNode(@JsonProperty("input") Map<String, Edge> input, @JsonProperty("meta") EditorMeta meta) {
        super(input, meta);
    }

    @Override
    public void invoke(NodeContainer container) {
        Collection<Object> collection = input().map(Fields.VALUE).get();
        for (var c : collection) {
            super.output().set(Fields.CURRENT, c);
            super.invoke(container);
        }
    }
}
