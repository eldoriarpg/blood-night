/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'

/** A small catalog: an entry, a branch with an entity and a number input, an action and an entity value. */
export const catalog: Catalog = {
    format: 1,
    version: 'test',
    selectable: {},
    bindings: {},
    types: [],
    nodes: [
        {id: 'mob.tick', kind: 'entry', category: 'entry', name: 'Every tick', description: '', macroOnly: false, inputs: [], outputs: [], executions: [{id: 'next', name: '', repeating: false}], config: []},
        {
            id: 'entity.is_near', kind: 'control_flow', category: 'logic', name: 'Target nearby', description: '', macroOnly: false,
            inputs: [
                {id: 'target', name: 'target', description: '', type: {id: 'org.bukkit.entity.LivingEntity', arguments: []}, optional: false, literal: false},
                {id: 'distance', name: 'distance', description: '', type: {id: 'double', arguments: []}, optional: false, literal: true},
            ],
            outputs: [],
            executions: [{id: 'then', name: 'then', repeating: false}, {id: 'else', name: 'else', repeating: false}],
            config: [],
        },
        {id: 'creeper.ignite', kind: 'action', category: 'function', name: 'Ignite', description: '', macroOnly: false, inputs: [], outputs: [], executions: [{id: 'next', name: '', repeating: false}], config: []},
        {
            id: 'mob.target', kind: 'pure', category: 'value', name: 'Current target', description: '', macroOnly: false, inputs: [],
            outputs: [{id: 'entity', name: 'entity', description: '', type: {id: 'org.bukkit.entity.LivingEntity', arguments: []}, optional: false, literal: false}],
            executions: [], config: [],
        },
    ],
}

/** Every tick, when the target is near, ignite. */
export function graph(): Graph {
    return {
        nodes: {
            tick: {type: 'mob.tick', executions: {next: ['near']}},
            near: {type: 'entity.is_near', inputs: {target: {node: 'target', port: 'entity'}}, literals: {distance: 4}, executions: {then: ['ignite']}},
            ignite: {type: 'creeper.ignite', editor: {x: 900, y: 40}},
            target: {type: 'mob.target'},
        },
    }
}
