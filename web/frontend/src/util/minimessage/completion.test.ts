/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {completeTag, tagCandidates} from '~/util/minimessage/completion'

describe('completeTag', () => {
    it('completes the only tag and closes it', () => {
        expect(completeTag('<bo', 3)).toEqual({text: '<bold>', caret: 6})
    })

    it('completes what the candidates share, then the first one', () => {
        expect(completeTag('<dark', 5)).toEqual({text: '<dark_', caret: 6})
        expect(completeTag('<dark_', 6)).toEqual({text: '<dark_blue>', caret: 11})
    })

    it('leaves a tag taking arguments open', () => {
        expect(completeTag('<grad', 5)).toEqual({text: '<gradient:', caret: 10})
    })

    it('closes the innermost open tag', () => {
        expect(completeTag('<red><bold>Hi</', 15)).toEqual({text: '<red><bold>Hi</bold>', caret: 20})
        expect(completeTag('<red><bold>Hi</bold></', 22)).toEqual({text: '<red><bold>Hi</bold></red>', caret: 26})
    })

    it('completes placeholders and only where a tag is typed', () => {
        expect(completeTag('<na', 3, ['name'])).toEqual({text: '<name>', caret: 6})
        expect(completeTag('text', 4)).toBeUndefined()
        expect(completeTag('<red>', 5)).toBeUndefined()
    })

    it('completes in the middle of the text', () => {
        expect(completeTag('<re Blade', 3)).toEqual({text: '<red> Blade', caret: 5})
    })
})

describe('tagCandidates', () => {
    it('lists the tags the typed part can become', () => {
        expect(tagCandidates('<st', 3)?.candidates).toEqual(['strikethrough'])
    })
})
