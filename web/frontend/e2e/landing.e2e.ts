/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {expect, test} from '@playwright/test'

/** The landing page tells a visitor where the editor is opened from. */
test('the landing page points to the command that opens the editor', async ({page}) => {
    await page.goto('/')

    await expect(page.getByText('/bloodnight editor')).toBeVisible()
})
