/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorPayload, Graph} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import {section} from '~/util/session/sections'

/** What the workspace edits: mobs or macros. */
export type WorkspaceKind = 'mobs' | 'macros'

/** A mob or macro the workspace can open. */
export interface WorkspaceEntry {
    id: string
    detail: string
    graph: Graph
    base: string
    path: string
    section: string
}

/**
 * The mobs or macros of a payload with where their graph and their section are.
 *
 * @param payload the payload
 * @param kind mobs or macros
 * @returns the entries, mobs sorted by id, macros in their order
 */
export function workspaceEntries(payload: EditorPayload, kind: WorkspaceKind): WorkspaceEntry[] {
    if (kind === 'mobs') {
        return Object.values(payload.mobs ?? {}).sort((a, b) => a.id.localeCompare(b.id)).map(mob => ({
            id: mob.id,
            detail: mob.attributes?.entityType ?? '',
            graph: mob.behaviour ?? {nodes: {}},
            base: pointer('mobs', mob.id, 'behaviour'),
            path: pointer('mobs', mob.id),
            section: section.mob(mob.id),
        }))
    }
    return (payload.macros ?? []).map((macro, index) => ({
        id: macro.id,
        detail: macro.name,
        graph: macro.body ?? {nodes: {}},
        base: pointer('macros', index, 'body'),
        path: pointer('macros', index),
        section: section.macro(macro.id),
    }))
}
