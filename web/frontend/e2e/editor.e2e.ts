/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {expect, test, type Locator, type Page} from '@playwright/test'
import {stageEditor} from './editorFixture'


/** Opens the session and the full graph editor of the mob shown first. */
async function openGraph(page: Page, link: string): Promise<void> {
    await page.goto(link)
    await page.getByRole('button', {name: 'Edit'}).click()
}

/**
 * Where an element ends up once the canvas has stopped fitting its view.
 *
 * @param element the element
 * @returns its box once two looks agree
 */
async function settledBox(element: Locator): Promise<{x: number, y: number, width: number, height: number}> {
    let last = await element.boundingBox()
    await expect.poll(async () => {
        const now = await element.boundingBox()
        const settled = now !== null && JSON.stringify(now) === JSON.stringify(last)
        last = now
        return settled
    }, {intervals: [100]}).toBe(true)
    if (!last) throw new Error('No element')
    return last
}

/**
 * Picks a choice of one of the editor's dropdowns.
 *
 * @param page the page
 * @param field the dropdown's trigger
 * @param option the name of the choice
 */
async function choose(page: Page, field: Locator, option: string): Promise<void> {
    await field.click()
    await page.getByRole('option', {name: option, exact: true}).click()
}

/** The editor opens a session and draws the open mob's graph. */
test('the editor shows the mob and its graph', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)

    await expect(page.getByRole('button', {name: 'speed_creeper'})).toBeVisible()
    await expect(page.locator('.vue-flow__node')).toHaveCount(2)
    await expect(page.getByText('Ignite').first()).toBeVisible()
    await expect(page.getByText('Saved, revision 0')).toBeVisible()
})

/** A changed setting is sent as one patch operation and the revision the stream reports is shown. */
test('editing a world setting saves a revision', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.getByRole('tab', {name: 'Night', exact: true}).click()

    await page.getByRole('slider', {name: 'Night begins at'}).focus()
    await page.keyboard.press('ArrowLeft')

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes).toEqual([[{op: 'add', path: '/settings/worlds/w1/settings/nightSettings/begin', value: 12750}]])
})

/** A world another participant holds is read only and cannot be picked as a copy target. */
test('a world someone else holds is read only', async ({page}) => {
    const stage = await stageEditor(page, {'settings/worlds/w2': 'Alex'})
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.getByRole('button', {name: /world_nether/}).click()

    await expect(page.getByText('Alex is editing these settings')).toBeVisible()
    await expect(page.getByRole('switch').first()).toBeDisabled()
})

/** Copying world settings sends one change replacing the chosen sections in the chosen worlds. */
test('world settings are copied to other worlds', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.getByRole('button', {name: 'Copy settings'}).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByRole('button', {name: 'Clear'}).click()
    await dialog.getByLabel('Night', {exact: true}).check()
    await dialog.getByLabel('world_the_end').check()
    await dialog.getByRole('button', {name: 'Copy', exact: true}).click()

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes).toEqual([[{op: 'add', path: '/settings/worlds/w3/settings/nightSettings', value: {begin: 13000, end: 23000}}]])
})

/** An item's name is saved as MiniMessage and an enchantment is added from the server's enchantments. */
test('an item takes a name and an enchantment', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Items'}).click()
    await expect(page.getByRole('group', {name: 'Enchantments'})).toContainText('Protection')
    await expect(page.getByRole('figure', {name: 'In game'})).toContainText('Protection II')

    const level = page.getByLabel('Level of minecraft:protection')
    await level.fill('-3')
    await level.blur()
    await expect(level).toHaveValue('1')
    await level.fill('9')
    await level.blur()
    await expect(level).toHaveValue('4')

    const name = page.getByLabel('Name', {exact: true})
    await name.fill('<gray>Guard Helmet')
    await name.blur()
    await expect(page.getByText('Saved, revision 3')).toBeVisible()
    await page.getByLabel('Enchantment, e.g. minecraft:sharpness').fill('minecraft:sharpness')
    await page.getByRole('group').getByRole('button', {name: 'Add', exact: true}).click()

    await expect(page.getByText('Saved, revision 4')).toBeVisible()
    expect(stage.changes).toEqual([
        [{op: 'add', path: '/items/guard_helmet/enchantments', value: {'minecraft:protection': 1}}],
        [{op: 'add', path: '/items/guard_helmet/enchantments', value: {'minecraft:protection': 4}}],
        [{op: 'add', path: '/items/guard_helmet/name', value: '<gray>Guard Helmet'}],
        [{op: 'add', path: '/items/guard_helmet/enchantments', value: {'minecraft:protection': 4, 'minecraft:sharpness': 1}}],
    ])
})

/** Only a part a mob has no settings for offers to set it up; a filled part keeps its settings. */
test('only unset parts of a mob offer to be set up', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    for (const part of ['Spawn rules', 'Equipment', 'Extension', 'Loot']) await page.getByRole('button', {name: part, exact: true}).click()

    await expect(page.getByRole('button', {name: 'Set up'})).toHaveCount(1)
    await page.getByRole('button', {name: 'Set up'}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes).toEqual([[{op: 'add', path: '/mobs/speed_creeper/equipment', value: {}}]])
})

/** A loot table is made by name, gets an item, shows its chances and rolls; its uses lead to where it is given. */
test('a loot table is made, filled, previewed and followed to its uses', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Loot'}).click()
    await expect(page.getByRole('heading', {name: 'Zombie scraps'})).toBeVisible()
    await expect(page.getByRole('button', {name: 'World world'})).toBeVisible()

    await page.getByRole('button', {name: 'New loot table'}).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel('Name').fill('Knight Gear')
    await expect(dialog.getByText('Mobs, worlds and the server refer to it as knight_gear.')).toBeVisible()
    await dialog.getByRole('button', {name: 'Create'}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toEqual([{op: 'add', path: '/lootTables/knight_gear', value: {name: 'Knight Gear', entries: []}}])

    await page.getByRole('button', {name: 'Add an item'}).click()
    const picker = page.getByRole('dialog', {name: 'Add an item'})
    await picker.getByLabel('Search by name, type, lore or enchantment').fill('golden axe')
    await picker.getByLabel('Search by name, type, lore or enchantment').press('Enter')
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes[1]).toEqual([{op: 'add', path: '/lootTables/knight_gear/entries/-', value: {item: 'minecraft:golden_axe', weight: 10, amount: {mode: 'RANGE', min: 1, max: 1, choices: {}}, repeatable: false}}])

    await expect(page.getByText('100 % of picks')).toBeVisible()
    await expect(page.getByText('Always one: this item does not stack.').last()).toBeVisible()
    await page.getByRole('button', {name: 'Roll one pick'}).click()
    await expect(page.getByText('1 × Golden Axe')).toBeVisible()

    await page.getByRole('button', {name: 'Zombie scraps'}).click()
    await page.getByRole('button', {name: 'World world'}).click()
    await expect(page.locator('main').getByRole('tab', {name: 'Loot', selected: true})).toBeVisible()
    await expect(page.locator('main').getByRole('button', {name: 'Zombie scraps', exact: true})).toBeVisible()
})

/** An item gets a trim, wear and attributes; the first attribute keeps the material's own, and the preview shows them. */
test('an item takes a trim, wear and attributes', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Items'}).click()
    const preview = page.getByRole('figure', {name: 'In game'})
    await expect(preview).toContainText('+2 Armor')

    await page.getByRole('button', {name: 'Trim pattern'}).click()
    await page.getByRole('option', {name: 'Silence'}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toEqual([{op: 'add', path: '/items/guard_helmet/trim', value: {pattern: 'minecraft:silence', material: 'minecraft:iron'}}])
    await expect(preview).toContainText('Silence Armor Trim')

    await page.getByRole('switch', {name: 'Unbreakable'}).click()
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    await expect(preview).toContainText('Unbreakable')

    await page.getByRole('button', {name: 'Add a modifier'}).click()
    await expect(page.getByText('Saved, revision 3')).toBeVisible()
    expect(stage.changes[2]).toEqual([{op: 'add', path: '/items/guard_helmet/attributes', value: [
        {attribute: 'minecraft:armor', amount: 2, operation: 'ADD_NUMBER', slot: 'HEAD'},
        {attribute: 'minecraft:max_health', amount: 1, operation: 'ADD_NUMBER', slot: 'MAINHAND'},
    ]}])

    await page.getByRole('checkbox', {name: 'Attributes'}).click()
    await expect(page.getByText('Saved, revision 4')).toBeVisible()
    await expect(preview).not.toContainText('+2 Armor')
})

/** A new item is made from a material and gets its own section. */
test('an item is created from a material', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Items'}).click()
    await page.getByRole('button', {name: 'New item'}).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel('Id').fill('knight_axe')
    await dialog.getByRole('button', {name: 'Material'}).click()
    const picker = page.getByRole('dialog', {name: 'Material'})
    await picker.getByLabel('Search by name, type, lore or enchantment').fill('gold')
    await expect(picker.getByRole('option')).toHaveCount(1)
    await expect(picker.getByRole('option', {name: /Golden Axe/}).locator('img')).toHaveAttribute('src', '/api/v1/assets/26.3/textures/item/golden_axe.png')
    await picker.getByLabel('Search by name, type, lore or enchantment').press('Enter')
    await expect(dialog.getByRole('button', {name: 'Material'})).toContainText('Golden Axe')
    await dialog.getByRole('button', {name: 'Create'}).click()

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes).toEqual([[{op: 'add', path: '/items/knight_axe', value: {kind: 'DEFINED', material: 'minecraft:golden_axe', lore: [], enchantments: {}, unbreakable: false, attributes: [], hidden: []}}]])
})

/** A wire let go on the empty canvas offers the nodes that take it and adds the one picked, wired. */
test('a pulled wire adds a wired node', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    const pin = page.locator('.vue-flow__node[data-id="tick"] [data-handleid="exec:next"]')
    const box = await settledBox(pin)
    await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2)
    await page.mouse.down()
    await page.mouse.move(box.x + 120, box.y + 200, {steps: 8})
    await page.mouse.up()

    const picker = page.getByRole('dialog', {name: 'Nodes for this wire'})
    await expect(picker.getByRole('button', {name: 'Ignite'})).toBeVisible()
    await expect(picker.getByRole('button', {name: 'Every tick'})).toHaveCount(0)
    await picker.getByRole('button', {name: 'Ignite'}).click()

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    await expect(page.locator('.vue-flow__edge')).toHaveCount(2)
    const operations = stage.changes[0] as {path: string, value?: unknown}[]
    expect(operations.map(operation => operation.path)).toEqual([
        '/mobs/speed_creeper/behaviour/nodes/ignite_1',
        '/mobs/speed_creeper/behaviour/nodes/tick/executions/next/-',
    ])
})

/** A single click selects a node; a double click opens its details. */
test('a double click opens a node', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    const node = page.locator('.vue-flow__node[data-id="ignite"]')
    const header = node.getByText('Ignite', {exact: true})
    await header.click()
    await expect(page.getByText('Lights the fuse.')).toHaveCount(0)
    await header.dblclick()
    await expect(page.getByText('Lights the fuse.')).toBeVisible()
})

/** Resting the pointer on a pin shows its type and documentation. */
test('a pin shows its documentation', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.locator('.vue-flow__node[data-id="ignite"]').getByText('fuse', {exact: true}).hover()

    const tooltip = page.getByTestId('port-tooltip')
    await expect(tooltip).toContainText('Integer')
    await expect(tooltip).toContainText('Ticks until the creeper explodes.')
})

/** The item list finds items by type, lore and enchantment, every word matching something. */
test('items are found by type, lore and enchantment', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Items'}).click()
    const list = page.getByRole('navigation', {name: 'Items'})
    const search = list.getByLabel('Search by name, type, lore or enchantment')
    const helmet = list.getByRole('button', {name: /guard_helmet/})

    await expect(helmet).toContainText('Iron Helmet · Protection 2')
    for (const query of ['iron helmet', 'night watch', 'protection', 'helmet protection']) {
        await search.fill(query)
        await expect(helmet).toBeVisible()
    }
    await search.fill('protection sword')
    await expect(helmet).toHaveCount(0)
})

/** A graph opened without positions is arranged and saved; Arrange lays it out again, and nothing else moves. */
test('nodes are arranged and their positions saved', async ({page}) => {
    const stage = await stageEditor(page, {}, true)
    await openGraph(page, stage.link)

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    const placed = stage.changes[0] as {path: string, value: {x: number, y: number}}[]
    expect(placed.map(operation => operation.path).sort()).toEqual([
        '/mobs/speed_creeper/behaviour/nodes/ignite/editor',
        '/mobs/speed_creeper/behaviour/nodes/tick/editor',
    ])
    const tick = placed.find(operation => operation.path.endsWith('/tick/editor'))!.value
    const ignite = placed.find(operation => operation.path.endsWith('/ignite/editor'))!.value
    expect(tick.x).toBeLessThan(ignite.x)

    await page.getByRole('button', {name: 'Arrange nodes'}).click()
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
})

/** Dragging on the empty canvas selects the nodes in a box; it does not move the view. */
test('a drag on the canvas selects nodes', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    const ignite = page.locator('.vue-flow__node[data-id="ignite"]')
    const box = await settledBox(ignite)

    await page.mouse.move(box.x + box.width + 20, box.y + box.height + 20)
    await page.mouse.down()
    await page.mouse.move(box.x - 20, box.y - 20, {steps: 10})
    await page.mouse.up()

    await expect(ignite).toHaveClass(/selected/)
    expect(await settledBox(ignite)).toEqual(box)
})

/** Holding Space turns a drag on the canvas into moving the view. */
test('space and drag move the view', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    const ignite = page.locator('.vue-flow__node[data-id="ignite"]')
    const box = await settledBox(ignite)
    const viewport = page.locator('.vue-flow__transformationpane')
    const before = await viewport.getAttribute('style')

    await page.mouse.move(box.x + box.width + 40, box.y + box.height + 40)
    await page.keyboard.down('Space')
    await page.mouse.down()
    await page.mouse.move(box.x + box.width + 140, box.y + box.height + 90, {steps: 10})
    await page.mouse.up()
    await page.keyboard.up('Space')

    await expect(ignite).not.toHaveClass(/selected/)
    expect(await viewport.getAttribute('style')).not.toEqual(before)
    expect(stage.changes).toEqual([])
})

/** Mob settings parts start folded; the entity type offers only mobs, by their Minecraft names; an opened part stays open. */
test('mob settings pick named choices and fold their parts', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    const sheet = page.getByRole('complementary', {name: 'Mob settings'})
    const attributes = sheet.getByRole('button', {name: 'Attributes'})
    await expect(attributes).toHaveAttribute('aria-expanded', 'false')
    await expect(sheet.getByRole('button', {name: 'Spawn rules'})).toHaveAttribute('aria-expanded', 'false')
    await expect(sheet.getByRole('button', {name: 'Entity type'})).toHaveCount(0)

    await attributes.click()
    await expect(attributes).toHaveAttribute('aria-expanded', 'true')
    const entityType = sheet.getByRole('button', {name: 'Entity type'}).first()
    await expect(entityType).toContainText('Creeper')
    await entityType.click()
    await expect(page.getByRole('listbox').getByRole('option')).toHaveText(['Creeper', 'Skeleton', 'Zombie'])
    await page.getByRole('option', {name: 'Zombie', exact: true}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes).toEqual([[{op: 'add', path: '/mobs/speed_creeper/attributes/entityType', value: 'ZOMBIE'}]])

    await page.reload()
    await expect(sheet.getByRole('button', {name: 'Attributes'})).toHaveAttribute('aria-expanded', 'true')
    await expect(sheet.getByRole('button', {name: 'Spawn rules'})).toHaveAttribute('aria-expanded', 'false')
})

/** A value's type and value are set on the node itself; a new type clears the value the old one held. */
test('a value is typed and set on its node', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    await page.getByRole('button', {name: 'speed_creeper'}).click()
    await page.getByRole('button', {name: /z_painter/}).click()
    await page.getByRole('button', {name: 'Close'}).first().click()
    const node = page.locator('.vue-flow__node[data-id="amount"]')
    await settledBox(node)

    await choose(page, node.getByRole('button', {name: 'Type'}), 'Integer')
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toContainEqual({op: 'add', path: '/mobs/z_painter/behaviour/nodes/amount/config/type', value: 'integer'})

    await node.getByRole('spinbutton', {name: 'Value'}).fill('7')
    await node.getByRole('spinbutton', {name: 'Value'}).press('Enter')
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes[1]).toContainEqual({op: 'add', path: '/mobs/z_painter/behaviour/nodes/amount/config/value', value: 7})
})

/** A node taking red, green and blue offers one colour picker that fills all three. */
test('a colour picker fills red, green and blue', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    await page.getByRole('button', {name: 'speed_creeper'}).click()
    await page.getByRole('button', {name: /z_painter/}).click()
    const node = page.locator('.vue-flow__node[data-id="paint"]')
    await settledBox(node)

    await node.getByLabel('Color').fill('#ff8000')

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toEqual([
        {op: 'add', path: '/mobs/z_painter/behaviour/nodes/paint/literals/red', value: 255},
        {op: 'add', path: '/mobs/z_painter/behaviour/nodes/paint/literals/green', value: 128},
        {op: 'add', path: '/mobs/z_painter/behaviour/nodes/paint/literals/blue', value: 0},
    ])
})

/** The question mark in a node's header tells what the node does. */
test('a node explains itself', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.locator('.vue-flow__node[data-id="ignite"]').getByTestId('node-help').hover()

    await expect(page.getByTestId('node-help-tooltip')).toContainText('Lights the fuse.')
})

/** A double click on a wire puts a reroute on it, keeping the chain in its place. */
test('a double click on a wire adds a reroute', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    const tick = await settledBox(page.locator('.vue-flow__node[data-id="tick"] [data-handleid="exec:next"]'))
    const ignite = await settledBox(page.locator('.vue-flow__node[data-id="ignite"] [data-handleid="exec-in"]'))

    await page.mouse.dblclick((tick.x + ignite.x) / 2 + tick.width / 2, (tick.y + ignite.y) / 2 + tick.height / 2)

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    const operations = stage.changes[0] as {op: string, path: string, value: unknown}[]
    expect(operations.map(operation => [operation.op, operation.path])).toEqual([
        ['add', '/mobs/speed_creeper/behaviour/nodes/execution_1'],
        ['replace', '/mobs/speed_creeper/behaviour/nodes/tick/executions/next/0'],
    ])
    expect(operations[0]!.value).toMatchObject({type: 'reroute.execution', executions: {next: ['ignite']}})
    await expect(page.getByTestId('reroute')).toHaveCount(1)
})

/** A dragged node lands on the grid the background draws. */
test('nodes snap to the grid', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    const header = page.locator('.vue-flow__node[data-id="ignite"]').getByText('Ignite', {exact: true})
    const box = await settledBox(header)

    await page.mouse.move(box.x + 5, box.y + 5)
    await page.mouse.down()
    await page.mouse.move(box.x + 52, box.y + 37, {steps: 8})
    await page.mouse.up()

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    const moved = (stage.changes[0] as {value: {x: number, y: number}}[])[0]!.value
    expect(moved.x % 22).toBe(0)
    expect(moved.y % 22).toBe(0)
})

/** A node with many properties draws them under the type that brings them, the node's own type first. */
test('properties are grouped by the type that brings them', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('button', {name: 'speed_creeper'}).click()
    await page.getByRole('button', {name: /z_painter/}).click()

    await expect(page.locator('.vue-flow__node[data-id="look"]').getByTestId('port-group')).toHaveText(['Creeper', 'Living Entity'])
})

/** Only values the plugin can read get a field; entities and vectors are wired, and long names do not overlap. */
test('only typed-in values get a field', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    await page.getByRole('button', {name: 'speed_creeper'}).click()
    await page.getByRole('button', {name: /z_painter/}).click()
    await page.setViewportSize({width: 1600, height: 1200})
    await page.getByRole('button', {name: 'Close'}).first().click()
    await page.getByRole('button', {name: 'Fit to view'}).click()
    const node = page.locator('.vue-flow__node[data-id="change"]')
    await settledBox(node)
    await expect(node.getByRole('button', {name: 'More inputs: 1'})).toBeVisible()

    await expect(node.getByRole('spinbutton')).toHaveCount(2)
    await expect(node.getByLabel('Creeper', {exact: true})).toHaveCount(0)
    await expect(node.getByLabel('Velocity', {exact: true})).toHaveCount(0)
    const inputs = await node.getByText('Maximum No Damage Ticks').first().boundingBox()
    const outputs = await node.getByText('Despawn In Peaceful Override').boundingBox()
    expect(inputs!.x + inputs!.width).toBeLessThan(outputs!.x)
    await expect(node.getByText('Max Fuse Ticks', {exact: true})).toBeVisible()
    expect((await node.getByText('Max Fuse Ticks', {exact: true}).boundingBox())!.width).toBeGreaterThan(60)
})

/** A double click on a field edits it; it does not open the node's details. */
test('a double click on a field does not open the node', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    const field = page.locator('.vue-flow__node[data-id="ignite"]').getByRole('spinbutton', {name: 'fuse'})
    await settledBox(field)

    await field.dblclick()

    await expect(page.getByText('Lights the fuse.')).toHaveCount(0)
})

/** A switch picks the type it switches on, and each case takes its value beside its pin. */
test('a switch matches texts typed beside its cases', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    await page.getByRole('button', {name: 'speed_creeper'}).click()
    await page.getByRole('button', {name: /z_painter/}).click()
    await page.setViewportSize({width: 1600, height: 1200})
    await page.getByRole('button', {name: 'Close'}).first().click()
    await page.getByRole('button', {name: 'Fit to view'}).click()
    const node = page.locator('.vue-flow__node[data-id="pick"]')
    await settledBox(node)

    await choose(page, node.getByRole('button', {name: 'Type'}), 'text')
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toEqual([
        {op: 'add', path: '/mobs/z_painter/behaviour/nodes/pick/config/type', value: 'text'},
        {op: 'remove', path: '/mobs/z_painter/behaviour/nodes/pick/config/case_0'},
    ])

    await expect(node.getByRole('textbox', {name: 'Case 2'})).toHaveCount(0)
    await node.getByRole('button', {name: 'Add case'}).click()
    await node.getByRole('textbox', {name: 'Case 2'}).fill('fire')
    await node.getByRole('textbox', {name: 'Case 2'}).press('Enter')
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes[1]).toContainEqual({op: 'add', path: '/mobs/z_painter/behaviour/nodes/pick/config/case_1', value: 'fire'})

    await node.getByRole('button', {name: 'Remove Case 2'}).click()
    await expect(page.getByText('Saved, revision 3')).toBeVisible()
    await expect(node.getByRole('textbox', {name: 'Case 2'})).toHaveCount(0)
})

/** Naming an existing variable on Get Variable takes over the type that variable holds. */
test('a variable node follows the variable it names', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    await page.getByRole('button', {name: 'speed_creeper'}).click()
    await page.getByRole('button', {name: /z_painter/}).click()
    await page.setViewportSize({width: 1600, height: 1200})
    await page.getByRole('button', {name: 'Close'}).first().click()
    await page.getByRole('button', {name: 'Fit to view'}).click()
    const node = page.locator('.vue-flow__node[data-id="read"]')
    await settledBox(node)

    await node.getByRole('combobox', {name: 'Name'}).fill('hi')
    const suggestion = page.getByRole('option', {name: /hits/})
    await expect(suggestion).toContainText('Integer')
    await suggestion.click()

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toContainEqual({op: 'add', path: '/mobs/z_painter/behaviour/nodes/read/config/type', value: 'integer'})
    await expect(node.getByRole('button', {name: 'Type'})).toContainText('Integer')
})

/** Deleting a wired node removes it and its wires in one change. */
test('a wired node is deleted with its wires', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    await page.locator('.vue-flow__node[data-id="ignite"]').getByText('Ignite', {exact: true}).click()

    await page.keyboard.press('Delete')

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    await expect(page.locator('.vue-flow__node')).toHaveCount(1)
    expect(stage.changes).toEqual([[
        {op: 'remove', path: '/mobs/speed_creeper/behaviour/nodes/tick/executions/next/0'},
        {op: 'remove', path: '/mobs/speed_creeper/behaviour/nodes/ignite'},
    ]])
})

/** A double click on the empty canvas offers every node and adds the one picked there. */
test('a double click on the canvas adds a node', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    await settledBox(page.locator('.vue-flow__node[data-id="ignite"]'))

    await page.mouse.dblclick(1100, 600)
    const picker = page.getByRole('dialog', {name: 'Add node'})
    await picker.getByRole('searchbox').fill('ign')
    await picker.getByRole('searchbox').press('Enter')

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    const operations = stage.changes[0] as {path: string, value: {type: string}}[]
    expect(operations.map(operation => [operation.path, operation.value.type])).toEqual([
        ['/mobs/speed_creeper/behaviour/nodes/ignite_1', 'creeper.ignite'],
    ])
})

/** The shortcut help is folded until asked for. */
test('the shortcut help opens on demand', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    const help = page.getByRole('region', {name: 'Shortcuts'})
    await expect(help).toHaveCount(0)

    await page.getByRole('button', {name: 'Shortcuts'}).click()

    await expect(help).toContainText('Space + drag')
    await help.getByRole('button', {name: 'Close'}).click()
    await expect(help).toHaveCount(0)
})

/** A routine the graph defines is called through a Call node, offered with the other nodes and drawn with its parameters. */
test('a routine is called from the node picker', async ({page}) => {
    const stage = await stageEditor(page)
    await openGraph(page, stage.link)
    await page.getByRole('button', {name: 'speed_creeper'}).click()
    await page.getByRole('button', {name: /z_painter/}).click()
    await page.getByRole('button', {name: 'Close'}).first().click()
    await page.getByRole('button', {name: 'Fit to view'}).click()
    const start = await settledBox(page.locator('.vue-flow__node[data-id="start"]'))

    await page.mouse.dblclick(start.x + start.width / 2, start.y + start.height + 40)
    const picker = page.getByRole('dialog', {name: 'Add node'})
    await picker.getByRole('searchbox').fill('call')
    await picker.getByRole('searchbox').press('Enter')

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    const operations = stage.changes[0] as {path: string, value: {type: string}}[]
    expect(operations.map(operation => operation.value.type)).toEqual(['routine.knockback'])
    await expect(page.getByText('Call knockback')).toBeVisible()
})

/** Switching between mobs keeps every mob's wires drawn. */
test('wires survive switching mobs', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await expect(page.locator('.vue-flow__edge')).toHaveCount(1)

    await page.getByRole('button', {name: 'speed_creeper'}).click()
    await page.getByRole('button', {name: /z_painter/}).click()
    await expect(page.locator('.vue-flow__node[data-id="change"]')).toBeVisible()
    await expect(page.locator('.vue-flow__edge')).toHaveCount(1)

    await page.getByRole('button', {name: 'z_painter'}).click()
    await page.getByRole('button', {name: /speed_creeper/}).click()
    await expect(page.locator('.vue-flow__node[data-id="ignite"]')).toBeVisible()
    await expect(page.locator('.vue-flow__edge')).toHaveCount(1)
})

/** The play button of an event lights up the nodes it runs and the wires between them, in order, then lets go. */
test('playing an event lights up what it runs', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    const tick = page.locator('.vue-flow__node[data-id="tick"]')
    const ignite = page.locator('.vue-flow__node[data-id="ignite"]')
    await settledBox(ignite)

    await tick.getByTestId('play').click()

    await expect(tick.getByTestId('step')).toHaveText('1')
    await expect(ignite.getByTestId('step')).toHaveText('2')
    await expect(ignite).toHaveClass(/bn-current/)
    await expect(page.locator('.vue-flow__edge.bn-lit-wire')).toHaveCount(1)
    await expect(page.getByTestId('step')).toHaveCount(0, {timeout: 5000})
    expect(stage.changes).toEqual([])
})

/** The full graph editor opens over the list, attributes and preview, and Back returns to them. */
test('edit opens the graph editor and back returns to the mob list', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)

    await expect(page.getByRole('button', {name: 'Attributes'})).toBeVisible()
    await page.getByRole('button', {name: 'Edit'}).click()
    await expect(page.getByRole('button', {name: 'Back'})).toBeVisible()
    await expect(page.locator('.vue-flow__node')).toHaveCount(2)

    await page.getByRole('button', {name: 'Back'}).click()
    await expect(page.getByRole('button', {name: 'Edit'})).toBeVisible()
})

/** A mob of the store is installed with its origin through the add menu. */
test('a mob is installed from the store', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)

    await page.getByRole('button', {name: 'Add', exact: true}).click()
    await page.getByRole('button', {name: /From the store/}).click()
    await expect(page.getByText('Night Hound')).toBeVisible()
    await page.getByRole('button', {name: 'Install', exact: true}).click()

    await expect.poll(() => stage.changes.length).toBe(1)
    expect(stage.changes[0]).toEqual([
        expect.objectContaining({
            op: 'add',
            path: '/mobs/night_hound',
            value: expect.objectContaining({origin: {store: 'bloodnight', entry: 'night_hound', hash: 'h1'}}),
        }),
        {op: 'add', path: '/lootTables/night_hound', value: {name: 'Night Hound', entries: []}},
    ])
    await expect(page.getByText('Installed')).toBeVisible()
})

/** The setup wizard turns on a world, installs the classic mobs, marks the setup as done and the session as done. */
test('the setup wizard sends everything as one change', async ({page}) => {
    const stage = await stageEditor(page, {}, false, true)
    await page.goto(stage.link)

    await expect(page.getByText('Which worlds get blood nights?')).toBeVisible()
    await page.getByRole('switch', {name: 'world_the_end'}).click()
    for (let step = 0; step < 4; step++) await page.getByRole('button', {name: 'Next'}).click()
    await expect(page.getByRole('checkbox')).toBeChecked()
    await page.getByRole('button', {name: 'Next'}).click()
    await expect(page.getByText('1 mobs are installed.')).toBeVisible()
    await page.getByRole('button', {name: 'Finish'}).click()

    await expect(page.getByText('Almost done')).toBeVisible()
    expect(stage.submits).toBe(1)
    const operations = stage.changes[0] as {path: string, value: unknown}[]
    expect(operations).toContainEqual({op: 'add', path: '/settings/worlds/w3/settings/enabled', value: true})
    expect(operations).toContainEqual(expect.objectContaining({op: 'add', path: '/mobs/night_hound'}))
    expect(operations.at(-1)).toEqual({op: 'add', path: '/settings/main/setup', value: {done: true}})

    await page.getByRole('button', {name: 'Open the full editor'}).click()
    await expect(page.getByRole('button', {name: 'speed_creeper'})).toBeVisible()
})

/** The read-only preview lays out a graph without positions for itself, drags the view without Space and saves nothing. */
test('the preview arranges a graph without positions', async ({page}) => {
    const stage = await stageEditor(page, {}, true)
    await page.goto(stage.link)

    const tick = page.locator('.vue-flow__node').filter({hasText: 'Every tick'})
    const ignite = page.locator('.vue-flow__node').filter({hasText: 'Ignite'})
    await expect.poll(async () => {
        const [first, second] = [await tick.boundingBox(), await ignite.boundingBox()]
        return first !== null && second !== null && first.x + first.width <= second.x
    }).toBe(true)

    await expect(page.locator('.bn-canvas[data-arranged="true"]')).toBeVisible()
    const before = await settledBox(tick)
    const pane = await page.locator('.vue-flow__pane').boundingBox()
    if (!pane) throw new Error('no pane')
    await page.mouse.move(pane.x + 20, pane.y + 20)
    await page.mouse.down()
    await page.mouse.move(pane.x + 120, pane.y + 60, {steps: 10})
    await page.mouse.up()
    await expect.poll(async () => ((await tick.boundingBox())?.x ?? 0) - before.x).toBeGreaterThan(20)
    expect(stage.changes).toEqual([])
})

/** A move made while a graph without positions is still being arranged stays: the view is not fitted over it. */
test('a view moved while the graph is arranged stays where it was moved', async ({page}) => {
    const stage = await stageEditor(page, {}, true)
    await page.route(/elk.*bundled/, async route => {
        await new Promise(resolve => setTimeout(resolve, 1500))
        await route.continue()
    })
    await page.goto(stage.link)
    const canvas = page.locator('.bn-canvas')
    await expect(canvas).toHaveAttribute('data-arranged', 'false')

    const pane = await page.locator('.vue-flow__pane').boundingBox()
    if (!pane) throw new Error('no pane')
    await page.mouse.move(pane.x + 20, pane.y + 20)
    await page.mouse.down()
    await page.mouse.move(pane.x + 140, pane.y + 70, {steps: 10})
    await page.mouse.up()
    const moved = await page.locator('.vue-flow__transformationpane').getAttribute('style')

    await expect(canvas).toHaveAttribute('data-arranged', 'true')
    expect(await page.locator('.vue-flow__transformationpane').getAttribute('style')).toBe(moved)
})

/** Values their modifier ignores stay hidden until the modifier uses them. */
test('mob attributes show only the values that are used', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)

    await page.getByRole('button', {name: 'Attributes'}).click()
    await expect(page.getByText('Health from').first()).toBeVisible()
    await expect(page.getByText('Health', {exact: true})).toHaveCount(0)
})

/** A settings page longer than the window scrolls, in a world and on the server. */
test.describe('long settings', () => {
    test.use({viewport: {width: 1280, height: 640}})

    test('scroll in world and server settings', async ({page}) => {
        const stage = await stageEditor(page)
        await page.goto(stage.link)
        await page.getByRole('tab', {name: 'Settings'}).click()
        await page.getByRole('tab', {name: 'Night', exact: true}).click()
        const scrolled = async () => page.locator('main.overflow-y-auto').evaluate(main => main.scrollTop)

        await page.mouse.move(800, 400)
        await page.mouse.wheel(0, 600)
        await expect.poll(scrolled).toBeGreaterThan(0)

        await page.setViewportSize({width: 1280, height: 360})
        await page.getByRole('tab', {name: 'Server settings'}).click()
        await page.mouse.move(800, 300)
        await page.mouse.wheel(0, 600)
        await expect.poll(scrolled).toBeGreaterThan(0)
    })
})

/** Blocked commands are a list: entries are added with Enter or the button and removed with their own button. */
test('commands are added and removed one by one', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.getByRole('tab', {name: 'Night', exact: true}).click()
    const field = page.getByLabel('Commands blocked during blood nights', {exact: true})
    await field.fill('home')
    await field.press('Enter')
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    await field.fill('spawn')
    await field.locator('../..').getByRole('button', {name: 'Add', exact: true}).click()
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    await page.getByRole('button', {name: 'Remove entry 1'}).last().click()
    await expect(page.getByText('Saved, revision 3')).toBeVisible()

    const path = '/settings/worlds/w1/settings/nightSettings/blockedCommands'
    expect(stage.changes).toEqual([
        [{op: 'add', path, value: ['home']}],
        [{op: 'add', path, value: ['home', 'spawn']}],
        [{op: 'add', path, value: ['spawn']}],
    ])
})

/** A sound is picked by what it sounds like, played, and tuned with sliders; the ambience pause is one range. */
test('sounds are picked by ear and tuned with sliders', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.getByRole('tab', {name: 'Sounds', exact: true}).click()

    const begin = page.getByRole('group', {name: 'When a blood night begins'})
    await expect(begin.getByRole('button', {name: 'Sound 1'})).toContainText('Thunder roars')

    await begin.getByRole('button', {name: 'Sound 1'}).click()
    await page.getByPlaceholder('Search by what you hear, e.g. thunder or zombie').fill('zombie')
    await page.getByRole('option', {name: /Zombie groans/}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    const path = '/settings/worlds/w1/settings/soundSettings/begin'
    expect(stage.changes[0]).toEqual([{op: 'add', path, value: [
        {sound: 'minecraft:entity.zombie.ambient', pitch: {min: 0.8, max: 1}, volume: {min: 1, max: 1}},
    ]}])

    await begin.getByRole('slider', {name: 'Highest pitch'}).focus()
    await page.keyboard.press('ArrowRight')
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes[1]).toEqual([{op: 'add', path, value: [
        {sound: 'minecraft:entity.zombie.ambient', pitch: {min: 0.8, max: 1.05}, volume: {min: 1, max: 1}},
    ]}])

    const played = page.waitForRequest('**/api/v1/assets/26.3/sounds/entity.zombie.ambient.ogg')
    await begin.getByRole('button', {name: 'Play as players may hear it'}).click()
    await played

    await page.getByRole('slider', {name: 'Shortest pause'}).focus()
    await page.keyboard.press('ArrowRight')
    await expect(page.getByText('Saved, revision 3')).toBeVisible()
    expect(stage.changes[2]).toEqual([
        {op: 'add', path: '/settings/worlds/w1/settings/soundSettings/minInterval', value: 11},
        {op: 'add', path: '/settings/worlds/w1/settings/soundSettings/maxInterval', value: 40},
    ])
})

/** World settings show what matters for the world's kind; times, effects, rules and dragons are picked, not typed. */
test.describe('settings an everyday user can fill in', () => {
    test.use({viewport: {width: 1280, height: 1400}})

    test('night times are a clock, and the Nether has no night times or night selection', async ({page}) => {
        const stage = await stageEditor(page)
        await page.goto(stage.link)
        await page.getByRole('tab', {name: 'Settings'}).click()
        await page.locator('main').getByRole('tab', {name: 'Night', exact: true}).click()
        await expect(page.getByText('19:00')).toBeVisible()
        await page.getByRole('slider', {name: 'Night begins at'}).focus()
        await page.keyboard.press('ArrowRight')
        await expect(page.getByText('19:15')).toBeVisible()
        await expect(page.getByText('Saved, revision 1')).toBeVisible()
        expect(stage.changes[0]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightSettings/begin', value: 13250}])
        await expect(page.getByText('A new chance every (minutes)')).toHaveCount(0)

        await page.getByRole('button', {name: /world_nether/}).click()
        await expect(page.locator('main').getByRole('tab', {name: 'Night selection'})).toHaveCount(0)
        await page.locator('main').getByRole('tab', {name: 'Night', exact: true}).click()
        await expect(page.getByRole('slider', {name: 'Night begins at'})).toHaveCount(0)
        await expect(page.getByText('A new chance every (minutes)')).toBeVisible()
        await expect(page.getByText('A dragon fight is a blood night')).toHaveCount(0)
    })

    test('the End picks its dragon from the mobs based on the Ender Dragon', async ({page}) => {
        const stage = await stageEditor(page)
        await page.goto(stage.link)
        await page.getByRole('tab', {name: 'Settings'}).click()
        await page.getByRole('button', {name: /world_the_end/}).click()
        await page.getByRole('switch', {name: 'Blood nights in this world'}).click()
        await page.locator('main').getByRole('tab', {name: 'Night', exact: true}).click()
        await page.getByRole('button', {name: 'Mob the dragon becomes'}).click()
        await expect(page.getByRole('listbox').getByRole('option')).toHaveText(['Blood Dragon'])
    })

    test('effects, game rules and how many drop are picked by name', async ({page}) => {
        const stage = await stageEditor(page)
        await page.goto(stage.link)
        await page.getByRole('tab', {name: 'Settings'}).click()

        await page.locator('main').getByRole('tab', {name: 'World rules'}).click()
        await page.getByRole('button', {name: 'Add a game rule'}).click()
        await page.getByRole('option', {name: 'Spawn phantoms'}).click()
        await expect(page.getByText('Saved, revision 1')).toBeVisible()
        expect(stage.changes[0]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightRules/gameRules', value: {'minecraft:spawn_phantoms': 'true'}}])

        await page.getByRole('switch', {name: 'Change the monster spawn limit'}).click()
        await expect(page.getByText('Saved, revision 2')).toBeVisible()
        expect(stage.changes[1]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightRules/monsterSpawnLimit', value: 1}])

        await page.locator('main').getByRole('tab', {name: 'Loot'}).click()
        await expect(page.getByText('As the server').first()).toBeVisible()
        await page.getByRole('button', {name: 'Set its own'}).first().click()
        await expect(page.getByText('Saved, revision 3')).toBeVisible()
        expect(stage.changes[2]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/loot/slots', value: {1: 1}}])

        await page.getByRole('button', {name: 'Add a number'}).first().click()
        await expect(page.getByText('Saved, revision 4')).toBeVisible()
        expect(stage.changes[3]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/loot/slots', value: {0: 10, 1: 1}}])
        await expect(page.getByText('Nothing')).toBeVisible()

        await page.getByRole('button', {name: 'Use the broader setting'}).first().click()
        await expect(page.getByText('Saved, revision 5')).toBeVisible()
        expect(stage.changes[4]).toEqual([{op: 'remove', path: '/settings/worlds/w1/settings/loot/slots'}])
    })
})

/** Command lists suggest the server's commands in the editor's own list, picked with the keyboard. */
test('the server\'s commands are suggested while typing', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.locator('main').getByRole('tab', {name: 'Night', exact: true}).click()

    const field = page.getByLabel('Commands blocked during blood nights', {exact: true})
    await field.fill('ho')
    await expect(page.getByRole('option', {name: 'home'})).toBeVisible()
    await expect(page.getByRole('option', {name: 'spawn'})).toHaveCount(0)
    await field.press('ArrowDown')
    await field.press('Enter')
    await field.press('Enter')

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightSettings/blockedCommands', value: ['home']}])
})

/** The moon phase selection lists every phase with its picture and a slider, and saves all of them together. */
test('every moon phase gets its own chance', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.locator('main').getByRole('tab', {name: 'Night selection'}).click()
    await page.getByRole('button', {name: 'Type'}).click()
    await page.getByRole('option', {name: 'Moon phase', exact: true}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()

    await expect(page.getByRole('slider')).toHaveCount(8)
    await page.getByRole('slider', {name: 'Full moon'}).focus()
    await page.keyboard.press('ArrowRight')
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes[1]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightSelection/moonPhase/probabilities', value: {
        FULL_MOON: 5, WANING_GIBBOUS: 10, LAST_QUARTER: 20, WANING_CRESCENT: 40, NEW_MOON: 100, WAXING_CRESCENT: 40, FIRST_QUARTER: 20, WAXING_GIBBOUS: 10,
    }}])
})

/** The phase cycle is an equalizer: nights are added with a button and each bar is pulled up or down. */
test('the phase cycle is set like an equalizer', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.locator('main').getByRole('tab', {name: 'Night selection'}).click()
    await page.getByRole('button', {name: 'Type'}).click()
    await page.getByRole('option', {name: 'Phase cycle', exact: true}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()

    await expect(page.getByRole('slider')).toHaveCount(3)
    await page.getByRole('button', {name: 'One night more'}).click()
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes[1]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightSelection/phase/probabilities', value: [50, 50, 50, 50]}])

    const last = page.getByRole('slider', {name: 'Night 4'})
    const box = await last.boundingBox()
    if (!box) throw new Error('no bar')
    await page.mouse.move(box.x + box.width / 2, box.y + box.height - 4)
    await page.mouse.down()
    await page.mouse.move(box.x + box.width / 2, box.y - box.height * 0.6, {steps: 5})
    await page.mouse.up()
    await expect(page.getByText('Saved, revision 3')).toBeVisible()
    expect(stage.changes[2]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightSelection/phase/probabilities', value: [50, 50, 50, 80]}])

    await last.focus()
    await page.keyboard.press('ArrowDown')
    await expect(page.getByText('Saved, revision 4')).toBeVisible()
    expect(stage.changes[3]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightSelection/phase/probabilities', value: [50, 50, 50, 75]}])

    const nights = page.getByRole('spinbutton', {name: 'Nights in the cycle'})
    await nights.fill('6')
    await nights.press('Enter')
    await expect(page.getByText('Saved, revision 5')).toBeVisible()
    await expect(page.getByRole('slider', {name: /^Night \d+$/})).toHaveCount(6)
})

/** The curve shows a graph of the chance per night that follows its sliders. */
test('the curve draws its chances', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.locator('main').getByRole('tab', {name: 'Night selection'}).click()
    await page.getByRole('button', {name: 'Type'}).click()
    await page.getByRole('option', {name: 'Curve', exact: true}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()

    const graph = page.getByRole('figure', {name: 'Chance of a blood night per night'})
    await expect(graph.getByTitle('Night 6: 80 %')).toBeVisible()
    await page.getByRole('slider', {name: 'Chance halfway through'}).focus()
    await page.keyboard.press('ArrowRight')
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes[1]).toEqual([{op: 'add', path: '/settings/worlds/w1/settings/nightSelection/curve', value: {min: 20, max: 85, period: 10}}])
    await expect(graph.getByTitle('Night 6: 85 %')).toBeVisible()
})

/** Range handles that meet can be pulled apart either way, by mouse or keys, from whichever handle is on top. */
test('range handles that meet can be pulled apart either way', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.getByRole('tab', {name: 'Sounds', exact: true}).click()

    const begin = page.getByRole('group', {name: 'When a blood night begins'})
    const track = await begin.getByRole('slider', {name: 'Quietest volume'}).boundingBox()
    if (!track) throw new Error('no volume slider')
    const y = track.y + track.height / 2
    await page.mouse.move(track.x + track.width - 8, y)
    await page.mouse.down()
    await page.mouse.move(track.x + track.width / 2, y, {steps: 5})
    await page.mouse.up()

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    const [change] = stage.changes[0] ?? []
    const [entry] = (change as {value: {volume: {min: number, max: number}}[]}).value
    expect(entry?.volume.max).toBe(1)
    expect(entry?.volume.min).toBeLessThan(0.7)

    const pitch = begin.getByRole('slider', {name: 'Highest pitch'})
    await pitch.focus()
    for (let step = 0; step < 4; step++) await page.keyboard.press('ArrowLeft')
    await begin.getByRole('slider', {name: 'Deepest pitch'}).focus()
    await page.keyboard.press('ArrowRight')
    await expect(begin.getByRole('slider', {name: 'Deepest pitch'})).toHaveValue('0.8')
    await expect(pitch).toHaveValue('0.85')
    await pitch.focus()
    await page.keyboard.press('ArrowLeft')
    await page.keyboard.press('ArrowLeft')
    await expect(begin.getByRole('slider', {name: 'Deepest pitch'})).toHaveValue('0.75')
    await expect(pitch).toHaveValue('0.8')
})

/** MiniMessage is typed with Tab completion and a quick reference that stays open while typing, and the item shows its tooltip. */
test('item names and lore are typed with help and previewed', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Items'}).click()
    await page.getByRole('navigation', {name: 'Items'}).getByRole('button', {name: /guard_helmet/}).click()

    const tooltip = page.getByRole('figure', {name: 'In game'})
    await expect(tooltip).toContainText('Iron Helmet')
    await expect(tooltip).toContainText('Worn by the night watch')

    const name = page.getByLabel('Name', {exact: true})
    await name.fill('<re')
    await expect(page.getByText('Tab completes: red')).toBeVisible()
    await name.press('Tab')
    await expect(name).toHaveValue('<red>')
    await name.pressSequentially('Guard')
    await expect(tooltip).toContainText('Guard')

    await page.getByRole('button', {name: 'MiniMessage quick reference'}).first().click()
    const help = page.getByRole('dialog', {name: 'Colours and styles'})
    await expect(help).toBeVisible()
    await name.press('End')
    await name.pressSequentially(' Helmet')
    await expect(help).toBeVisible()
    await help.getByRole('button', {name: '<bold>'}).click()
    await expect(name).toHaveValue('<red>Guard Helmet<bold>')
    await expect(help).not.toContainText('editor.')
})

/** Command lists offer the placeholders the plugin fills in. */
test('commands get placeholders with a click', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.locator('main').getByRole('tab', {name: 'Player deaths'}).click()
    const field = page.getByLabel('Commands on death', {exact: true})
    await field.fill('say')
    await page.getByRole('button', {name: 'Insert {player}'}).first().click()
    await expect(field).toHaveValue('say {player}')
    await expect(page.locator('main').getByRole('tab', {name: 'Enemy deaths'})).toBeVisible()
})

/** A mob's name is written per language, a language is added by picking it, and the style shows in the preview. */
test('a mob name is written per language', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    const sheet = page.getByRole('complementary', {name: 'Mob settings'})
    await sheet.getByRole('button', {name: 'Attributes'}).click()
    await expect(sheet.getByRole('textbox', {name: 'Name in English'})).toHaveValue('Speed Creeper')
    await expect(sheet.getByRole('textbox', {name: 'Name in German'})).toHaveValue('Flinker Creeper')
    await expect(sheet.getByText(/^On: this mob only takes the place/)).toBeVisible()
    await expect(sheet).not.toContainText('MythicMobs')

    const adding = sheet.getByRole('combobox', {name: 'Add a language'})
    await adding.fill('fr')
    await page.getByRole('option', {name: /French/}).click()
    const french = sheet.getByRole('textbox', {name: 'Name in French'})
    await french.fill('Creeper rapide')
    await french.press('Enter')
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toEqual([{op: 'add', path: '/mobs/speed_creeper/attributes/name', value: {style: '', texts: {en: 'Speed Creeper', de: 'Flinker Creeper', fr: 'Creeper rapide'}}}])
})

/** Biomes are picked by name and biome tags read as groups of biomes. */
test('biomes and biome groups are picked by name', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    const sheet = page.getByRole('complementary', {name: 'Mob settings'})
    await sheet.getByRole('button', {name: 'Spawn rules'}).click()
    const field = sheet.getByLabel('Biomes', {exact: true})
    await field.click()
    await expect(page.getByRole('option', {name: /All ocean biomes/})).toBeVisible()
    await page.getByRole('option', {name: /Desert/}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toEqual([{op: 'add', path: '/mobs/speed_creeper/spawn/conditions/biomes', value: ['minecraft:desert']}])
    await expect(sheet.getByRole('listitem').filter({hasText: 'Desert'})).toContainText('minecraft:desert')
})

/** A world switches mobs off and warns about mobs that never spawn by themselves in its kind of world. */
test('a world picks its mobs and warns about ones that cannot spawn there', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    await page.getByRole('tab', {name: 'Settings'}).click()
    await page.getByRole('button', {name: /world_nether/}).click()
    await page.locator('main').getByRole('tab', {name: 'Mobs', exact: true}).click()

    await expect(page.getByText(/No .* spawns by itself in the Nether/).first()).toBeVisible()
    await expect(page.getByRole('region', {name: 'CREEPER'}).or(page.getByRole('region', {name: 'Creeper'}))).toContainText('Speed Creeper')
    await page.getByRole('switch', {name: 'Speed Creeper spawns in this world'}).click()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes[0]).toEqual([{op: 'add', path: '/settings/worlds/w2/settings/mobSettings/spawning/mobs', value: {speed_creeper: 0}}])
    await expect(page.getByRole('spinbutton', {name: 'Weight of Speed Creeper in this world'})).toHaveCount(0)

    await page.getByRole('button', {name: 'Switch those off'}).click()
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    await expect(page.getByRole('button', {name: 'Switch those off'})).toHaveCount(0)
    expect(Object.values((stage.changes[1]?.[0] as {value: Record<string, number>}).value).every(weight => weight === 0)).toBe(true)
})

/** The footer switches the editor to light colours and to another language, and both stay after a reload. */
test('the footer switches colours and language', async ({page}) => {
    const stage = await stageEditor(page)
    await page.goto(stage.link)
    const footer = page.getByRole('contentinfo')
    await expect(footer.getByRole('link', {name: 'Docs'})).toHaveAttribute('href', /bloodnight\.docs/)

    await footer.getByRole('button', {name: 'Switch to light colours'}).click()
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'light')
    await footer.getByRole('combobox').selectOption('de')
    await expect(page.getByRole('tab', {name: 'Einstellungen'})).toBeVisible()

    await page.reload()
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'light')
    await expect(page.getByRole('tab', {name: 'Einstellungen'})).toBeVisible()
    await page.getByRole('contentinfo').getByRole('button', {name: 'Zu dunklen Farben wechseln'}).click()
    await expect(page.locator('html')).not.toHaveAttribute('data-theme', 'light')
})
