/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime;

/**
 * Integer arithmetic that cannot throw, as generated code writes it, statically imported: {@code divide(a, b)}.
 */
public final class Integers {
    private Integers() {}

    /**
     * @param dividend the number divided
     * @param divisor  the number divided by
     * @return the quotient rounded toward zero, or 0 when dividing by zero
     */
    public static int divide(int dividend, int divisor) {
        return divisor == 0 ? 0 : dividend / divisor;
    }

    /**
     * @param dividend the number divided
     * @param divisor  the number divided by
     * @return the quotient rounded toward zero, or 0 when dividing by zero
     */
    public static long divide(long dividend, long divisor) {
        return divisor == 0 ? 0 : dividend / divisor;
    }

    /**
     * @param dividend the number divided
     * @param divisor  the number divided by
     * @return the remainder with the divisor's sign, e.g. -1 modulo 3 is 2; 0 when dividing by zero
     */
    public static int modulo(int dividend, int divisor) {
        return divisor == 0 ? 0 : Math.floorMod(dividend, divisor);
    }
}
