/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import type {EditorPayload} from '~/api/generated/payload'
import {catalog, graph} from '~/util/graph/graphFixtures'
import {boundType, mobBindings} from '~/util/graph/graphBindings'
import {graphView} from '~/util/graph/graphView'

const LIVING = 'org.bukkit.entity.LivingEntity'
const CREEPER = 'org.bukkit.entity.Creeper'
const bound = {
    ...catalog,
    bindings: {SELF: LIVING},
    types: [{id: CREEPER, name: 'Creeper', raw: CREEPER, arguments: [], literal: false, values: [], assignableTo: [LIVING]}],
    nodes: [...catalog.nodes, {
        id: 'mob.self', kind: 'pure', category: 'mob', name: 'Self', description: '', macroOnly: false, inputs: [], executions: [], config: [],
        outputs: [{id: 'entity', name: 'entity', description: '', type: {variable: 'SELF', arguments: []}, optional: false, literal: false}],
    }],
}

describe('graph bindings', () => {
    it('takes the graph binding, else the catalog fallback', () => {
        expect(boundType({variable: 'SELF', arguments: []}, bound, {SELF: CREEPER})?.id).toBe(CREEPER)
        expect(boundType({variable: 'SELF', arguments: []}, bound, {})?.id).toBe(LIVING)
        expect(boundType({variable: 'T', arguments: []}, bound, {})?.variable).toBe('T')
    })

    it('binds Self and Extension of a mob from the server entity classes', () => {
        const payload = {
            choices: {entityClasses: {CREEPER, VEX: 'org.bukkit.entity.Vex'}},
            mobs: {ghost: {attributes: {entityType: 'CREEPER'}, extension: {type: 'RIDER', entityType: 'VEX'}}},
        } as unknown as EditorPayload
        expect(mobBindings(payload, 'ghost')).toEqual({SELF: CREEPER, EXTENSION: 'org.bukkit.entity.Vex'})
        expect(mobBindings(payload, 'missing')).toEqual({})
    })

    it('draws Self as the mob type', () => {
        const start = graph()
        const nodes = graphView({nodes: {...start.nodes, me: {type: 'mob.self'}}}, bound, [], {SELF: CREEPER}).nodes
        const self = nodes.find(node => node.id === 'me')
        expect(self?.title).toBe('Self: Creeper')
        expect(self?.outputs[0]).toMatchObject({typeName: 'Creeper', pin: 'entity'})
    })
})
