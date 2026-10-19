/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {EditorItem, MaterialInfo} from '~/api/generated/payload'

/** What every part of the item form is given: the item, what its material offers, and whether it may be changed. */
export interface ItemSectionProps {
    item: EditorItem
    info: MaterialInfo
    readOnly: boolean
}

/** What every part of the item form sends: one field of the item and its new value, unset to clear it. */
export interface ItemSectionEmits {
    save: [key: keyof EditorItem, value: unknown]
}
