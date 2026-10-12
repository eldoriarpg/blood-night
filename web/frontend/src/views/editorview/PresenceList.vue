/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
/** Who has the session open, as initials; this participant first and in the accent colour. */
const props = defineProps<{
  names: readonly string[]
  you: string
}>()

const {t} = useI18n()

const people = computed(() => [...props.names]
  .sort((a, b) => Number(b === props.you) - Number(a === props.you) || a.localeCompare(b))
  .map(name => ({name, initials: initials(name), you: name === props.you})))

function initials(name: string): string {
  return name.slice(0, 2).toUpperCase()
}
</script>

<template>
  <ul :aria-label="t('editor.header.online')" class="flex items-center gap-1">
    <li
        v-for="person in people"
        :key="person.name"
        :class="person.you ? 'bg-focus' : 'bg-held'"
        :title="person.you ? t('editor.header.you', {name: person.name}) : person.name"
        class="flex size-8 items-center justify-center rounded-full text-xs font-bold text-ground"
    >
      {{ person.initials }}
    </li>
  </ul>
</template>
