/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {describe, expect, it} from 'vitest'
import {preview} from './preview'

describe('MiniMessage preview', () => {
    it('colours, decorates and fills in placeholders', () => {
        expect(preview('<red><bold><name>', {name: 'Blood Night'})).toEqual([{text: 'Blood Night', color: '#ff5555', bold: true}])
    })

    it('ends a tag where it is closed and starts over at reset', () => {
        expect(preview('<gold>A</gold> b <#123456>c<reset>d')).toEqual([
            {text: 'A', color: '#ffaa00'},
            {text: ' b '},
            {text: 'c', color: '#123456'},
            {text: 'd'},
        ])
    })

    it('shows gradients by their first colour and unknown tags as they are', () => {
        expect(preview('<gradient:#ff0000:#0000ff>Hi</gradient> <shadow>')).toEqual([
            {text: 'Hi', color: '#ff0000'},
            {text: ' <shadow>'},
        ])
    })

    it('reads named colours by argument', () => {
        expect(preview('<color:dark_red>x')).toEqual([{text: 'x', color: '#aa0000'}])
    })
})
