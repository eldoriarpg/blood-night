/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {defineEventHandler, proxyRequest} from 'h3'
import {useRuntimeConfig} from '#imports'

/**
 * Whether a path belongs to the backend.
 *
 * @param path the request path
 * @returns true for everything below `/api/`
 */
function belongsToBackend(path: string): boolean {
    return path.startsWith('/api/')
}

/**
 * Hands the paths the backend owns straight to it, at the address this server holds while it runs.
 *
 * <p>The address is read per request, so one build serves any backend. Everything else falls
 * through untouched: returning nothing from a middleware lets the request carry on to the pages.
 *
 * <p>This covers HTTP only. A WebSocket upgrade never reaches a middleware: Nitro hands it to the
 * route that declares a WebSocket handler for the path, which is where the editor stream is relayed
 * (`server/routes/api/v1/editor/sessions/[id]/stream.ts`).
 */
export default defineEventHandler(event => {
    if (!belongsToBackend(event.path)) return

    const {backendUrl} = useRuntimeConfig(event)
    return proxyRequest(event, `${backendUrl}${event.path}`)
})
