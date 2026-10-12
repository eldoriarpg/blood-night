/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {PatchError, segments} from '~/util/patch/jsonPointer'

type Json = null | boolean | number | string | Json[] | {[key: string]: Json}

/** One JSON Patch operation (RFC 6902), as the editor service takes them. */
export type PatchOperation =
    | {op: 'add' | 'replace' | 'test', path: string, value: unknown}
    | {op: 'remove', path: string}
    | {op: 'move' | 'copy', from: string, path: string}

/**
 * Applies operations to a copy of a document, all or none, exactly as the editor service does, so a change looks
 * the same before and after the service accepted it.
 *
 * @param document the document, left unchanged
 * @param operations the operations
 * @returns the patched copy
 * @throws PatchError when an operation does not fit
 */
export function applyPatch<T>(document: T, operations: readonly PatchOperation[]): T {
    let result = jsonClone(document) as Json
    operations.forEach((operation, index) => {
        try {
            result = applyOne(result, operation)
        } catch (error) {
            throw new PatchError(`Operation ${index}: ${(error as Error).message}`)
        }
    })
    return result as T
}

function applyOne(document: Json, operation: PatchOperation): Json {
    const path = segments(operation.path)
    switch (operation.op) {
        case 'add': return add(document, path, clone(operation.value))
        case 'remove': return remove(document, path)
        case 'replace':
            if (path.length === 0) return clone(operation.value)
            get(document, path)
            return add(remove(document, path), path, clone(operation.value))
        case 'move': {
            const from = segments(operation.from)
            if (path.length > from.length && from.every((segment, i) => path[i] === segment)) {
                throw new PatchError('Cannot move a value into itself')
            }
            const value = get(document, from)
            return add(remove(document, from), path, value)
        }
        case 'copy': return add(document, path, clone(get(document, segments(operation.from))))
        case 'test':
            if (JSON.stringify(get(document, path)) !== JSON.stringify(operation.value)) throw new PatchError('Test failed')
            return document
    }
}

function add(document: Json, path: string[], value: Json): Json {
    if (path.length === 0) return value
    const parent = container(get(document, path.slice(0, -1)))
    const key = path.at(-1) as string
    if (Array.isArray(parent)) {
        if (key === '-') parent.push(value)
        else parent.splice(index(key, parent.length), 0, value)
    } else {
        parent[key] = value
    }
    return document
}

function remove(document: Json, path: string[]): Json {
    if (path.length === 0) throw new PatchError('Cannot remove the whole document')
    const parent = container(get(document, path.slice(0, -1)))
    const key = path.at(-1) as string
    if (Array.isArray(parent)) {
        parent.splice(index(key, parent.length - 1), 1)
    } else {
        if (!(key in parent)) throw new PatchError(`Nothing at ${key}`)
        Reflect.deleteProperty(parent, key)
    }
    return document
}

function get(document: Json, path: string[]): Json {
    let node: Json | undefined = document
    for (const key of path) {
        if (Array.isArray(node)) node = node[index(key, node.length - 1)]
        else if (node !== null && typeof node === 'object') node = node[key]
        else node = undefined
        if (node === undefined) throw new PatchError(`Nothing at ${key}`)
    }
    return node
}

function container(node: Json): Json[] | {[key: string]: Json} {
    if (node !== null && typeof node === 'object') return node
    throw new PatchError('Not an object or list')
}

function index(key: string, max: number): number {
    const value = Number(key)
    if (!/^(0|[1-9][0-9]*)$/.test(key) || value > max) throw new PatchError(`Bad list index ${key}`)
    return value
}

function clone(value: unknown): Json {
    if (value === undefined) throw new PatchError('Missing value')
    return jsonClone(value) as Json
}

/**
 * A deep copy of a JSON value. Works on Vue's reactive proxies, which `structuredClone` refuses.
 *
 * @param value the value
 * @returns the copy
 */
export function jsonClone<T>(value: T): T {
    return value === undefined ? value : JSON.parse(JSON.stringify(value)) as T
}
