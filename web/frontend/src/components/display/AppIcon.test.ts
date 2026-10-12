/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/** @vitest-environment happy-dom */
import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import AppIcon from './AppIcon.vue'

/** An icon named as a pair or by its bare name reaches FontAwesome as the same pair. */
describe('AppIcon', () => {
    it('hands a pair to FontAwesome as it stands', () => {
        const icon = mount(AppIcon, {props: {icon: ['fas', 'xmark']}})

        expect(icon.find('[data-testid="icon"]').attributes('data-icon')).toBe('fas,xmark')
    })

    it('reads a bare name as an icon of the solid set', () => {
        const icon = mount(AppIcon, {props: {icon: 'check'}})

        expect(icon.find('[data-testid="icon"]').attributes('data-icon')).toBe('fas,check')
    })
})
