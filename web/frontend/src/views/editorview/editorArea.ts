/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** The areas of the editor, one tab each. */
export type EditorArea = 'mobs' | 'macros' | 'items' | 'settings' | 'revisions'

/** The areas in the order of their tabs. */
export const EDITOR_AREAS: readonly EditorArea[] = ['mobs', 'macros', 'items', 'settings', 'revisions']
