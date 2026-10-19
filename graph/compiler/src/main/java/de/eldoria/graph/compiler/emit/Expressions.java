/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler.emit;

import com.palantir.javapoet.CodeBlock;

import java.util.regex.Pattern;

/**
 * Looks at emitted expressions as text, to decide how they may be placed into other code.
 */
final class Expressions {
    private static final Pattern NAME = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    private static final Pattern PLAIN_LITERAL =
            Pattern.compile("-?[0-9][0-9A-Za-z_.]*|true|false|null|\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'");
    private static final String OPERATORS = "?:+-*/%<>=!&|^~";

    private Expressions() {}

    /**
     * @return whether the expression is a single identifier, such as a local or parameter
     */
    static boolean isName(CodeBlock expression) {
        return NAME.matcher(expression.toString()).matches();
    }

    /**
     * @return whether the expression is a conditional {@code a ? b : c}, which reads poorly when placed into others
     */
    static boolean isConditional(CodeBlock expression) {
        String text = expression.toString();
        String inner = wrappedWhole(text) ? text.substring(1, text.length() - 1) : text;
        return topLevel(inner, "?");
    }

    /**
     * Whether the expression's Java type is exactly what it produces: a name, a literal or a call chain. Arithmetic is
     * not, since {@code (a + b)} of two ints stays an int where the node promised a double; such values keep the typed
     * local that converts them.
     *
     * @return whether the expression is a name, a plain literal or a call chain
     */
    static boolean isCallOrLiteral(CodeBlock expression) {
        String text = expression.toString();
        return isName(expression) || isPlainLiteral(expression) || !hasTopLevelOperator(text);
    }

    /**
     * @return whether the expression is a number, boolean, character, string or {@code null} literal
     */
    static boolean isPlainLiteral(CodeBlock expression) {
        return PLAIN_LITERAL.matcher(expression.toString()).matches();
    }

    /**
     * Wraps an expression in parentheses unless it already binds tighter than any operator around it: a name, a
     * literal, a call chain, or an expression that is already parenthesised as a whole.
     *
     * @param expression the expression
     * @return the expression, safe to place as an operand
     */
    static CodeBlock grouped(CodeBlock expression) {
        String text = expression.toString();
        if (isPlainLiteral(expression) || wrappedWhole(text) || !hasTopLevelOperator(text)) return expression;
        return CodeBlock.of("($L)", expression);
    }

    private static boolean wrappedWhole(String text) {
        if (!text.startsWith("(")) return false;
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                i = skipQuoted(text, i);
                continue;
            }
            if (c == '(') depth++;
            if (c == ')' && --depth == 0) return i == text.length() - 1;
        }
        return false;
    }

    private static boolean topLevel(String text, String symbols) {
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                i = skipQuoted(text, i);
                continue;
            }
            if (c == '(' || c == '[' || c == '{') depth++;
            else if (c == ')' || c == ']' || c == '}') depth--;
            else if (depth == 0 && symbols.indexOf(c) >= 0) return true;
        }
        return false;
    }

    private static boolean hasTopLevelOperator(String text) {
        if (text.startsWith("(")) return true;
        if (text.contains(" instanceof ")) return true;
        return topLevel(text, OPERATORS);
    }

    private static int skipQuoted(String text, int start) {
        char quote = text.charAt(start);
        for (int i = start + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') i++;
            else if (c == quote) return i;
        }
        return text.length();
    }
}
