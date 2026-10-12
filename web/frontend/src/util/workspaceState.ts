/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** Which mob and which macro the workspace shows, kept while the editor switches areas. */
export function workspaceState() {
    return useState<{mobs: string, macros: string}>('workspaceState', () => ({mobs: '', macros: ''}))
}
