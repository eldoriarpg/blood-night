/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {existsSync, readFileSync} from 'node:fs'
import {resolve} from 'node:path'
import {describe, expect, it} from 'vitest'
import {DOCS_URL, LINKED_PAGES, docsLink, headingSlug, nodeDocsLink} from '~/util/docsLinks'
import {EDITOR_AREAS} from '~/views/editorview/editorArea'

describe('docsLink', () => {
    it('points every area at a page that exists in the docs', () => {
        for (const area of EDITOR_AREAS) {
            const link = docsLink(area)
            expect(link.startsWith(DOCS_URL + '/')).toBe(true)
            const page = link.slice(DOCS_URL.length + 1).split('#')[0]
            expect(existsSync(resolve(__dirname, '../../../../docs/docs', page + '.md')), page).toBe(true)
        }
    })
})

const DOCS = resolve(__dirname, '../../../../docs/docs')

function anchors(page: string): string[] {
    return readFileSync(resolve(DOCS, page + '.md'), 'utf8').split('\n').filter(line => /^#{2,3} /.test(line)).map(line => headingSlug(line.replace(/^#+ /, '')))
}

describe('links into the docs', () => {
    it('lead to pages and parts that exist', () => {
        for (const link of LINKED_PAGES) {
            const [page = '', anchor] = link.slice(1).split('#')
            expect(existsSync(resolve(DOCS, page + '.md')), page).toBe(true)
            if (anchor) expect(anchors(page), link).toContain(anchor)
        }
    })

    it('make anchors as the docs do', () => {
        expect(headingSlug('How many drops?')).toBe('how-many-drops')
        expect(nodeDocsLink('living_entity', 'Do Once')).toBe(`${DOCS_URL}/reference/nodes/living-entity#do-once`)
    })
})

