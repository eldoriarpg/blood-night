/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {defineConfig, devices} from '@playwright/test'

/**
 * The end-to-end suite. It walks the running application in real browsers, against the address
 * `BN_E2E_URL` names, `http://localhost:4000` unless told otherwise. Nothing starts a server for
 * it yet: bring up the frontend and the backend first.
 *
 * <p>Specs are named after their feature as `<feature>.e2e.ts`, which Playwright's default pattern
 * does not find, so `testMatch` names the pattern.
 */
export default defineConfig({
    testDir: './e2e',
    outputDir: './e2e/results',
    testMatch: /.*\.e2e\.ts/,
    timeout: 60_000,
    expect: {timeout: 15_000},
    retries: process.env.CI ? 1 : 0,
    fullyParallel: true,
    reporter: process.env.CI ? [['html', {outputFolder: 'e2e/report'}], ['list']] : 'list',

    use: {
        baseURL: process.env.BN_E2E_URL || 'http://localhost:4000',
        actionTimeout: 30_000,
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
    },

    projects: [
        {name: 'chromium', use: {...devices['Desktop Chrome']}},
        {name: 'firefox', use: {...devices['Desktop Firefox']}},
    ],

    webServer: process.env.CI || process.env.BN_E2E_URL ? undefined : {
        command: 'npx nuxi dev',
        url: 'http://localhost:4000',
        reuseExistingServer: true,
        timeout: 180_000,
    },
})
