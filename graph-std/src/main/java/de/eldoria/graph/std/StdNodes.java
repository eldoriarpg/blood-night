/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

/**
 * Type ids of the standard nodes, for building graphs in code without spelling ids by hand.
 */
public interface StdNodes {
    String DEBUG_PRINT = "debug.print";
    String FLOW_BATCH = "flow.batch";
    String FLOW_BRANCH = "flow.branch";
    String FLOW_FOR_EACH = "flow.for_each";
    String FLOW_SWITCH = "flow.switch";
    String FUNCTION_CHANCE = "function.chance";
    String FUNCTION_COOLDOWN = "function.cooldown";
    String FUNCTION_RANDOM_INTEGER = "function.random_integer";
    String FUNCTION_RANDOM_NUMBER = "function.random_number";
    String LOGIC_AND = "logic.and";
    String LOGIC_EQUAL = "logic.equal";
    String LOGIC_GREATER = "logic.greater";
    String LOGIC_GREATER_OR_EQUAL = "logic.greater_or_equal";
    String LOGIC_IS_NULL = "logic.is_null";
    String LOGIC_LESS = "logic.less";
    String LOGIC_LESS_OR_EQUAL = "logic.less_or_equal";
    String LOGIC_NOT = "logic.not";
    String LOGIC_OR = "logic.or";
    String MATH_ABS = "math.abs";
    String MATH_ABS_INTEGER = "math.abs_integer";
    String MATH_ADD = "math.add";
    String MATH_CEIL = "math.ceil";
    String MATH_CLAMP = "math.clamp";
    String MATH_CLAMP_INTEGER = "math.clamp_integer";
    String MATH_DIVIDE = "math.divide";
    String MATH_FLOOR = "math.floor";
    String MATH_MULTIPLY = "math.multiply";
    String MATH_ROUND = "math.round";
    String MATH_SUBTRACT = "math.subtract";
    String TIMER_ELAPSED = "timer.elapsed";
    String TIMER_RESET = "timer.reset";
    String VALUE_BOOLEAN = "value.boolean";
    String VALUE_INTEGER = "value.integer";
    String VALUE_NUMBER = "value.number";
    String VALUE_TEXT = "value.text";

    /**
     * @param selectable id of a selectable type
     * @return the type id of the cast node to that type
     */
    static String cast(String selectable) {
        return "cast." + selectable;
    }
}
