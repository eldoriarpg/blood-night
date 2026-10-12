/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {failure} from '~/api/editor'
import {showToast} from '~/util/toast'
import {editorState} from '~/util/editorState'

/**
 * The sections participants hold: who holds one, and taking and releasing them for this participant. A section held
 * by someone else is shown read only.
 */
export function useEditorLocks() {
    const {data} = editorState()
    const session = useEditorSession()
    const {t} = useI18n()

    function holder(section: string): string | null {
        return data.value.locks.find(lock => lock.section === section)?.holder ?? null
    }

    function heldByOther(section: string): string | null {
        const name = holder(section)
        return name !== null && name !== data.value.you ? name : null
    }

    async function take(section: string): Promise<boolean> {
        if (holder(section) === data.value.you) return true
        try {
            await session.api().take(section)
            return true
        } catch (error) {
            if (failure(error).status !== 423) showToast(failure(error).message, 'error')
            return false
        }
    }

    async function release(section: string): Promise<void> {
        if (holder(section) !== data.value.you) return
        try {
            await session.api().release(section)
        } catch (error) {
            showToast(t('editor.locks.release', {reason: failure(error).message}), 'error')
        }
    }

    return {holder, heldByOther, take, release}
}
