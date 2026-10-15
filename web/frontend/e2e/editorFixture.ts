/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Page, WebSocketRoute} from '@playwright/test'

/** The session every editor story opens, the participant's link, and what the stories inspect. */
export interface EditorStage {
    link: string
    changes: unknown[][]
    submits: number
    stream(): WebSocketRoute
}

const SESSION = 's1'

/** A one pixel PNG standing in for every item icon. */
const PIXEL = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==', 'base64')

const catalog = {
    format: 1,
    version: 'e2e',
    selectable: {boolean: 'boolean', integer: 'int', text: 'java.lang.String', color: 'org.bukkit.Color'},
    bindings: {},
    types: [
        {id: 'int', name: 'Integer', raw: 'int', literal: true},
        {id: 'boolean', name: 'Boolean', raw: 'boolean', literal: true},
        {id: 'org.bukkit.Color', name: 'Color', raw: 'org.bukkit.Color', literal: true, format: 'color'},
    ],
    nodes: [
        {id: 'mob.tick', kind: 'entry', category: 'entry', name: 'Every tick', description: '', macroOnly: false, executions: [{id: 'next', name: ''}]},
        {
            id: 'creeper.ignite', kind: 'action', category: 'entity', name: 'Ignite', description: 'Lights the fuse.', macroOnly: false, executions: [{id: 'next', name: ''}],
            inputs: [{id: 'fuse', name: 'fuse', description: 'Ticks until the creeper explodes.', type: {id: 'int', arguments: []}, optional: false, literal: true}],
        },
        {
            id: 'value', kind: 'value', category: 'value', name: 'Value', description: 'A fixed value of the picked type', macroOnly: false,
            config: [
                {id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'boolean', selects: 'T', choices: ['boolean', 'integer', 'color']},
                {id: 'value', name: 'Value', type: {variable: 'T', arguments: []}},
            ],
            outputs: [{id: 'value', name: 'Value', description: 'The value typed in', type: {variable: 'T', arguments: []}}],
        },
        {
            id: 'color.rgb', kind: 'pure', category: 'value', name: 'RGB', description: 'A color from red, green and blue', macroOnly: false,
            inputs: ['red', 'green', 'blue'].map(id => ({id, name: id, description: '', type: {id: 'int', arguments: []}, optional: false, literal: true})),
            outputs: [{id: 'result', name: 'result', description: '', type: {id: 'org.bukkit.Color', arguments: []}}],
        },
        {
            id: 'reroute', kind: 'pure', category: 'layout', name: 'Reroute', description: '', macroOnly: false,
            inputs: [{id: 'value', name: 'value', description: '', type: {variable: 'T', arguments: []}, optional: false, literal: false}],
            outputs: [{id: 'value', name: 'value', description: '', type: {variable: 'T', arguments: []}}],
        },
        {
            id: 'creeper.read', kind: 'pure', category: 'entity', name: 'Read Creeper', description: 'Reads the properties of a Creeper', macroOnly: false,
            inputs: [{id: 'target', name: 'Creeper', description: '', type: {id: 'org.bukkit.entity.Creeper', arguments: []}, optional: false, literal: false}],
            outputs: [
                {id: 'fuse_ticks', name: 'Fuse Ticks', description: '', type: {id: 'int', arguments: []}, group: 'Creeper'},
                {id: 'powered', name: 'Powered', description: '', type: {id: 'boolean', arguments: []}, group: 'Creeper'},
                {id: 'health', name: 'Health', description: '', type: {id: 'double', arguments: []}, group: 'Living Entity'},
            ],
        },
        {
            id: 'creeper.mutate', kind: 'action', category: 'entity', name: 'Mutate Creeper', description: 'Changes the properties of a Creeper', macroOnly: false, executions: [{id: 'next', name: ''}],
            inputs: [
                {id: 'target', name: 'Creeper', description: '', type: {id: 'org.bukkit.entity.Creeper', arguments: []}},
                {id: 'max_fuse_ticks', name: 'Max Fuse Ticks', description: '', type: {id: 'int', arguments: []}, optional: true, literal: true, group: 'Creeper'},
                {id: 'maximum_no_damage_ticks', name: 'Maximum No Damage Ticks', description: '', type: {id: 'int', arguments: []}, optional: true, literal: true, group: 'Living Entity'},
                {id: 'velocity', name: 'Velocity', description: '', type: {id: 'org.bukkit.util.Vector', arguments: []}, optional: true, group: 'Entity'},
                {id: 'glowing', name: 'Glowing', description: '', type: {id: 'boolean', arguments: []}, optional: true, literal: true, group: 'Entity'},
            ],
            outputs: [
                {id: 'despawn_in_peaceful_override', name: 'Despawn In Peaceful Override', description: '', type: {id: 'boolean', arguments: []}, group: 'Mob'},
                {id: 'maximum_no_damage_ticks', name: 'Maximum No Damage Ticks', description: '', type: {id: 'int', arguments: []}, group: 'Living Entity'},
            ],
        },
        {
            id: 'flow.switch', kind: 'control_flow', category: 'flow', name: 'Switch', description: 'Runs the chain of the case matching the value', macroOnly: false,
            config: [
                {id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'integer', selects: 'T', choices: ['integer', 'text']},
                {id: 'case_0', name: 'Case 1', type: {variable: 'T', arguments: []}, defaultValue: null, beside: 'case_0'},
                {id: 'case_1', name: 'Case 2', type: {variable: 'T', arguments: []}, defaultValue: null, beside: 'case_1'},
            ],
            inputs: [{id: 'value', name: 'value', description: '', type: {variable: 'T', arguments: []}, optional: false, literal: false}],
            executions: [{id: 'case_0', name: 'Case 1'}, {id: 'case_1', name: 'Case 2'}, {id: 'default', name: 'default'}, {id: 'next', name: 'next'}],
        },
        ...['variable.get', 'variable.set'].map(id => ({
            id, kind: id === 'variable.get' ? 'pure' : 'action', category: 'variable', name: id === 'variable.get' ? 'Get Variable' : 'Set Variable', description: '', macroOnly: false,
            config: [
                {id: 'name', name: 'Name', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'variable'},
                {id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'boolean', selects: 'T', choices: ['boolean', 'integer', 'text']},
            ],
            outputs: [{id: 'value', name: 'Value', description: '', type: {variable: 'T', arguments: []}}],
        })),
        {
            id: 'routine.define', kind: 'entry', category: 'routine', name: 'Routine', description: 'Where a routine starts', macroOnly: false, executions: [{id: 'next', name: ''}],
            config: [{id: 'name', name: 'Name', type: {id: 'java.lang.String', arguments: []}}],
        },
        {
            id: 'routine.parameter', kind: 'value', category: 'routine', name: 'Parameter', description: 'A parameter of a routine', macroOnly: false,
            config: [
                {id: 'type', name: 'Type', type: {id: 'java.lang.String', arguments: []}, defaultValue: 'boolean', selects: 'T', choices: ['boolean', 'integer', 'text']},
                {id: 'routine', name: 'Routine', type: {id: 'java.lang.String', arguments: []}},
                {id: 'name', name: 'Name', type: {id: 'java.lang.String', arguments: []}},
            ],
            outputs: [{id: 'value', name: 'Value', description: '', type: {variable: 'T', arguments: []}}],
        },
        {id: 'reroute.execution', kind: 'action', category: 'layout', name: 'Reroute', description: '', macroOnly: false, executions: [{id: 'next', name: ''}]},
    ],
}

const payload = {
    catalog: 'e2e',
    mobs: {
        speed_creeper: {
            id: 'speed_creeper',
            attributes: {name: {style: '', texts: {en: 'Speed Creeper', de: 'Flinker Creeper'}}, entityType: 'CREEPER', exactType: true, healthModifier: 'DEFAULT', health: 1, damageModifier: 'DEFAULT', damage: 1, changes: []},
            behaviour: {nodes: {
                tick: {type: 'mob.tick', executions: {next: ['ignite']}, editor: {x: 0, y: 0}},
                ignite: {type: 'creeper.ignite', editor: {x: 420, y: 0}},
            }},
            extension: {type: 'NONE'},
            spawn: {weight: 10, nightOnly: true, despawn: 'NIGHT_END', reasons: [], conditions: {worlds: [], biomes: [], surface: 'ANY'}},
        },
        zz_dragon: {
            id: 'zz_dragon',
            attributes: {name: {style: '', texts: {en: 'Blood Dragon'}}, entityType: 'ENDER_DRAGON', exactType: true, healthModifier: 'DEFAULT', health: 1, damageModifier: 'DEFAULT', damage: 1, changes: []},
            behaviour: {nodes: {}},
            extension: {type: 'NONE'},
            spawn: {weight: 0, nightOnly: true, despawn: 'NIGHT_END', reasons: [], conditions: {worlds: [], biomes: [], surface: 'ANY'}},
        },
        z_painter: {
            id: 'z_painter',
            attributes: {name: {style: '', texts: {en: 'Painter'}}, entityType: 'ZOMBIE', exactType: true, healthModifier: 'DEFAULT', health: 1, damageModifier: 'DEFAULT', damage: 1, changes: []},
            behaviour: {nodes: {
                amount: {type: 'value', editor: {x: 0, y: 0}},
                paint: {type: 'color.rgb', literals: {red: 255, green: 0, blue: 0}, editor: {x: 0, y: 220}},
                look: {type: 'creeper.read', editor: {x: 440, y: 0}},
                pick: {type: 'flow.switch', config: {case_0: 3}, editor: {x: 900, y: 0}},
                count: {type: 'variable.set', config: {name: 'hits', type: 'integer'}, editor: {x: 900, y: 400}},
                read: {type: 'variable.get', editor: {x: 1300, y: 400}},
                param: {type: 'routine.parameter', config: {routine: 'knockback', name: 'power', type: 'integer'}, editor: {x: 1300, y: 0}},
                start: {type: 'routine.define', config: {name: 'knockback'}, editor: {x: 1700, y: 0}},
                change: {
                    type: 'creeper.mutate', literals: {max_fuse_ticks: 30, maximum_no_damage_ticks: 10},
                    inputs: {velocity: {node: 'look', port: 'health'}},
                    editor: {x: 440, y: 260},
                },
            }},
            extension: {type: 'NONE'},
            spawn: {weight: 10, nightOnly: true, despawn: 'NIGHT_END', reasons: [], conditions: {worlds: [], biomes: [], surface: 'ANY'}},
        },
    },
    macros: [],
    items: {
        guard_helmet: {kind: 'REGISTERED', material: 'minecraft:iron_helmet', lore: ['<gray>Worn by the night watch'], enchantments: {'minecraft:protection': 2}},
    },
    choices: {minecraftVersion: '26.3', materials: ['minecraft:iron_helmet', 'minecraft:golden_axe'], enchantments: ['minecraft:protection', 'minecraft:sharpness'], mobTypes: ['CREEPER', 'SKELETON', 'ZOMBIE'], attributes: ['minecraft:max_health'], biomes: ['#minecraft:is_ocean', 'minecraft:desert'], effects: ['minecraft:darkness', 'minecraft:nausea'], gameRules: {'minecraft:spawn_phantoms': {type: 'boolean', translationKey: 'gamerule.doInsomnia'}, 'minecraft:max_entity_cramming': {type: 'integer', translationKey: 'gamerule.maxEntityCramming'}}, commands: ['home', 'spawn', 'tpa']},
    problems: {},
    macroProblems: {},
    configProblems: {},
    settings: {
        main: {announcements: {prefix: '[BN] '}, resourcePack: {offer: true}, setup: {done: false}},
        globalDrops: {},
        worlds: {
            w1: {name: 'world', environment: 'NORMAL', dayCycle: true, settings: {enabled: true, nightSettings: {begin: 13000, end: 23000}, nightSelection: {type: 'RANDOM', moonPhase: {}, phase: {}}, mobSettings: {}, soundSettings: {}, nightRules: {}, worldDrops: {}, atmosphere: {sky: true, heartbeat: true}}},
            w2: {name: 'world_nether', environment: 'NETHER', dayCycle: false, settings: {enabled: true, nightSettings: {begin: 12000, end: 22000}, nightSelection: {type: 'RANDOM'}, mobSettings: {spawning: {}}, atmosphere: {sky: true, heartbeat: true}}},
            w3: {name: 'world_the_end', environment: 'THE_END', dayCycle: false, settings: {enabled: false, nightSettings: {begin: 13000, end: 23000}, nightSelection: {type: 'RANDOM'}, mobSettings: {}, atmosphere: {sky: true, heartbeat: true}}},
        },
    },
    hashes: {},
    store: {
        packs: [{id: 'classic', names: {en: 'Classic'}, descriptions: {en: 'The mobs of the old blood night.'}}],
        offers: [{
            id: 'night_hound', pack: 'classic', names: {en: 'Night Hound'}, descriptions: {en: 'A wolf that hunts in packs.'}, requires: [], hash: 'h1',
            mob: {
                id: 'night_hound',
                attributes: {name: {style: '', texts: {en: 'Night Hound'}}, entityType: 'ZOMBIE', exactType: true, healthModifier: 'DEFAULT', health: 1, damageModifier: 'DEFAULT', damage: 1, changes: []},
                behaviour: {nodes: {}},
                extension: {type: 'NONE'},
                spawn: {weight: 10, nightOnly: true, despawn: 'NIGHT_END', reasons: [], conditions: {worlds: [], biomes: [], surface: 'ANY'}},
                origin: {store: 'bloodnight', entry: 'night_hound', hash: 'h1'},
            },
        }],
        states: {},
    },
}

function withoutPositions(): typeof payload {
    const copy = JSON.parse(JSON.stringify(payload)) as typeof payload
    for (const node of Object.values(copy.mobs.speed_creeper.behaviour.nodes) as {editor?: unknown}[]) delete node.editor
    return copy
}

/**
 * Stands in for the editor service: the session, its catalog and its stream, answering changes with the next
 * revision and reporting them on the stream as the service does.
 *
 * @param page the page
 * @param holders sections other participants hold, by section
 * @param unplaced whether the mob's nodes come without saved positions, as fresh default mobs do
 * @param setup whether the session was opened by the setup command
 * @returns the stage
 */
export async function stageEditor(page: Page, holders: Record<string, string> = {}, unplaced = false, setup = false): Promise<EditorStage> {
    const changes: unknown[][] = []
    let socket: WebSocketRoute | null = null
    let revision = 0
    const locks = Object.entries(holders).map(([section, holder]) => ({section, holder}))
    const stage: EditorStage = {
        link: `/session/${SESSION}#token`,
        changes,
        submits: 0,
        stream: () => {
            if (!socket) throw new Error('No stream yet')
            return socket
        },
    }
    await page.route(`**/api/v1/editor/sessions/${SESSION}`, route => route.fulfill({json: {
        id: SESSION, state: 'EDITING', revision, lastApplied: null, catalog: 'e2e', payload: {...unplaced ? withoutPositions() : payload, setup}, problems: [], locks, online: ['Steve', ...Object.values(holders)], you: 'Steve', areas: ['MOBS', 'MACROS', 'ITEMS', 'WORLDS', 'SERVER', 'DROPS'], expires: '2026-10-20T00:00:00Z',
    }}))
    await page.route('**/api/v1/editor/catalogs/e2e', route => route.fulfill({json: catalog}))
    await page.route(`**/api/v1/editor/sessions/${SESSION}/locks`, route => route.fulfill({status: 204}))
    await page.route('**/api/v1/assets/26.3/items', route => route.fulfill({json: {version: '26.3', items: {
        'minecraft:golden_axe': {texture: 'item/golden_axe', names: {en_us: 'Golden Axe', de_de: 'Goldaxt'}},
        'minecraft:iron_helmet': {texture: 'item/iron_helmet', names: {en_us: 'Iron Helmet', de_de: 'Eisenhelm'}},
    }, names: {
        enchantment: {'minecraft:protection': {en_us: 'Protection', de_de: 'Schutz'}},
        entity_type: {'minecraft:creeper': {en_us: 'Creeper', de_de: 'Creeper'}, 'minecraft:zombie': {en_us: 'Zombie', de_de: 'Zombie'}},
        effect: {'minecraft:darkness': {en_us: 'Darkness', de_de: 'Dunkelheit'}, 'minecraft:nausea': {en_us: 'Nausea', de_de: 'Übelkeit'}},
        biome: {'minecraft:desert': {en_us: 'Desert', de_de: 'Wüste'}},
        game_rule: {'minecraft:doInsomnia': {en_us: 'Spawn phantoms', de_de: 'Phantome erscheinen'}, 'minecraft:maxEntityCramming': {en_us: 'Entity cramming threshold', de_de: 'Gedrängegrenze'}},
    }}}))
    await page.route('**/api/v1/assets/26.3/textures/**', route => route.fulfill({contentType: 'image/png', body: PIXEL}))
    await page.route('**/api/v1/assets/26.3/sounds', route => route.fulfill({json: {version: '26.3', sounds: {
        'entity.lightning_bolt.thunder': {group: 'entity', subtitles: {en_us: 'Thunder roars', de_de: 'Donner grollt'}, playable: true},
        'entity.zombie.ambient': {group: 'entity', subtitles: {en_us: 'Zombie groans', de_de: 'Zombie stöhnt'}, playable: true},
        'ambient.basalt_deltas.additions': {group: 'ambient', subtitles: {}, playable: true},
    }}}))
    await page.route('**/api/v1/assets/26.3/sounds/*.ogg', route => route.fulfill({contentType: 'audio/ogg', body: Buffer.from('OggS')}))
    await page.route(`**/api/v1/editor/sessions/${SESSION}/changes`, async route => {
        const operations = (route.request().postDataJSON() as {operations: unknown[]}).operations
        changes.push(operations)
        revision++
        await route.fulfill({json: {revision, problems: []}})
        socket?.send(JSON.stringify({type: 'CHANGE', revision, author: 'Steve', operations}))
    })
    await page.route(`**/api/v1/editor/sessions/${SESSION}/submit`, async route => {
        stage.submits++
        await route.fulfill({status: 204})
    })
    await page.routeWebSocket(/\/stream\?token=/, ws => {
        socket = ws
        ws.send(JSON.stringify({type: 'HELLO', revision, participants: ['Steve', ...Object.values(holders)], locks}))
    })
    return stage
}
