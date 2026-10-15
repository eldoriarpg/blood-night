/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {changesSinceApply} from '~/util/session/lastApplied'

describe('changesSinceApply', () => {
    it('counts the revisions saved after the applied one', () => {
        expect(changesSinceApply(7, {revision: 4, at: '2026-10-15T12:00:00Z'})).toBe(3)
    })

    it('is 0 right after an apply and before the first one', () => {
        expect(changesSinceApply(4, {revision: 4, at: '2026-10-15T12:00:00Z'})).toBe(0)
        expect(changesSinceApply(4, null)).toBe(0)
    })
})
