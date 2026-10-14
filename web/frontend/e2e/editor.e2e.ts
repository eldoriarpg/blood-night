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

    const begin = page.getByLabel('Night begins (ticks after sunrise)')
    await begin.fill('12500')
    await begin.blur()

    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    expect(stage.changes).toEqual([[{op: 'add', path: '/settings/worlds/w1/settings/nightSettings/begin', value: 12500}]])
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

    const name = page.getByLabel('Name', {exact: true})
    await name.fill('<gray>Guard Helmet')
    await name.blur()
    await expect(page.getByText('Saved, revision 1')).toBeVisible()
    await page.getByLabel('Enchantment, e.g. minecraft:sharpness').fill('minecraft:sharpness')
    await page.getByRole('button', {name: 'Add', exact: true}).click()

    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes).toEqual([
        [{op: 'add', path: '/items/guard_helmet/name', value: '<gray>Guard Helmet'}],
        [{op: 'add', path: '/items/guard_helmet/enchantments', value: {'minecraft:protection': 2, 'minecraft:sharpness': 1}}],
    ])
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
    expect(stage.changes).toEqual([[{op: 'add', path: '/items/knight_axe', value: {kind: 'DEFINED', material: 'minecraft:golden_axe', lore: [], enchantments: {}}}]])
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
    await page.goto(stage.link)
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
    await expect(page.getByRole('option')).toHaveText(['Creeper', 'Skeleton', 'Zombie'])
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

    await node.getByRole('textbox', {name: 'Case 2'}).fill('fire')
    await node.getByRole('textbox', {name: 'Case 2'}).press('Enter')
    await expect(page.getByText('Saved, revision 2')).toBeVisible()
    expect(stage.changes[1]).toContainEqual({op: 'add', path: '/mobs/z_painter/behaviour/nodes/pick/config/case_1', value: 'fire'})
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
    expect(stage.changes[0]).toEqual([expect.objectContaining({
        op: 'add',
        path: '/mobs/night_hound',
        value: expect.objectContaining({origin: {store: 'bloodnight', entry: 'night_hound', hash: 'h1'}}),
    })])
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
