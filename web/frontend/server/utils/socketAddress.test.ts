/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment node */
import {describe, expect, it} from 'vitest'
import {NORMAL_CLOSURE, relayableCloseCode, upstreamAddress} from './socketAddress'

/** Where the relay connects to, and which close codes it can pass on. */
describe('the editor stream relay', () => {
    it('keeps the path and the token of the request at the backend', () => {
        expect(upstreamAddress('http://localhost:8080', '/api/v1/editor/sessions/abc/stream?token=t1'))
            .toBe('ws://localhost:8080/api/v1/editor/sessions/abc/stream?token=t1')
    })

    it('reads an absolute request address the same way', () => {
        expect(upstreamAddress('http://backend:8080', 'http://localhost:3000/api/v1/editor/sessions/abc/stream?token=t1'))
            .toBe('ws://backend:8080/api/v1/editor/sessions/abc/stream?token=t1')
    })

    it('keeps a path the backend address carries', () => {
        expect(upstreamAddress('http://host/editor/', '/api/v1/editor/sessions/abc/stream'))
            .toBe('ws://host/editor/api/v1/editor/sessions/abc/stream')
    })

    it('speaks wss to a backend behind https', () => {
        expect(upstreamAddress('https://editor.example.org', '/api/v1/editor/sessions/abc/stream'))
            .toBe('wss://editor.example.org/api/v1/editor/sessions/abc/stream')
    })

    it('passes on the codes an application closes with', () => {
        expect(relayableCloseCode(4401)).toBe(4401)
        expect(relayableCloseCode(NORMAL_CLOSURE)).toBe(NORMAL_CLOSURE)
    })

    it('sends a normal closure for codes a client may not send', () => {
        expect(relayableCloseCode(1001)).toBe(NORMAL_CLOSURE)
        expect(relayableCloseCode(1006)).toBe(NORMAL_CLOSURE)
        expect(relayableCloseCode(undefined)).toBe(NORMAL_CLOSURE)
    })
})
