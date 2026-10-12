/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import NodeValue from '~/views/editorview/workspace/NodeValue.vue'
import type {ConfigView} from '~/util/graph/graphView'

/**
 * One setting of a node, edited on the node: in its settings block, or beside the pin it belongs to, such as the
 * value of a switch's case. Its choices carry their names in the editor's language; a name field suggests the names
 * related nodes already use.
 */
const props = defineProps<{
  field: ConfigView
  readOnly: boolean
  /** The name read out for the field, the field's own name when not given. */
  label?: string
}>()

const emit = defineEmits<{
  save: [value: unknown]
}>()

const names = useChoiceLabels()
const labels = computed(() => Object.keys(props.field.labels).length > 0 ? props.field.labels : names.labels(props.field.options, {typeId: props.field.typeId}))
</script>

<template>
  <div :class="field.pin === 'number' ? 'w-20' : 'w-28'" class="shrink-0">
    <NodeValue
        :format="field.format"
        :label="label ?? field.name"
        :labels="labels"
        :options="field.options"
        :pin="field.pin"
        :read-only="readOnly"
        :suggestions="field.suggestions"
        :value="field.value"
        compact
        @save="value => emit('save', value)"
    />
  </div>
</template>
