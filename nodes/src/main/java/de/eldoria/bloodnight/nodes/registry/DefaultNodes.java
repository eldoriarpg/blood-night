package de.eldoria.bloodnight.nodes.registry;

import de.eldoria.bloodnight.nodes.action.impl.PrintNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.BatchNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.ForEachNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.IfNode;
import de.eldoria.bloodnight.nodes.transform.impl.construction.CreateLocationNode;
import de.eldoria.bloodnight.nodes.transform.impl.construction.CreateVectorNode;
import de.eldoria.bloodnight.nodes.transform.impl.deconstruction.SplitEntityNode;
import de.eldoria.bloodnight.nodes.transform.impl.deconstruction.SplitLocationNode;
import de.eldoria.bloodnight.nodes.transform.impl.deconstruction.SplitVectorNode;
import de.eldoria.bloodnight.nodes.transform.impl.function.CooldownNode;
import de.eldoria.bloodnight.nodes.transform.impl.function.NearbyEntitiesNode;
import de.eldoria.bloodnight.nodes.transform.impl.function.RandomIntegerNode;
import de.eldoria.bloodnight.nodes.transform.impl.function.RandomNumberNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.AndNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.EqualNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.GreaterNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.GreaterOrEqualNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.IsNullNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.LessNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.LessOrEqualNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.NotNode;
import de.eldoria.bloodnight.nodes.transform.impl.logical.OrNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.AbsIntegerNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.AbsNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.AddNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.CeilNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.ClampIntegerNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.ClampNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.DirectionVectorNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.DistanceNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.DistanceSqrtNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.DivideNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.FloorNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.MultiplyNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.NormalizeNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.RoundNode;
import de.eldoria.bloodnight.nodes.transform.impl.math.SubtractNode;
import de.eldoria.bloodnight.nodes.trigger.impl.DebugNode;
import de.eldoria.bloodnight.nodes.trigger.impl.TickNode;
import de.eldoria.bloodnight.nodes.value.impl.BooleanNode;
import de.eldoria.bloodnight.nodes.value.impl.IntegerNode;
import de.eldoria.bloodnight.nodes.value.impl.NumberNode;
import de.eldoria.bloodnight.nodes.value.impl.StringNode;

import static de.eldoria.bloodnight.nodes.registry.NodeRegistry.register;

public class DefaultNodes {
    public static void registerAll() {
        // Actions
        register(PrintNode.class);

        // Control Flow
        register(BatchNode.class);
        register(ForEachNode.class);
        register(IfNode.class);

        // Transform - Construction
        register(CreateLocationNode.class);
        register(CreateVectorNode.class);

        // Transform - Deconstruction
        register(SplitEntityNode.class);
        register(SplitLocationNode.class);
        register(SplitVectorNode.class);

        // Transform - Function
        register(CooldownNode.class);
        register(NearbyEntitiesNode.class);
        register(RandomIntegerNode.class);
        register(RandomNumberNode.class);

        // Transform - logical
        register(AndNode.class);
        register(EqualNode.class);
        register(GreaterNode.class);
        register(GreaterOrEqualNode.class);
        register(IsNullNode.class);
        register(LessNode.class);
        register(LessOrEqualNode.class);
        register(NotNode.class);
        register(OrNode.class);

        // Transform - math
        register(AbsIntegerNode.class);
        register(AbsNode.class);
        register(AddNode.class);
        register(CeilNode.class);
        register(ClampIntegerNode.class);
        register(ClampNode.class);
        register(DirectionVectorNode.class);
        register(DistanceNode.class);
        register(DistanceSqrtNode.class);
        register(DivideNode.class);
        register(FloorNode.class);
        register(MultiplyNode.class);
        register(NormalizeNode.class);
        register(RoundNode.class);
        register(SubtractNode.class);

        // Trigger
        register(DebugNode.class);
        register(TickNode.class);

        // Value
        register(BooleanNode.class);
        register(IntegerNode.class);
        register(NumberNode.class);
        register(StringNode.class);
    }
}
