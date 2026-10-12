/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {itemIndex, textureUrl} from '~/api/assets'
import {itemAssetsState} from '~/util/itemAssetsState'
import {enchantmentName, itemChoices, translated} from '~/util/items/itemChoices'

/**
 * Minecraft's item icons and names for the session's Minecraft version, loaded once per version and shared by every
 * item picker. While they load or when the web backend cannot fetch them, pickers show plain keys.
 */
export function useItemAssets() {
    const data = itemAssetsState()
    const session = useEditorSession()
    const {locale} = useI18n()

    const version = computed(() => session.payload.value?.choices?.minecraftVersion ?? '')

    async function load(): Promise<void> {
        const wanted = version.value
        if (wanted === '' || (data.value.version === wanted && data.value.status !== 'failed')) return
        data.value = {version: wanted, status: 'loading', items: {}, names: {}}
        try {
            const index = await itemIndex(wanted)
            if (data.value.version === wanted) {
                data.value = {version: wanted, status: 'ready', items: index.items ?? {}, names: index.names ?? {}}
            }
        } catch {
            if (data.value.version === wanted) data.value = {version: wanted, status: 'failed', items: {}, names: {}}
        }
    }

    function icon(texture: string | undefined): string | undefined {
        return texture && version.value ? textureUrl(version.value, texture) : undefined
    }

    /**
     * @param withItems whether the payload's items are offered, before the materials
     * @param withMaterials whether the server's materials are offered
     * @returns what an item picker offers
     */
    function choices(withItems: boolean, withMaterials = true) {
        return computed(() => itemChoices(
            withMaterials ? session.payload.value?.choices?.materials ?? [] : [],
            withItems ? session.payload.value?.items ?? {} : {},
            {items: data.value.items, enchantments: data.value.names.enchantment ?? {}},
            locale.value,
        ))
    }

    /**
     * @param key an enchantment key, e.g. `minecraft:sharpness`
     * @returns its name in the editor's language
     */
    function enchantment(key: string): string {
        return enchantmentName(key, data.value.names.enchantment?.[key], locale.value)
    }

    /**
     * @param registry a registry of Minecraft's, e.g. `entity_type`
     * @param key an entry's key, e.g. `minecraft:creeper`
     * @returns its name in the editor's language, none while not loaded or when Minecraft has none
     */
    function entry(registry: string, key: string): string | undefined {
        return translated(data.value.names[registry]?.[key], locale.value)
    }

    return {
        items: computed(() => data.value.items),
        names: computed(() => data.value.names),
        status: computed(() => data.value.status),
        version,
        load,
        icon,
        choices,
        enchantment,
        entry,
    }
}
