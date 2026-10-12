/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useId} from 'vue'
import FieldLabel from '@/components/typography/FieldLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'

/**
 * One input with its label above it and, where there is one, a line underneath saying what it is for.
 *
 * <p>A form of eight fields is eight of these rather than eight hand-built wrappers, which is what keeps
 * the labels, the spacing and the hint text the same everywhere without anybody remembering to make them
 * so.
 *
 * <p>The slot is handed the `id` the label points at; binding it on the input is what ties the two
 * together for a screen reader.
 */
defineProps<{
  label: string
  /** Whether the field may be left as it is, which the label marks. */
  hint?: boolean
  /** A line under the field, for the ones whose consequence is not obvious from the label. */
  help?: string
}>()

const fieldId = useId()
</script>

<template>
  <div class="space-y-1">
    <FieldLabel :for="fieldId" :hint="hint">{{ label }}</FieldLabel>
    <slot :id="fieldId"/>
    <MutedText v-if="help" size="sm" tag="p">{{ help }}</MutedText>
  </div>
</template>
