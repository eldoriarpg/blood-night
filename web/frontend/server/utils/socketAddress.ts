/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/** The close code sent on when the other side gave one a WebSocket client may not send itself. */
export const NORMAL_CLOSURE = 1000

/**
 * The WebSocket address of the backend for the path and query of an upgrade request, appended to the
 * backend address the way the HTTP proxy appends a path.
 *
 * @param backendUrl the backend's HTTP address
 * @param requestUrl the address the browser asked for, absolute or not
 * @returns the same path and query at the backend, as a `ws:` or `wss:` address
 */
export function upstreamAddress(backendUrl: string, requestUrl: string): string {
    const requested = new URL(requestUrl, 'http://localhost')
    const target = new URL(`${backendUrl.replace(/\/$/, '')}${requested.pathname}${requested.search}`)
    target.protocol = target.protocol === 'https:' ? 'wss:' : 'ws:'
    return target.toString()
}

/**
 * A close code the relay may pass on through a WebSocket client: 1000 and the range 3000 to 4999.
 * Anything else, a missing code included, is sent on as a normal closure.
 *
 * @param code the code the other side closed with
 * @returns the code to close with
 */
export function relayableCloseCode(code: number | undefined): number {
    if (code === NORMAL_CLOSURE) return code
    if (code !== undefined && code >= 3000 && code <= 4999) return code
    return NORMAL_CLOSURE
}
