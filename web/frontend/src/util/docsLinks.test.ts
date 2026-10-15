/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {existsSync} from 'node:fs'
import {resolve} from 'node:path'
import {describe, expect, it} from 'vitest'
import {DOCS_URL, docsLink} from '~/util/docsLinks'
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
