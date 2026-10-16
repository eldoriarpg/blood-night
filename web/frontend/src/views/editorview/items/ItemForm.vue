/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import ErrorButton from '@/components/button/ErrorButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import ButtonRow from '@/components/button/ButtonRow.vue'
import Alert from '@/components/feedback/Alert.vue'
import Modal from '@/components/feedback/Modal.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import FieldHint from '@/components/typography/FieldHint.vue'
import MiniMessageText from '@/components/display/MiniMessageText.vue'
import MiniMessageInput from '~/views/editorview/form/MiniMessageInput.vue'
import MutedText from '@/components/typography/MutedText.vue'
import PageHeader from '@/components/typography/PageHeader.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import FieldTexts from '~/views/editorview/form/FieldTexts.vue'
import ItemPicker from '~/views/editorview/form/ItemPicker.vue'
import ItemEnchantments from '~/views/editorview/items/ItemEnchantments.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import {ItemKind} from '~/api/generated/payload'
import {pointer} from '~/util/patch/jsonPointer'
import {section} from '~/util/session/sections'
import {nameItem, type ItemEntry} from '~/util/session/itemEntries'
import {enchantmentLines} from '~/util/items/enchantmentLines'

/**
 * One item: its material, its MiniMessage name and lore with a preview of the item's tooltip as players see it while
 * they are typed, its enchantments, whether it glints and whether its tooltip lists the enchantments. An item the server registered keeps
 * its material and everything else it carries; only an item made in the editor can change its material or be deleted.
 */
const props = defineProps<{
  entry: ItemEntry
}>()

const emit = defineEmits<{
  deleted: []
}>()

const locks = useEditorLocks()
const changes = useEditorChanges()
const {t} = useI18n()
const name = ref('')
const lore = ref<string[]>([])
const NAME_COLOR = '#ffffff'
const LORE_COLOR = '#aa00aa'
const ENCHANTMENT_COLOR = '#aaaaaa'
const CURSE_COLOR = '#ff5555'
const confirming = ref(false)
const MINI_MESSAGE_EXAMPLE = '<red>Blood <bold>Blade'

const heldBy = computed(() => locks.heldByOther(section.item(props.entry.id)))
const denied = computed(() => !locks.mayChange(section.item(props.entry.id)))
const readOnly = computed(() => heldBy.value !== null || denied.value)
const defined = computed(() => props.entry.item.kind === ItemKind.DEFINED)
const assets = useItemAssets()
const materialChoices = assets.choices(false)
const session = useEditorSession()
const glint = computed(() => props.entry.item.glint === true ? 'ON' : props.entry.item.glint === false ? 'OFF' : 'GAME')
const glintLabels = computed(() => ({GAME: t('editor.items.glint.GAME'), ON: t('editor.items.glint.ON'), OFF: t('editor.items.glint.OFF')}))
const enchanted = computed(() => enchantmentLines(props.entry.item.enchantments, key => assets.enchantment(key), session.payload.value?.choices?.enchantments ?? {}))
const materialName = computed(() => materialChoices.value.find(choice => choice.key === props.entry.item.material)?.name ?? props.entry.item.material)

watch(() => props.entry.item, item => {
  name.value = item.name ?? ''
  lore.value = [...item.lore]
}, {immediate: true})

function path(...keys: string[]): string {
  return pointer('items', props.entry.id, ...keys)
}

function saveName(): void {
  if (readOnly.value) return
  const operations = nameItem(props.entry.id, props.entry.item, name.value)
  if (operations.length > 0) void changes.apply(operations)
}

function saveMaterial(value: string | undefined): void {
  if (readOnly.value || !defined.value || !value || value === props.entry.item.material) return
  void changes.apply([{op: 'replace', path: path('material'), value}])
}

function save(key: 'lore' | 'enchantments' | 'hideEnchantments', value: unknown): void {
  if (readOnly.value || JSON.stringify(value) === JSON.stringify(props.entry.item[key])) return
  void changes.apply([{op: 'add', path: path(key), value}])
}

function saveGlint(choice: string | undefined): void {
  const glint = choice === 'ON' ? true : choice === 'OFF' ? false : undefined
  if (readOnly.value || glint === (props.entry.item.glint ?? undefined)) return
  void changes.apply([glint === undefined ? {op: 'remove', path: path('glint')} : {op: 'add', path: path('glint'), value: glint}])
}

function remove(): void {
  confirming.value = false
  void changes.apply([{op: 'remove', path: path()}]).then(applied => {
    if (applied) emit('deleted')
  })
}
</script>

<template>
  <main class="overflow-y-auto px-6 py-5">
    <div class="flex flex-wrap items-center gap-3">
      <PageHeader class="flex-1 font-mono">{{ entry.id }}</PageHeader>
      <InfoBadge>{{ t(`editor.items.kind.${entry.item.kind}`) }}</InfoBadge>
    </div>
    <Alert v-if="heldBy" class="mt-3" variant="held">{{ t('editor.items.heldBy', {name: heldBy}) }}</Alert>
    <Alert v-else-if="denied" class="mt-3" variant="held">{{ t('editor.permission.readOnly') }}</Alert>
    <MutedText v-if="!defined" class="mt-3" size="sm" tag="p">{{ t('editor.items.registeredHint') }}</MutedText>
    <div class="flex max-w-2xl flex-col gap-4 pt-4">
      <LabelledField :label="t('editor.items.material')">
        <ItemPicker
            :choices="materialChoices"
            :label="t('editor.items.material')"
            :model-value="entry.item.material"
            :read-only="readOnly || !defined"
            @update:model-value="saveMaterial"
        />
      </LabelledField>
      <LabelledField v-slot="{id}" :label="t('editor.items.name')">
        <MiniMessageInput :id="id" v-model="name" :disabled="readOnly" :placeholder="MINI_MESSAGE_EXAMPLE" @focusout="saveName"/>
        <FieldHint>{{ t('editor.items.miniMessage', {example: MINI_MESSAGE_EXAMPLE}) }}</FieldHint>
      </LabelledField>
      <FieldTexts :label="t('editor.items.lore')" :read-only="readOnly" :value="entry.item.lore" mini-message @edit="lore = $event" @save="value => save('lore', value)"/>
      <figure :aria-label="t('editor.items.tooltip')" class="flex flex-col gap-1">
        <figcaption class="text-xs text-ink-muted">{{ t('editor.items.tooltip') }}</figcaption>
        <div class="w-fit min-w-48 max-w-full rounded-sm border-2 border-[#25015b] bg-[#100010f0] px-2 py-1.5 text-sm leading-snug">
          <div><MiniMessageText :base="NAME_COLOR" :text="name || materialName"/></div>
          <div v-for="line in entry.item.hideEnchantments ? [] : enchanted" :key="line.text" :style="{color: line.curse ? CURSE_COLOR : ENCHANTMENT_COLOR}" class="font-[Minecraft,monospace]">{{ line.text }}</div>
          <div v-for="(line, index) in lore" :key="index"><MiniMessageText :base="LORE_COLOR" :text="line"/></div>
        </div>
      </figure>
      <ItemEnchantments :read-only="readOnly" :value="entry.item.enchantments" @save="value => save('enchantments', value)"/>
      <LabelledField :label="t('editor.items.glint.label')">
        <ChoicePicker :label="t('editor.items.glint.label')" :labels="glintLabels" :model-value="glint" :options="['GAME', 'ON', 'OFF']" :read-only="readOnly" @update:model-value="saveGlint"/>
      </LabelledField>
      <ToggleSetting :disabled="readOnly" :label="t('editor.items.hideEnchantments')" :model-value="Boolean(entry.item.hideEnchantments)" @update:model-value="value => save('hideEnchantments', value)"/>
      <div v-if="defined && !readOnly"><ErrorButton compact icon="trash" @click="confirming = true">{{ t('editor.items.delete') }}</ErrorButton></div>
    </div>
    <Modal v-model="confirming" size="sm">
      <div class="space-y-4">
        <SubHeader>{{ t('editor.items.confirm', {id: entry.id}) }}</SubHeader>
        <MutedText size="sm" tag="p">{{ t('editor.sheet.confirmHint') }}</MutedText>
        <ButtonRow align="end" pair>
          <SecondaryButton data-cancel @click="confirming = false">{{ t('editor.sheet.cancel') }}</SecondaryButton>
          <PrimaryButton @click="remove">{{ t('editor.items.delete') }}</PrimaryButton>
        </ButtonRow>
      </div>
    </Modal>
  </main>
</template>
