/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {workspaceState} from '~/util/workspaceState'

/** Which mob and which macro the workspace shows, kept while the editor switches areas. */
export function useWorkspaceSelection() {
    return workspaceState()
}
