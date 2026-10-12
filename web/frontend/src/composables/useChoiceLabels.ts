/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {choiceLabel, type ChoiceOf} from '~/util/graph/choiceLabels'

/**
 * Names choices in the editor's language: entity types, effects, items and other registry entries by Minecraft's own
 * names, enums by the editor's translations, anything else by a name made from the value. Loads Minecraft's names
 * for the session's version; until they arrive, registry entries get names made from their keys.
 */
export function useChoiceLabels() {
    const assets = useItemAssets()
    const {t, te, locale} = useI18n()

    watch(assets.version, () => {
        void assets.load()
    }, {immediate: true})

    function translate(key: string): string | undefined {
        return te(key) ? t(key) : undefined
    }

    /**
     * @param value the stored value, e.g. `CREEPER` or `minecraft:speed`
     * @param of the graph type or payload schema enum it belongs to
     * @returns its name
     */
    function label(value: string, of: ChoiceOf): string {
        return choiceLabel(value, of, {items: assets.items.value, names: assets.names.value}, locale.value, translate)
    }

    /**
     * @param values the stored values
     * @param of the graph type or payload schema enum they belong to
     * @returns their names by value
     */
    function labels(values: readonly string[], of: ChoiceOf): Record<string, string> {
        return Object.fromEntries(values.map(value => [value, label(value, of)]))
    }

    return {label, labels}
}
