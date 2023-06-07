package de.eldoria.bloodnight.nodes.registry;

import de.eldoria.bloodnight.nodes.action.impl.PrintNode;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.controlflow.impl.BatchNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.ForEachNode;
import de.eldoria.bloodnight.nodes.controlflow.impl.BranchNode;
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

import java.util.List;

public class DefaultNodes {
    public static List<Class<? extends Node>> defaultNodes() {
        return List.of(
        // Actions
        PrintNode.class,

        // Control Flow
        BatchNode.class,
        ForEachNode.class,
        BranchNode.class,

        // Transform - Construction
        CreateLocationNode.class,
        CreateVectorNode.class,

        // Transform - Deconstruction
        SplitEntityNode.class,
        SplitLocationNode.class,
        SplitVectorNode.class,

        // Transform - Function
        CooldownNode.class,
        NearbyEntitiesNode.class,
        RandomIntegerNode.class,
        RandomNumberNode.class,

        // Transform - logical
        AndNode.class,
        EqualNode.class,
        GreaterNode.class,
        GreaterOrEqualNode.class,
        IsNullNode.class,
        LessNode.class,
        LessOrEqualNode.class,
        NotNode.class,
        OrNode.class,

        // Transform - math
        AbsIntegerNode.class,
        AbsNode.class,
        AddNode.class,
        CeilNode.class,
        ClampIntegerNode.class,
        ClampNode.class,
        DirectionVectorNode.class,
        DistanceNode.class,
        DistanceSqrtNode.class,
        DivideNode.class,
        FloorNode.class,
        MultiplyNode.class,
        NormalizeNode.class,
        RoundNode.class,
        SubtractNode.class,

        // Trigger
        DebugNode.class,
        TickNode.class,

        // Value
        BooleanNode.class,
        IntegerNode.class,
        NumberNode.class,
        StringNode.class);
    }
}
