/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import DocsLink from '@/components/navigation/DocsLink.vue'
import {nodeDocsLink} from '~/util/docsLinks'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import ErrorButton from '@/components/button/ErrorButton.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import SubHeader from '@/components/typography/SubHeader.vue'
import MutedText from '@/components/typography/MutedText.vue'
import NodeValue from '~/views/editorview/workspace/NodeValue.vue'
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {changingSibling} from '~/util/graph/mutate'
import {configEdit} from '~/util/graph/nodeEdits'
import type {GraphBindings} from '~/util/graph/graphBindings'
import {pinKind} from '~/util/graph/pins'
import {choiceName, isTypeField, pickedTitle, pickedTypes, resolvePicked} from '~/util/graph/pickedTypes'

/** The selected node: what it does, the values typed into its unwired inputs, its config, and deleting it. */
const props = defineProps<{
  node: string
  graph: Graph
  base: string
  catalog: Catalog
  /** The graph's graph-wide types, such as the mob's own entity type for Self. */
  bindings: GraphBindings
  readOnly: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const {changes, editor, apply} = useGraphEdits(() => props)
const {t} = useI18n()
const names = useChoiceLabels()

const instance = computed(() => props.graph.nodes?.[props.node])
const definition = computed(() => (props.catalog.nodes ?? []).find(node => node.id === instance.value?.type))
const values = computed(() => new Map((props.catalog.types ?? []).map(type => [type.id ?? '', type.values ?? []])))
const formats = computed(() => new Map((props.catalog.types ?? []).map(type => [type.id ?? '', type.format])))
const picked = computed(() => pickedTypes(definition.value, instance.value, props.catalog))
const title = computed(() => pickedTitle(definition.value, picked.value, props.catalog) ?? definition.value?.name ?? instance.value?.type)
const literals = computed(() => (changingSibling(props.catalog, instance.value?.type ?? '')?.inputs ?? definition.value?.inputs ?? [])
  .filter(port => port.literal && instance.value?.inputs?.[port.id ?? ''] === undefined)
  .map(port => {
    const type = resolvePicked(port.type, picked.value)
    const options = values.value.get(type?.id ?? '') ?? []
    return {id: port.id ?? '', name: port.name ?? port.id ?? '', pin: pinKind(type), options, labels: names.labels(options, {typeId: type?.id}), format: formats.value.get(type?.id ?? ''), value: instance.value?.literals?.[port.id ?? '']}
  }))
const config = computed(() => (definition.value?.config ?? []).map(field => {
  const type = resolvePicked(field.type, picked.value)
  const choices = isTypeField(field) ? field.choices ?? [] : values.value.get(type?.id ?? '') ?? []
  return {
    id: field.id ?? '',
    name: field.name ?? field.id ?? '',
    pin: isTypeField(field) ? 'other' as const : pinKind(type),
    options: choices,
    labels: isTypeField(field) ? Object.fromEntries(choices.map(choice => [choice, choiceName(choice, props.catalog)])) : names.labels(choices, {typeId: type?.id}),
    format: isTypeField(field) ? undefined : formats.value.get(type?.id ?? ''),
    value: instance.value?.config?.[field.id ?? ''] ?? field.defaultValue,
  }
}))

function saveConfig(field: string, value: unknown): void {
  void apply(configEdit(props.base, props.graph, props.catalog, props.node, field, value))
}

function remove(): void {
  void changes.apply(editor().remove([props.node])).then(applied => {
    if (applied) emit('close')
  })
}
</script>

<template>
  <aside v-if="instance" :aria-label="t('editor.inspector.title')" class="flex max-h-full w-80 flex-col gap-3 overflow-y-auto rounded-panel border border-line bg-surface p-4">
    <div class="flex items-center justify-between">
      <SectionLabel>{{ t('editor.inspector.title') }}</SectionLabel>
      <MutedIconButton :label="t('editor.inspector.close')" icon="xmark" @click="emit('close')"/>
    </div>
    <SubHeader>{{ title }}</SubHeader>
    <span class="font-mono text-xs text-ink-faint">{{ instance.type }} · {{ node }}</span>
    <MutedText v-if="definition?.description" size="sm" tag="p">{{ definition.description }}</MutedText>
    <DocsLink v-if="definition?.category && definition.name" :href="nodeDocsLink(definition.category, definition.name)">{{ t('editor.docs.node') }}</DocsLink>
    <NodeValue
        v-for="port in literals"
        :key="`literal-${port.id}`"
        :format="port.format"
        :label="port.name"
        :labels="port.labels"
        :options="port.options"
        :pin="port.pin"
        :read-only="readOnly"
        :value="port.value"
        @save="value => apply(editor().literal(node, port.id, value))"
    />
    <NodeValue
        v-for="field in config"
        :key="`config-${field.id}`"
        :format="field.format"
        :label="field.name"
        :labels="field.labels"
        :options="field.options"
        :pin="field.pin"
        :read-only="readOnly"
        :value="field.value"
        @save="value => saveConfig(field.id, value)"
    />
    <div v-if="!readOnly"><ErrorButton compact icon="trash" @click="remove">{{ t('editor.inspector.delete') }}</ErrorButton></div>
  </aside>
</template>
