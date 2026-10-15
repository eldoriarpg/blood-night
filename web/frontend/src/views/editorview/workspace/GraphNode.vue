/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {Handle, Position} from '@vue-flow/core'
import BareButton from '@/components/button/BareButton.vue'
import AppIcon from '@/components/display/AppIcon.vue'
import CountBadge from '@/components/badge/CountBadge.vue'
import ColorInput from '@/components/input/color/ColorInput.vue'
import PortTooltip from '~/views/editorview/workspace/PortTooltip.vue'
import NodeHelp from '~/views/editorview/workspace/NodeHelp.vue'
import NodeValue from '~/views/editorview/workspace/NodeValue.vue'
import NodeOutputs from '~/views/editorview/workspace/NodeOutputs.vue'
import NodeField from '~/views/editorview/workspace/NodeField.vue'
import {handles, isGrouped, portGroups, type ConfigView, type NodeView, type PortView} from '~/util/graph/graphView'
import {channelsHex} from '~/util/graph/nodeEdits'
import {caseSlots} from '~/util/graph/caseSlots'

/**
 * A node drawn like a Blueprint node: a header coloured by what the node does, with a question mark explaining it,
 * its settings below the header, white execution pins, data pins in the colour of their value, filled when wired, and
 * a field beside each unwired input to type its value into. A node taking a colour as red, green and blue also gets
 * one colour picker filling all three. Until it is expanded, a node with many outputs, such as one reading every
 * property of an entity, shows only its wired outputs, and a node with optional inputs, such as one setting
 * properties, only those that are wired or typed in. Ports of a node reading or changing a type with many properties
 * are drawn under the type that brings them, the node's own type first. A setting with the id of an execution
 * output, such as the value of a switch's case, is drawn beside that output instead of below the header. Of the
 * numbered cases of a switch or select only those up to the last in use are drawn (see {@link caseSlots}), with a
 * button adding the next and one beside the last clearing it away. The body is slightly see-through, so wires running behind a
 * node can still be followed. A reroute is only a dot with a pin on either side.
 */
const props = defineProps<{
  data: NodeView
  selected?: boolean
  heldBy?: string | null
  readOnly: boolean
  /** The node's number in a playing chain, while it is lit. */
  step?: number
}>()

const emit = defineEmits<{
  literal: [port: string, value: unknown]
  config: [field: string, value: unknown]
  channels: [hex: string]
  play: []
}>()

const {t} = useI18n()
const names = useChoiceLabels()
const expanded = ref(false)
const added = ref<ReadonlySet<string>>(new Set())
const slots = computed(() => caseSlots(props.data, added.value))
const slotInputs = computed(() => props.data.inputs.filter(port => !slots.value.hidden.has(port.id)))
const executions = computed(() => props.data.executions.filter(port => !slots.value.hidden.has(port.id)))

const MANY = 6
const crowded = computed(() => props.data.outputs.length > MANY)
const outputs = computed(() => crowded.value && !expanded.value ? props.data.outputs.filter(port => port.connected) : props.data.outputs)
const inputs = computed(() => expanded.value ? slotInputs.value : slotInputs.value.filter(port => !port.optional || port.connected || typed(port) || slots.value.shown.includes(port.id)))
const hiddenInputs = computed(() => slotInputs.value.length - inputs.value.length)
const hiddenOutputs = computed(() => props.data.outputs.length - outputs.value.length)
const hidden = computed(() => hiddenInputs.value + hiddenOutputs.value)
const moreLabel = computed(() => {
  if (hiddenInputs.value > 0 && hiddenOutputs.value > 0) return t('editor.graph.more.both', {inputs: hiddenInputs.value, outputs: hiddenOutputs.value})
  return hiddenInputs.value > 0 ? t('editor.graph.more.inputs', {count: hiddenInputs.value}) : t('editor.graph.more.outputs', {count: hiddenOutputs.value})
})
const foldable = computed(() => expanded.value || hidden.value > 0)
const grouped = computed(() => isGrouped(props.data))
const inputGroups = computed(() => portGroups(inputs.value, grouped.value))
const outputGroups = computed(() => portGroups(outputs.value, grouped.value))
const editable = computed(() => props.data.config.length > 0 || props.data.inputs.some(port => !port.connected && port.literalAllowed))
const settings = computed(() => props.data.config.filter(field => field.beside === undefined))
const besides = computed(() => new Map(props.data.config.flatMap(field => field.beside === undefined ? [] : [[field.beside, field] as const])))
const colour = computed(() => channelsHex(Object.fromEntries(props.data.inputs.map(port => [port.id, port.literal]))))
const execution = computed(() => props.data.type === 'reroute.execution')

const tone = computed(() => ({
  event: 'bg-node-event',
  action: 'bg-node-action',
  value: 'bg-node-value',
  flow: 'bg-node-flow',
  macro: 'bg-node-macro',
}[props.data.tone]))

function addCase(): void {
  const next = slots.value.next
  if (next) added.value = new Set([...added.value, next])
}

function removeCase(port: string): void {
  const at = slots.value.shown.indexOf(port)
  added.value = new Set([...added.value].filter(id => !slots.value.shown.slice(at).includes(id)))
  const field = besides.value.get(port)
  if (field) emit('config', field.id, null)
}

function typed(port: PortView): boolean {
  return port.literal !== undefined && port.literal !== null && port.literal !== ''
}

function editsInline(port: PortView): boolean {
  return !port.connected && port.literalAllowed
}

function portLabels(port: PortView): Record<string, string> {
  return names.labels(port.options, {typeId: port.typeId})
}

function fieldLabels(field: ConfigView): Record<string, string> {
  return Object.keys(field.labels).length > 0 ? field.labels : names.labels(field.options, {typeId: field.typeId})
}
</script>

<template>
  <div
      v-if="data.reroute"
      :class="selected ? 'border-focus' : 'border-line'"
      :title="data.title"
      class="relative size-4 rounded-full border-2 bg-raised"
      data-testid="reroute"
  >
    <template v-if="execution">
      <Handle :id="handles.execIn" :position="Position.Left" class="pin-reroute" type="target"/>
      <Handle :id="handles.execOut('next')" :position="Position.Right" class="pin-reroute" type="source"/>
    </template>
    <template v-else>
      <Handle :id="handles.input('value')" :position="Position.Left" class="pin-reroute" type="target"/>
      <Handle :id="handles.output('value')" :position="Position.Right" class="pin-reroute" type="source"/>
    </template>
  </div>
  <div
      v-else
      :class="[selected ? 'border-focus' : 'border-line', editable ? 'min-w-72' : 'min-w-60']"
      class="w-max max-w-[36rem] rounded-node border bg-raised/85 text-xs text-ink"
  >
    <div :class="tone" class="flex items-center gap-2 rounded-t-node px-3 py-1.5">
      <span class="text-[10px] uppercase tracking-wider text-white/75">{{ t(`editor.graph.tones.${data.tone}`) }}</span>
      <span class="min-w-0 truncate text-sm font-semibold text-white">{{ data.title }}</span>
      <CountBadge v-if="step !== undefined" :count="step" data-testid="step" small/>
      <span class="ml-auto flex shrink-0 items-center gap-1.5">
        <BareButton
            v-if="data.entry"
            :aria-label="t('editor.graph.play', {name: data.title})"
            :title="t('editor.graph.play', {name: data.title})"
            class="nodrag flex size-4 items-center justify-center rounded-full text-[10px] text-white/75 hover:text-white focus-visible:text-white"
            data-testid="play"
            @click="emit('play')"
        >
          <AppIcon icon="play"/>
        </BareButton>
        <NodeHelp v-if="data.description" :description="data.description" :title="data.title" :type="data.type"/>
      </span>
    </div>
    <div v-if="settings.length > 0 || data.channels" class="grid grid-cols-[auto_minmax(0,1fr)] items-center gap-x-2 gap-y-1 border-b border-line px-3 py-2">
      <template v-for="field in settings" :key="field.id">
        <span class="truncate text-ink-muted">{{ field.name }}</span>
        <NodeValue
            :format="field.format"
            :label="field.name"
            :labels="fieldLabels(field)"
            :options="field.options"
            :pin="field.pin"
            :read-only="readOnly"
            :suggestions="field.suggestions"
            :value="field.value"
            compact
            @save="value => emit('config', field.id, value)"
        />
      </template>
      <template v-if="data.channels">
        <span class="truncate text-ink-muted">{{ t('editor.graph.colour') }}</span>
        <ColorInput :disabled="readOnly" :label="t('editor.graph.colour')" :model-value="colour" class="nodrag" compact @update:model-value="hex => hex && emit('channels', hex)"/>
      </template>
    </div>
    <BareButton v-if="foldable" class="nodrag w-full border-b border-line px-3 py-1 text-right text-ink-faint hover:text-ink" @click="expanded = !expanded">
      {{ expanded ? t('editor.graph.fewer') : moreLabel }}
    </BareButton>
    <div class="flex justify-between gap-4 py-2">
      <div class="flex min-w-0 flex-col gap-1.5">
        <div v-if="data.execIn" class="relative flex h-4 items-center pl-4">
          <Handle :id="handles.execIn" :position="Position.Left" class="pin-exec" type="target"/>
        </div>
        <template v-for="group in inputGroups" :key="group.name ?? ''">
          <span v-if="group.name" class="pl-4 pt-1 text-[10px] uppercase tracking-wider text-ink-faint" data-testid="port-group">{{ group.name }}</span>
          <PortTooltip v-for="port in group.ports" :key="port.id" :port="port" side="left">
            <div class="relative flex min-h-4 items-center gap-1.5 pl-4">
              <Handle :id="handles.input(port.id)" :class="['pin-data', `pin-${port.pin}`, port.connected && 'pin-filled']" :position="Position.Left" type="target"/>
              <span class="min-w-16 truncate text-ink-muted">{{ port.name }}</span>
              <NodeField v-if="besides.get(port.id)" :field="besides.get(port.id)!" :read-only="readOnly" @save="value => emit('config', besides.get(port.id)!.id, value)"/>
              <BareButton
                  v-if="!readOnly && slots.removable === port.id"
                  :aria-label="t('editor.graph.removeCase', {name: port.name})"
                  class="nodrag text-ink-faint hover:text-problem"
                  @click="removeCase(port.id)"
              >
                <AppIcon icon="xmark"/>
              </BareButton>
              <div v-if="editsInline(port)" :class="port.pin === 'number' ? 'w-20' : 'w-28'" class="shrink-0">
                <NodeValue
                    :format="port.format"
                    :label="port.name"
                    :labels="portLabels(port)"
                    :options="port.options"
                    :pin="port.pin"
                    :read-only="readOnly"
                    :value="port.literal"
                    compact
                    @save="value => emit('literal', port.id, value)"
                />
              </div>
            </div>
          </PortTooltip>
        </template>
      </div>
      <NodeOutputs
          :cases="besides"
          :executions="executions"
          :groups="outputGroups"
          :read-only="readOnly"
          :removable="slots.removable"
          @config="(field, value) => emit('config', field, value)"
          @remove="removeCase"
      />
    </div>
    <BareButton
        v-if="!readOnly && slots.next"
        class="nodrag flex w-full items-center justify-center gap-1.5 border-t border-line px-3 py-1 text-ink-faint hover:text-ink"
        @click="addCase"
    >
      <AppIcon icon="plus"/>
      {{ t('editor.graph.addCase') }}
    </BareButton>
    <div v-if="!data.known" class="border-t border-line px-3 py-1 text-ink-faint">{{ data.type }}</div>
    <div v-if="heldBy" class="border-t border-line px-3 py-1 text-held">{{ t('editor.graph.heldBy', {name: heldBy}) }}</div>
  </div>
</template>

<style scoped>
:deep(.vue-flow__handle) {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  border-width: 2px;
  background: var(--color-raised);
}

:deep(.pin-reroute) {
  width: 8px;
  height: 8px;
  min-width: 0;
  min-height: 0;
  border: 0;
  background: var(--color-ink-faint);
}

:deep(.pin-exec) {
  border: 0;
  border-radius: 0;
  background: var(--color-pin-exec);
  clip-path: polygon(0 0, 60% 0, 100% 50%, 60% 100%, 0 100%);
  opacity: 0.55;
}

:deep(.pin-exec.pin-filled) {
  opacity: 1;
}

:deep(.pin-data.pin-filled) {
  background: currentColor;
}

:deep(.pin-entity) { border-color: var(--color-pin-entity); color: var(--color-pin-entity); }
:deep(.pin-number) { border-color: var(--color-pin-number); color: var(--color-pin-number); }
:deep(.pin-boolean) { border-color: var(--color-pin-boolean); color: var(--color-pin-boolean); }
:deep(.pin-text) { border-color: var(--color-pin-text); color: var(--color-pin-text); }
:deep(.pin-location) { border-color: var(--color-pin-location); color: var(--color-pin-location); }
:deep(.pin-item) { border-color: var(--color-pin-item); color: var(--color-pin-item); }
:deep(.pin-other) { border-color: var(--color-pin-other); color: var(--color-pin-other); }
</style>
