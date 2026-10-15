/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {LastApplied} from '~/api/generated/schema'

/**
 * How many revisions were saved since the server last applied the session; 0 before the first apply.
 */
export function changesSinceApply(revision: number, lastApplied: LastApplied | null): number {
    return lastApplied ? Math.max(0, revision - lastApplied.revision) : 0
}
