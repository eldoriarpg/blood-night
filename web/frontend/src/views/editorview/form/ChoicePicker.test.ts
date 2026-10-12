/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {afterEach, describe, expect, it} from 'vitest'
import {mount, type VueWrapper} from '@vue/test-utils'
import {nextTick} from 'vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'

let wrapper: VueWrapper | null = null

afterEach(() => {
    wrapper?.unmount()
    wrapper = null
    document.body.innerHTML = ''
})

const FILLERS = Array.from({length: 12}, (_, index) => `FILLER_${index}`)

function mountPicker(props: Record<string, unknown> = {}) {
    wrapper = mount(ChoicePicker, {
        attachTo: document.body,
        props: {
            label: 'Entity',
            options: ['CAVE_SPIDER', 'SPIDER', 'CREEPER', ...FILLERS],
            labels: {CAVE_SPIDER: 'Cave Spider', SPIDER: 'Spider', CREEPER: 'Creeper'},
            modelValue: 'CREEPER',
            ...props,
        },
    })
    return wrapper
}

function options(): string[] {
    return [...document.body.querySelectorAll('[role="option"]')].map(option => option.textContent.trim())
}

async function openIt(): Promise<void> {
    await wrapper!.get('button').trigger('click')
    await nextTick()
}

async function search(text: string): Promise<HTMLInputElement> {
    const input = document.body.querySelector<HTMLInputElement>('[role="dialog"] input')!
    input.value = text
    input.dispatchEvent(new Event('input'))
    await nextTick()
    return input
}

describe('ChoicePicker', () => {
    it('shows the chosen label and its key', () => {
        const trigger = mountPicker().get('button')
        expect(trigger.text()).toContain('Creeper')
        expect(trigger.text()).toContain('CREEPER')
    })

    it('finds choices by label and key and picks one with enter', async () => {
        mountPicker()
        await openIt()

        await search('spider')
        expect(options()).toEqual(['SpiderSPIDER', 'Cave SpiderCAVE_SPIDER'])

        const input = await search('cave_')
        input.dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter'}))
        await nextTick()

        expect(wrapper!.emitted('update:modelValue')).toEqual([['CAVE_SPIDER']])
    })

    it('lists a short list without search and picks with the arrow keys', async () => {
        mountPicker({options: ['BLOCKS', 'ENTITIES', 'BOTH'], labels: {BLOCKS: 'Blocks', ENTITIES: 'Entities', BOTH: 'Both'}, modelValue: 'BLOCKS'})
        expect(wrapper!.get('button').text()).not.toContain('BLOCKS')
        await openIt()

        expect(document.body.querySelector('[role="dialog"] input')).toBeNull()
        expect(options()).toEqual(['Blocks', 'Entities', 'Both'])
        const list = document.body.querySelector<HTMLElement>('[role="listbox"]')!
        list.dispatchEvent(new KeyboardEvent('keydown', {key: 'ArrowDown'}))
        list.dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter'}))
        await nextTick()

        expect(wrapper!.emitted('update:modelValue')).toEqual([['ENTITIES']])
    })

    it('draws a compact trigger that neither drags nor scrolls the canvas', () => {
        const trigger = mountPicker({compact: true}).get('button')
        expect(trigger.classes()).toEqual(expect.arrayContaining(['nodrag', 'nowheel', 'h-6', 'text-xs']))
        expect(trigger.text()).toBe('Creeper')
    })
})
