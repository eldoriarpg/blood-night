/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog, LockInfo, PayloadProblem, SessionState} from '~/api/generated/schema'
import type {EditorPayload} from '~/api/generated/payload'
import type {EditorStream} from '~/api/editorStream'
import type {PatchOperation} from '~/util/patch/jsonPatch'

/** A change sent to the service and not yet seen coming back on the stream. */
export interface PendingChange {
    id: number
    operations: PatchOperation[]
}

/** Where the editor stands with its session. */
export type EditorStatus = 'idle' | 'loading' | 'ready' | 'gone' | 'failed'

/** Everything the editor knows about the session it edits. */
export interface EditorData {
    id: string
    token: string
    you: string
    status: EditorStatus
    catalog: Catalog | null
    confirmed: EditorPayload | null
    revision: number
    pending: PendingChange[]
    nextPending: number
    locks: LockInfo[]
    online: string[]
    problems: PayloadProblem[]
    state: SessionState
}

/**
 * The editor's session: the payload as the service confirmed it, the changes still on their way, the catalog, locks
 * and who is online. Held per app, read and changed only through the editor composables.
 */
export function editorState() {
    const data = useState<EditorData>('editorState', () => ({
        id: '', token: '', you: '', status: 'idle', catalog: null, confirmed: null, revision: 0, pending: [], nextPending: 1,
        locks: [], online: [], problems: [], state: 'EDITING',
    }))
    const stream = useState<EditorStream | null>('editorState.stream', () => null)
    return {
        data,
        stream,
        reset(): void {
            stream.value?.close()
            stream.value = null
            Object.assign(data.value, {
                id: '', token: '', you: '', status: 'idle', catalog: null, confirmed: null, revision: 0, pending: [], nextPending: 1,
                locks: [], online: [], problems: [], state: 'EDITING',
            } satisfies EditorData)
        },
    }
}
