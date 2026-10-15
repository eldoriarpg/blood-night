/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {editorApi, failure} from '~/api/editor'
import {openEditorStream} from '~/api/editorStream'
import type {SessionView, StreamEvent} from '~/api/generated/schema'
import type {EditorPayload} from '~/api/generated/payload'
import {applyPatch, type PatchOperation} from '~/util/patch/jsonPatch'
import {editorState} from '~/util/editorState'

export type {EditorStatus} from '~/util/editorState'

const RECONNECT_MILLIS = 3000
const UNAUTHORIZED = 4401

/**
 * Opens a session and keeps it in step with everyone editing it: the payload is the service's, changed by every
 * revision the stream reports in order; a gap or a lost stream loads the session again. The payload shown adds this
 * participant's changes still on their way.
 * <p>
 * The payload is replaced as a whole on every change and never changed in place, so it is kept out of Vue's deep
 * reactivity (`markRaw`): with hundreds of mobs, proxying and copying every field made each toggle stall.
 */
export function useEditorSession() {
    const {data, stream, reset} = editorState()

    const payload = computed<EditorPayload | null>(() => {
        const confirmed = data.value.confirmed
        if (!confirmed) return null
        try {
            return data.value.pending.reduce((current, change) => applyPatch(current, change.operations), confirmed)
        } catch {
            return confirmed
        }
    })

    function api() {
        return editorApi(data.value.id, data.value.token)
    }

    function take(view: SessionView): void {
        data.value.confirmed = markRaw(view.payload as EditorPayload)
        data.value.revision = view.revision
        data.value.pending = []
        data.value.locks = view.locks
        data.value.online = view.online
        data.value.problems = view.problems
        data.value.state = view.state
        data.value.lastApplied = view.lastApplied ?? null
        data.value.you = view.you ?? data.value.you
        data.value.areas = view.areas
    }

    async function reload(): Promise<void> {
        try {
            take(await api().session())
        } catch (error) {
            if (failure(error).status === 404 || failure(error).status === 401) data.value.status = 'gone'
        }
    }

    function changed(event: StreamEvent): void {
        const revision = event.revision ?? 0
        if (revision <= data.value.revision) return
        if (revision !== data.value.revision + 1 || !data.value.confirmed) {
            void reload()
            return
        }
        const operations = (event.operations ?? []) as PatchOperation[]
        data.value.confirmed = markRaw(applyPatch(data.value.confirmed, operations))
        data.value.revision = revision
        data.value.state = 'EDITING'
        if (event.author === data.value.you) {
            const sent = JSON.stringify(operations)
            const index = data.value.pending.findIndex(change => JSON.stringify(change.operations) === sent)
            if (index >= 0) data.value.pending.splice(index, 1)
        }
    }

    function applied(revision: number): void {
        data.value.lastApplied = {revision, at: new Date().toISOString()}
        if (revision === data.value.revision) data.value.state = 'APPLIED'
    }

    function heard(event: StreamEvent): void {
        switch (event.type) {
            case 'HELLO':
                data.value.online = event.participants ?? []
                data.value.locks = event.locks ?? []
                if ((event.revision ?? 0) !== data.value.revision) void reload()
                break
            case 'CHANGE': changed(event); break
            case 'LOCK': data.value.locks = [...data.value.locks.filter(lock => lock.section !== event.section), {section: event.section ?? '', holder: event.holder ?? ''}]; break
            case 'UNLOCK': data.value.locks = data.value.locks.filter(lock => lock.section !== event.section); break
            case 'PRESENCE': data.value.online = event.participants ?? []; break
            case 'SUBMITTED': data.value.state = 'SUBMITTED'; break
            case 'APPLIED': applied(event.revision ?? 0); break
            case 'CLOSED': data.value.status = 'gone'; break
        }
    }

    function connect(): void {
        stream.value?.close()
        stream.value = openEditorStream(data.value.id, data.value.token, {
            event: heard,
            closed: code => {
                if (code === UNAUTHORIZED || data.value.status === 'gone') {
                    data.value.status = 'gone'
                    return
                }
                setTimeout(() => {
                    if (data.value.status === 'ready') {
                        void reload()
                        connect()
                    }
                }, RECONNECT_MILLIS)
            },
        }, window.location.origin)
    }

    async function open(id: string, token: string): Promise<void> {
        reset()
        data.value.id = id
        data.value.token = token
        data.value.status = 'loading'
        try {
            const view = await api().session()
            take(view)
            data.value.catalog = await api().catalog(view.catalog)
            data.value.status = 'ready'
            connect()
        } catch (error) {
            const status = failure(error).status
            data.value.status = status === 404 || status === 401 ? 'gone' : 'failed'
        }
    }

    const catalog = computed(() => data.value.catalog)

    return {
        data: readonly(data),
        payload,
        catalog,
        api,
        open,
        reload,
        close: reset,
    }
}
