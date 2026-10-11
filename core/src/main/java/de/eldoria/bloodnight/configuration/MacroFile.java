/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.graph.model.Macro;

import java.util.List;

/**
 * The macros users defined, shared by all mobs.
 *
 * @param macros the macros
 */
public record MacroFile(List<Macro> macros) {
    public MacroFile {
        macros = macros == null ? List.of() : List.copyOf(macros);
    }

    public MacroFile() {
        this(List.of());
    }
}
