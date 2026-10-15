/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {withFallbacks} from '~/util/minimessage/translations'

/** A run of text in one style. */
export interface Segment {
    text: string
    color?: string
    bold?: boolean
    italic?: boolean
    underlined?: boolean
    strikethrough?: boolean
}

type Style = Omit<Segment, 'text'>

/** Minecraft's named colours. */
export const COLORS: Readonly<Record<string, string>> = {
    black: '#000000', dark_blue: '#0000aa', dark_green: '#00aa00', dark_aqua: '#00aaaa', dark_red: '#aa0000',
    dark_purple: '#aa00aa', gold: '#ffaa00', gray: '#aaaaaa', grey: '#aaaaaa', dark_gray: '#555555', dark_grey: '#555555',
    blue: '#5555ff', green: '#55ff55', aqua: '#55ffff', red: '#ff5555', light_purple: '#ff55ff', yellow: '#ffff55',
    white: '#ffffff',
}

const DECORATIONS: Readonly<Record<string, keyof Style>> = {
    bold: 'bold', b: 'bold', italic: 'italic', i: 'italic', em: 'italic', underlined: 'underlined', u: 'underlined',
    strikethrough: 'strikethrough', st: 'strikethrough',
}

function color(name: string): string | undefined {
    if (/^#[0-9a-f]{6}$/i.test(name)) return name
    return COLORS[name.toLowerCase()]
}

/**
 * Renders the MiniMessage a player would see, for a preview in the editor: colours, `<color:...>`, decorations,
 * `<reset>`, closing tags, and gradients and rainbows as their first colour. Texts in the player's language show their
 * English fallback. Placeholders are filled in from the
 * samples; any other tag is shown as it is, as MiniMessage does with tags it does not know.
 *
 * @param text MiniMessage, e.g. `<red><bold><name>`
 * @param samples text for the placeholders, e.g. `{name: 'Blood Night'}`
 * @returns the text in runs of one style each
 */
export function preview(text: string, samples: Readonly<Record<string, string>> = {}): Segment[] {
    const segments: Segment[] = []
    const stack: {tag: string, style: Style}[] = [{tag: '', style: {}}]
    const current = () => stack.at(-1)?.style ?? {}
    const push = (text: string) => {
        if (text === '') return
        const last = segments.at(-1)
        const style = current()
        if (last && JSON.stringify({...last, text: ''}) === JSON.stringify({...style, text: ''})) last.text += text
        else segments.push({text, ...style})
    }
    let rest = withFallbacks(text)
    while (rest !== '') {
        const match = /<(\/?)([a-z0-9_#:.-]+)>/i.exec(rest)
        if (!match) {
            push(rest)
            break
        }
        push(rest.slice(0, match.index))
        rest = rest.slice(match.index + match[0].length)
        const closing = match[1] === '/'
        const [name = '', ...args] = (match[2] ?? '').split(':')
        const tag = name.toLowerCase()
        if (closing) {
            const index = stack.findLastIndex(entry => entry.tag === tag)
            if (index > 0) stack.splice(index)
            else push(match[0])
            continue
        }
        if (tag === 'reset') {
            stack.splice(1)
            continue
        }
        if (tag === 'newline' || tag === 'br') {
            push('\n')
            continue
        }
        const decoration = DECORATIONS[tag]
        const named = color(tag)
        const argument = ['color', 'colour', 'c'].includes(tag) ? color(args[0] ?? '') : undefined
        const gradient = tag === 'gradient' ? color(args[0] ?? '') ?? COLORS.white : tag === 'rainbow' ? COLORS.red : undefined
        const picked = named ?? argument ?? gradient
        if (picked) stack.push({tag, style: {...current(), color: picked}})
        else if (decoration) stack.push({tag, style: {...current(), [decoration]: true}})
        else if (samples[tag] !== undefined) push(samples[tag] ?? '')
        else push(match[0])
    }
    return segments
}
