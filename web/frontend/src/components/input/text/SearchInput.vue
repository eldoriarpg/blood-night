/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref, useAttrs} from 'vue'
import {useI18n} from 'vue-i18n'
import AppIcon from '@/components/display/AppIcon.vue'

/**
 * A search field with a magnifier in front and a button that clears it.
 *
 * <p>Layout and identity belong to the box, behaviour belongs to the field. Callers size this
 * component by class and find it by test id, so those stay on the root. What moves to the input is
 * what only an input can answer: the keys a caller listens for and the ARIA a screen reader reads.
 */
defineOptions({inheritAttrs: false})

const model = defineModel<string>()

withDefaults(defineProps<{
  placeholder?: string
  disabled?: boolean
}>(), {
  placeholder: undefined,
})

const {t} = useI18n()
const attrs = useAttrs()

/**
 * Whether an attribute belongs to the input rather than to the box around it.
 *
 * @param name the attribute's name
 * @returns true for ARIA, the role and listeners
 */
function belongsToField(name: string): boolean {
  return name.startsWith('aria-') || name === 'role' || name.startsWith('on')
}

const fieldAttrs = computed(() => Object.fromEntries(Object.entries(attrs).filter(([name]) => belongsToField(name))))

const boxAttrs = computed(() => Object.fromEntries(Object.entries(attrs)
    .filter(([name]) => !belongsToField(name) && name !== 'class' && name !== 'style')))

const inputEl = ref<HTMLInputElement | null>(null)

/** Puts the cursor into the field. */
function focus() {
  inputEl.value?.focus()
}

/** Empties the field and keeps the cursor in it. */
function clear() {
  model.value = ''
  focus()
}

defineExpose({focus})
</script>

<template>
  <div v-bind="boxAttrs" :class="['relative w-full min-w-0', attrs.class as string]" :style="attrs.style as string">
    <span class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-ink-faint">
      <AppIcon :icon="['fas', 'magnifying-glass']" class="h-3.5 w-3.5"/>
    </span>
    <input
        ref="inputEl"
        v-model="model"
        :aria-label="placeholder"
        v-bind="fieldAttrs"
        :disabled="disabled"
        :placeholder="placeholder"
        type="search"
        class="w-full pl-9 pr-9 py-2 rounded-control border border-line bg-surface text-sm text-ink transition-colors duration-150 outline-none hover:border-ink-faint focus:border-focus focus-visible:outline-none disabled:opacity-50 disabled:cursor-not-allowed [&::-webkit-search-cancel-button]:hidden"
    />
    <button
        v-if="model"
        type="button"
        :aria-label="t('search.clear')"
        :title="t('search.clear')"
        class="absolute inset-y-0 right-0 flex items-center pr-3 text-ink-faint transition-colors hover:text-ink"
        @click="clear"
    >
      <AppIcon :icon="['fas', 'xmark']" class="h-3.5 w-3.5"/>
    </button>
  </div>
</template>
