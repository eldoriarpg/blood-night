package de.eldoria.bloodnight.nodes.trigger.base;

import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.meta.Fields;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.Map;

public abstract class CancelableEventTriggerNode<T extends CancelableEventTriggerNode<T, V>, V extends Event & Cancellable> extends TriggerNode<T, V> {

    protected V event;

    public CancelableEventTriggerNode(Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
    }

    @Override
    protected void inject(V event) {
        this.event = event;
    }

    @Override
    protected OutputContainer output(OutputContainer output) {
        return output.set(Fields.CANCELABLE_EVENT, event);
    }
}
