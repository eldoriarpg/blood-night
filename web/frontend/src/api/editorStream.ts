/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {StreamEvent} from '~/api/generated/schema'

/** What a session stream reports to its owner. */
export interface StreamHandlers {
    event(event: StreamEvent): void
    closed(code: number): void
}

/** An open stream; closing it does not report `closed`. */
export interface EditorStream {
    close(): void
}

const PING_MILLIS = 20000

/**
 * Opens the stream of a session for a participant; it pings so the participant stays present. Browsers cannot put a
 * token into a socket's headers, so it goes into the query.
 *
 * @param id the session's id
 * @param token the participant's token
 * @param handlers what to call on events and when the service closes the stream
 * @param origin where the page runs, e.g. `https://bloodnight.example`
 * @returns the stream
 */
export function openEditorStream(id: string, token: string, handlers: StreamHandlers, origin: string): EditorStream {
    const url = new URL(`/api/v1/editor/sessions/${encodeURIComponent(id)}/stream`, origin)
    url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
    url.searchParams.set('token', token)
    const socket = new WebSocket(url)
    let closedByOwner = false
    const ping = setInterval(() => {
        if (socket.readyState === WebSocket.OPEN) socket.send('ping')
    }, PING_MILLIS)
    socket.addEventListener('message', message => handlers.event(JSON.parse(String(message.data)) as StreamEvent))
    socket.addEventListener('close', close => {
        clearInterval(ping)
        if (!closedByOwner) handlers.closed(close.code)
    })
    return {
        close() {
            closedByOwner = true
            clearInterval(ping)
            socket.close(1000)
        },
    }
}
