/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/**
 * A field's label: its translation under `settings.fields`, else its property name split into words.
 *
 * @param key the property name
 * @param translate the translation lookup
 * @param exists whether a translation exists
 * @returns the label
 */
export function fieldLabel(key: string, translate: (key: string) => string, exists: (key: string) => boolean): string {
    const translation = `settings.fields.${key}`
    if (exists(translation)) return translate(translation)
    const words = key.replaceAll(/([a-z0-9])([A-Z])/g, '$1 $2').toLowerCase()
    return words.charAt(0).toUpperCase() + words.slice(1)
}
