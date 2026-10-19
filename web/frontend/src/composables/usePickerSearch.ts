/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Ref, WatchSource} from 'vue'

/**
 * The search of a picker: what is typed, everything that matches, the first matches shown and the one the arrow
 * keys point at. Whenever the picker opens the search is cleared and the pointer starts at the current value; whenever
 * the search or one of `resets` changes, it starts at the top.
 *
 * @param open whether the picker is open
 * @param search the matches for what is typed
 * @param limit how many matches are shown at most
 * @param current whether a match is the picker's current value
 * @param resets more things that start the pointer at the top when they change, e.g. a group filter
 */
export function usePickerSearch<T>(
    open: Ref<boolean>,
    search: (query: string) => readonly T[],
    limit: number,
    current: (match: T) => boolean,
    resets: WatchSource[] = [],
) {
    const query = ref('')
    const active = ref(0)
    const matches = computed(() => search(query.value))
    const shown = computed(() => matches.value.slice(0, limit))

    watch([query, ...resets], () => {
        active.value = 0
    })

    watch(open, now => {
        if (!now) return
        query.value = ''
        active.value = Math.max(0, shown.value.findIndex(current))
    })

    /** Moves the pointer by some matches, wrapping around at either end. */
    function step(by: number): void {
        if (shown.value.length > 0) active.value = (active.value + by + shown.value.length) % shown.value.length
    }

    return {query, active, matches, shown, step}
}
