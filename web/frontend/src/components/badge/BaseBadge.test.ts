/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import BaseBadge from './BaseBadge.vue'
import ErrorBadge from './ErrorBadge.vue'

/** A badge carries the colours its variant names, on the shared pill. */
describe('BaseBadge', () => {
    it('paints the background and the text it is given', () => {
        const badge = mount(BaseBadge, {props: {bgClass: 'bg-raised', textClass: 'text-held'}, slots: {default: 'Held'}})

        expect(badge.classes()).toEqual(expect.arrayContaining(['rounded-full', 'bg-raised', 'text-held']))
        expect(badge.text()).toBe('Held')
    })

    it('reads in the body text colour unless told otherwise', () => {
        expect(mount(BaseBadge, {props: {bgClass: 'bg-raised'}}).classes()).toContain('text-ink')
    })

    it('shows a problem in the problem colours', () => {
        expect(mount(ErrorBadge).classes()).toEqual(expect.arrayContaining(['bg-problem-bg', 'text-problem']))
    })
})
