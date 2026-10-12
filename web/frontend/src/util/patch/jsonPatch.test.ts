/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import {reactive} from 'vue'
import {applyPatch, type PatchOperation} from '~/util/patch/jsonPatch'
import {PatchError, pointer, segments} from '~/util/patch/jsonPointer'

describe('applyPatch', () => {
    it.each<[unknown, PatchOperation[], unknown]>([
        [{a: 1}, [{op: 'add', path: '/b', value: 2}], {a: 1, b: 2}],
        [{a: [1, 3]}, [{op: 'add', path: '/a/1', value: 2}], {a: [1, 2, 3]}],
        [{a: [1]}, [{op: 'add', path: '/a/-', value: 2}], {a: [1, 2]}],
        [{a: 1, b: 2}, [{op: 'remove', path: '/a'}], {b: 2}],
        [{a: 1}, [{op: 'replace', path: '/a', value: 5}], {a: 5}],
        [{a: 1}, [{op: 'replace', path: '', value: {z: 0}}], {z: 0}],
        [{a: {x: 1}, b: {}}, [{op: 'move', from: '/a/x', path: '/b/y'}], {a: {}, b: {y: 1}}],
        [{a: {x: 1}}, [{op: 'copy', from: '/a', path: '/b'}], {a: {x: 1}, b: {x: 1}}],
        [{'a/b': {'c~d': 1}}, [{op: 'replace', path: '/a~1b/c~0d', value: 2}], {'a/b': {'c~d': 2}}],
    ])('follows the RFC for %j', (document, operations, expected) => {
        expect(applyPatch(document, operations)).toEqual(expected)
    })

    it.each<[unknown, PatchOperation[]]>([
        [{a: 1}, [{op: 'remove', path: '/b'}]],
        [{a: [1]}, [{op: 'add', path: '/a/5', value: 1}]],
        [{a: [1]}, [{op: 'add', path: '/a/01', value: 1}]],
        [{a: 1}, [{op: 'test', path: '/a', value: 2}]],
        [{a: {}}, [{op: 'move', from: '/a', path: '/a/b'}]],
    ])('refuses what does not fit %j', (document, operations) => {
        expect(() => applyPatch(document, operations)).toThrow(PatchError)
    })

    it('patches reactive documents', () => {
        const document = reactive({a: {b: 1}})
        expect(applyPatch(document, [{op: 'replace', path: '/a/b', value: reactive({c: 2})}])).toEqual({a: {b: {c: 2}}})
    })

    it('leaves the document unchanged', () => {
        const document = {a: 1}
        expect(() => applyPatch(document, [{op: 'add', path: '/b', value: 2}, {op: 'remove', path: '/c'}])).toThrow()
        expect(document).toEqual({a: 1})
    })
})

describe('pointer', () => {
    it('escapes and splits segments', () => {
        expect(pointer('mobs', 'a/b', 'c~d', 0)).toBe('/mobs/a~1b/c~0d/0')
        expect(segments('/mobs/a~1b/c~0d/0')).toEqual(['mobs', 'a/b', 'c~d', '0'])
        expect(segments('')).toEqual([])
    })
})
