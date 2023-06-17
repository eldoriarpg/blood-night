package de.eldoria.bloodnight.nodes.provider.impl;

import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.meta.DataType;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.annotations.Output;
import de.eldoria.bloodnight.nodes.provider.ProviderNode;

@Output(name = Fields.VALUE, type = DataType.ENTITY)
@Meta(name = "Current Entity", description = "Provides the current entity")
public class CurrentEntityNode extends ProviderNode {

    @Override
    public OutputContainer output() {
        return super.output()
                .set(Fields.VALUE, container().meta().entity());
    }
}
