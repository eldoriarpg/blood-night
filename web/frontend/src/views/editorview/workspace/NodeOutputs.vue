/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {Handle, Position} from '@vue-flow/core'
import PortTooltip from '~/views/editorview/workspace/PortTooltip.vue'
import NodeField from '~/views/editorview/workspace/NodeField.vue'
import {handles, type ConfigView, type ExecutionView, type PortGroup} from '~/util/graph/graphView'

/**
 * The right column of a drawn node: its execution outputs, each with the setting of the same id beside it, such as the
 * value a switch's case matches, then its data outputs under their group headers.
 */
const props = defineProps<{
  executions: readonly ExecutionView[]
  /** Settings drawn beside an execution output, by the output's id. */
  cases: ReadonlyMap<string, ConfigView>
  groups: readonly PortGroup[]
  readOnly: boolean
}>()

const emit = defineEmits<{
  config: [field: string, value: unknown]
}>()

</script>

<template>
  <div class="flex min-w-0 flex-col items-end gap-1.5">
    <div v-for="port in executions" :key="port.id" class="relative flex min-h-4 items-center gap-1.5 pr-4">
      <span class="text-ink-muted">{{ port.name }}</span>
      <NodeField v-if="props.cases.get(port.id)" :field="props.cases.get(port.id)!" :label="port.name" :read-only="readOnly" @save="value => emit('config', props.cases.get(port.id)!.id, value)"/>
      <Handle :id="handles.execOut(port.id)" :class="['pin-exec', port.connected && 'pin-filled']" :position="Position.Right" type="source"/>
    </div>
    <template v-for="group in groups" :key="group.name ?? ''">
      <span v-if="group.name" class="pr-4 pt-1 text-[10px] uppercase tracking-wider text-ink-faint" data-testid="port-group">{{ group.name }}</span>
      <PortTooltip v-for="port in group.ports" :key="port.id" :port="port" side="right">
        <div class="relative flex h-4 max-w-full items-center pr-4">
          <span class="truncate text-ink-muted">{{ port.name }}</span>
          <Handle :id="handles.output(port.id)" :class="['pin-data', `pin-${port.pin}`, port.connected && 'pin-filled']" :position="Position.Right" type="source"/>
        </div>
      </PortTooltip>
    </template>
  </div>
</template>
