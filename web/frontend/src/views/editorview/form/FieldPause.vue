/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import RangeSlider from '@/components/input/range/RangeSlider.vue'

/**
 * The pause between two ambience sounds as one range, "a sound every 10 to 40 seconds", saving the shortest and the
 * longest pause together once a handle is let go. The slider reaches two minutes, or further when the settings
 * already hold a longer pause.
 */
const props = defineProps<{
  label: string
  /** Where the shortest and longest pause are stored, e.g. `.../soundSettings`. */
  path: string
  shortest: number
  longest: number
  readOnly: boolean
}>()

const {t} = useI18n()
const changes = useEditorChanges()
const range = ref<[number, number]>([1, 1])

watch(() => [props.shortest, props.longest], ([shortest, longest]) => {
  range.value = [Math.min(shortest ?? 1, longest ?? 1), Math.max(shortest ?? 1, longest ?? 1)]
}, {immediate: true})

const max = computed(() => Math.max(120, props.longest, props.shortest))
const marks = computed(() => [
  {value: 1, label: t('editor.sounds.seconds', {count: 1})},
  {value: 60, label: t('editor.sounds.minute')},
  {value: max.value, label: t('editor.sounds.seconds', {count: max.value})},
])

function save(value: [number, number]): void {
  if (props.readOnly || (value[0] === props.shortest && value[1] === props.longest)) return
  void changes.apply([
    {op: 'add', path: `${props.path}/minInterval`, value: value[0]},
    {op: 'add', path: `${props.path}/maxInterval`, value: value[1]},
  ])
}
</script>

<template>
  <LabelledField :label="label">
    <RangeSlider
        v-model="range"
        :disabled="readOnly"
        :format="value => t('editor.sounds.seconds', {count: value})"
        :from-label="t('editor.sounds.pauseFrom')"
        :marks="marks"
        :max="max"
        :min="1"
        :step="1"
        :to-label="t('editor.sounds.pauseTo')"
        @commit="save"
    />
  </LabelledField>
</template>
