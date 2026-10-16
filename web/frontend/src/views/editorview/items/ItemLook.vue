/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import SectionHeader from '@/components/typography/SectionHeader.vue'
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import ColorInput from '@/components/input/color/ColorInput.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import type {EditorItem, MaterialInfo} from '~/api/generated/payload'
import {keyWords} from '~/util/items/itemLook'

/**
 * How an item looks: whether it glints, the model a resource pack gives it, its dye colour where it takes one and its
 * armour trim where it takes one. A cleared field is saved as unset, so the item looks as its material does.
 */
const props = defineProps<{
  item: EditorItem
  info: MaterialInfo
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [key: keyof EditorItem, value: unknown]
}>()

const {t} = useI18n()
const session = useEditorSession()
const model = ref('')
const modelData = ref<number | undefined>(undefined)
const DEFAULT_COLOR = '#a06540'

watch(() => props.item, item => {
  model.value = item.model ?? ''
  modelData.value = item.modelData ?? undefined
}, {immediate: true})

const glint = computed(() => props.item.glint === true ? 'ON' : props.item.glint === false ? 'OFF' : 'GAME')
const glintLabels = computed(() => ({GAME: t('editor.items.glint.GAME'), ON: t('editor.items.glint.ON'), OFF: t('editor.items.glint.OFF')}))
const patterns = computed(() => session.payload.value?.choices?.trimPatterns ?? [])
const materials = computed(() => session.payload.value?.choices?.trimMaterials ?? [])
const patternLabels = computed(() => Object.fromEntries(patterns.value.map(key => [key, keyWords(key)])))
const materialLabels = computed(() => Object.fromEntries(materials.value.map(key => [key, keyWords(key)])))

function saveGlint(choice: string | undefined): void {
  emit('save', 'glint', choice === 'ON' ? true : choice === 'OFF' ? false : undefined)
}

function saveModel(): void {
  const key = model.value.trim()
  emit('save', 'model', key === '' ? undefined : key)
}

function saveModelData(): void {
  emit('save', 'modelData', modelData.value === undefined ? undefined : Math.round(modelData.value))
}

function saveTrim(part: 'pattern' | 'material', key: string | undefined): void {
  if (!key) {
    emit('save', 'trim', undefined)
    return
  }
  const current = props.item.trim ?? {pattern: patterns.value[0] ?? key, material: materials.value[0] ?? key}
  emit('save', 'trim', {...current, [part]: key})
}
</script>

<template>
  <fieldset class="flex flex-col gap-3">
    <legend class="pb-1"><SectionHeader>{{ t('editor.items.look.title') }}</SectionHeader></legend>
    <LabelledField :label="t('editor.items.glint.label')">
      <ChoicePicker :label="t('editor.items.glint.label')" :labels="glintLabels" :model-value="glint" :options="['GAME', 'ON', 'OFF']" :read-only="readOnly" @update:model-value="saveGlint"/>
    </LabelledField>
    <LabelledField v-slot="{id}" :help="t('editor.items.look.modelHint')" :label="t('editor.items.look.model')">
      <TextInput :id="id" v-model="model" :disabled="readOnly" class="font-mono" placeholder="myserver:blood_blade" @focusout="saveModel"/>
    </LabelledField>
    <LabelledField v-slot="{id}" :help="t('editor.items.look.modelDataHint')" :label="t('editor.items.look.modelData')">
      <div class="w-32"><NumberInput :id="id" v-model="modelData" :disabled="readOnly" @focusout="saveModelData"/></div>
    </LabelledField>
    <LabelledField v-if="info.dyeable" :label="t('editor.items.look.color')">
      <div class="flex items-center gap-2">
        <ColorInput v-if="item.color" :disabled="readOnly" :label="t('editor.items.look.color')" :model-value="item.color" @update:model-value="value => emit('save', 'color', value)"/>
        <template v-if="!readOnly">
          <SecondaryButton v-if="item.color" compact @click="emit('save', 'color', undefined)">{{ t('editor.items.look.undye') }}</SecondaryButton>
          <SecondaryButton v-else compact @click="emit('save', 'color', DEFAULT_COLOR)">{{ t('editor.items.look.dye') }}</SecondaryButton>
        </template>
      </div>
    </LabelledField>
    <div v-if="info.trimmable" class="flex flex-wrap gap-3">
      <LabelledField :label="t('editor.items.look.trimPattern')" class="min-w-48 flex-1">
        <ChoicePicker :label="t('editor.items.look.trimPattern')" :labels="patternLabels" :model-value="item.trim?.pattern" :options="patterns" :placeholder="t('editor.items.look.noTrim')" :read-only="readOnly" clearable @update:model-value="key => saveTrim('pattern', key)"/>
      </LabelledField>
      <LabelledField v-if="item.trim" :label="t('editor.items.look.trimMaterial')" class="min-w-48 flex-1">
        <ChoicePicker :label="t('editor.items.look.trimMaterial')" :labels="materialLabels" :model-value="item.trim.material" :options="materials" :read-only="readOnly" @update:model-value="key => saveTrim('material', key)"/>
      </LabelledField>
    </div>
  </fieldset>
</template>
