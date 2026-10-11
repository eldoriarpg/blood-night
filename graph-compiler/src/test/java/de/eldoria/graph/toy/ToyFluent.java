/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.toy;

/**
 * Class with record-style accessors: field-backed, paired, manual, and look-alikes that must stay out.
 */
public class ToyFluent {
    private String name = "fluent";
    private int level;

    public String name() {
        return name;
    }

    public ToyFluent name(String name) {
        this.name = name;
        return this;
    }

    public int level() {
        return level;
    }

    public String label() {
        return "label:" + name;
    }

    public void label(String label) {
        this.name = label;
    }

    public String computed() {
        return name.toUpperCase();
    }

    public int size() {
        return 3;
    }

    public void level(long level) {
        this.level = (int) level;
    }

    @Override
    public String toString() {
        return name;
    }
}
