/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** What a form field edits. */
export type FieldKind = 'boolean' | 'integer' | 'number' | 'text' | 'choice' | 'choices' | 'texts' | 'group' | 'map' | 'list' | 'any'

/** One field of a settings form, built from the payload schema. */
export interface Field {
    key: string
    kind: FieldKind
    options: string[]
    children: Field[]
    item: Field | undefined
    defaultValue: unknown
    /** For a choice or a set of choices: the enum's definition name, e.g. `SpawnReason`. */
    enumName: string | undefined
}

interface Schema {
    $ref?: string
    allOf?: Schema[]
    type?: string
    enum?: string[]
    items?: Schema
    properties?: Record<string, Schema>
    additionalProperties?: Schema | boolean
    uniqueItems?: boolean
    default?: unknown
}

/** A schema document with its definitions, as `payload.schema.json` is. */
export interface SchemaDocument {
    $defs: Record<string, Schema>
}

const DEPTH = 8

/**
 * The fields of a definition of the payload schema: switches for booleans, numbers, texts, choices for enums, lists
 * of texts, lists of choices for sets of enums, groups for nested settings, key and value rows for maps, and repeated
 * groups for lists of settings. Anything else is edited as JSON.
 *
 * @param document the payload schema
 * @param definition the definition's name, e.g. `NightSettings`
 * @returns the definition's fields in declaration order
 */
export function fieldsOf(document: SchemaDocument, definition: string): Field[] {
    return properties(document, document.$defs[definition] ?? {}, 0)
}

function properties(document: SchemaDocument, schema: Schema, depth: number): Field[] {
    const resolved = resolve(document, schema)
    return Object.entries(resolved.properties ?? {}).map(([key, property]) => field(document, key, property, depth))
}

function field(document: SchemaDocument, key: string, schema: Schema, depth: number): Field {
    const resolved = resolve(document, schema)
    const base: Field = {key, kind: 'any', options: [], children: [], item: undefined, defaultValue: schema.default ?? resolved.default, enumName: undefined}
    if (depth > DEPTH) return base
    if (resolved.enum) return {...base, kind: 'choice', options: resolved.enum, enumName: definitionName(schema)}
    switch (resolved.type) {
        case 'boolean': return {...base, kind: 'boolean'}
        case 'integer': return {...base, kind: 'integer'}
        case 'number': return {...base, kind: 'number'}
        case 'string': return {...base, kind: 'text'}
        case 'array': return array(document, base, resolved, depth)
        case 'object': return object(document, base, resolved, depth)
        default: return base
    }
}

function array(document: SchemaDocument, base: Field, schema: Schema, depth: number): Field {
    const item = resolve(document, schema.items ?? {})
    if (item.enum) return {...base, kind: 'choices', options: item.enum, enumName: definitionName(schema.items ?? {})}
    if (item.type === 'string') return {...base, kind: 'texts'}
    if (item.properties) return {...base, kind: 'list', item: {...field(document, 'item', schema.items ?? {}, depth + 1), key: 'item'}}
    return base
}

function object(document: SchemaDocument, base: Field, schema: Schema, depth: number): Field {
    if (schema.properties) return {...base, kind: 'group', children: properties(document, schema, depth + 1)}
    if (typeof schema.additionalProperties === 'object') {
        return {...base, kind: 'map', item: field(document, 'value', schema.additionalProperties, depth + 1)}
    }
    return base
}

function definitionName(schema: Schema): string | undefined {
    if (schema.$ref) return schema.$ref.replace('#/$defs/', '')
    return schema.allOf?.length === 1 && schema.allOf[0] ? definitionName(schema.allOf[0]) : undefined
}

function resolve(document: SchemaDocument, schema: Schema): Schema {
    if (schema.$ref) return resolve(document, document.$defs[schema.$ref.replace('#/$defs/', '')] ?? {})
    if (schema.allOf?.length === 1 && schema.allOf[0]) return {...resolve(document, schema.allOf[0]), default: schema.default}
    return schema
}
