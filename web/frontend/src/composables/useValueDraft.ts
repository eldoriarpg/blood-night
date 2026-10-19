/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

/**
 * A value being typed before it is saved, read and written as a number by number fields and as text by text fields;
 * each reads nothing while the draft holds the other kind.
 */
export function useValueDraft() {
    const draft = ref<string | number | undefined>(undefined)
    const numberDraft = computed<number | undefined>({
        get: () => typeof draft.value === 'number' ? draft.value : undefined,
        set: value => {
            draft.value = value
        },
    })
    const textDraft = computed<string | undefined>({
        get: () => typeof draft.value === 'string' ? draft.value : undefined,
        set: value => {
            draft.value = value
        },
    })
    return {draft, numberDraft, textDraft}
}
