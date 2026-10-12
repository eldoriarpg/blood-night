/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {failure} from '~/api/editor'
import {showToast} from '~/util/toast'
import {sectionsOf} from '~/util/session/sections'
import {editorState} from '~/util/editorState'
import type {PatchOperation} from '~/util/patch/jsonPatch'

const ECHO_MILLIS = 4000
const LOCKED = 423

/**
 * Sends changes to the session. A change shows at once; it is refused here when it touches a section someone else
 * holds, and taken back when the service refuses it. A change the stream does not report back in time is caught up
 * with by loading the session again.
 */
export function useEditorChanges() {
    const {data} = editorState()
    const session = useEditorSession()
    const {t} = useI18n()

    function blockedBy(operations: readonly PatchOperation[]): string[] {
        const touched = sectionsOf(session.payload.value, operations)
        return data.value.locks
            .filter(lock => lock.holder !== data.value.you && (touched.everything || touched.sections.has(lock.section)))
            .map(lock => lock.holder)
    }

    async function apply(operations: PatchOperation[]): Promise<boolean> {
        if (operations.length === 0) return true
        let holders: string[]
        try {
            holders = blockedBy(operations)
        } catch (error) {
            showToast((error as Error).message, 'error')
            return false
        }
        if (holders.length > 0) {
            showToast(t('editor.changes.held', {names: [...new Set(holders)].join(', ')}), 'info')
            return false
        }
        const change = {id: data.value.nextPending++, operations}
        data.value.pending.push(change)
        try {
            const accepted = await session.api().change(operations)
            data.value.problems = accepted.problems
            setTimeout(() => {
                if (data.value.pending.some(pending => pending.id === change.id)) void session.reload()
            }, ECHO_MILLIS)
            return true
        } catch (error) {
            data.value.pending = data.value.pending.filter(pending => pending.id !== change.id)
            const refused = failure(error)
            showToast(refused.status === LOCKED ? t('editor.changes.locked', {reason: refused.message}) : refused.message, 'error')
            if (refused.status !== 0) void session.reload()
            return false
        }
    }

    return {apply}
}
