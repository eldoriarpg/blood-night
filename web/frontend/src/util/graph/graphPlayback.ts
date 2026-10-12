/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {ref, type Ref} from 'vue'
import type {Step} from '~/util/graph/executionOrder'

/** How long the lit nodes stay after the last one before they fade. */
export const LINGER_MILLIS = 1500

/** A running playback of a chain: which nodes and wires are lit, in which order, and which one is reached now. */
export interface GraphPlayback {
    /** The step number of each lit node, from 1. */
    order: Ref<ReadonlyMap<string, number>>
    /** The lit wires, by the canvas's wire ids. */
    wires: Ref<ReadonlySet<string>>
    /** The node reached last, or null between playbacks. */
    current: Ref<string | null>
    /** Plays steps, stopping a playback still running. */
    play(steps: readonly Step[]): void
    /** Stops and clears the playback. */
    stop(): void
}

/**
 * Lights up the nodes and wires of a chain one after another at the times the steps give, keeps them lit a moment
 * after the last and then clears them.
 *
 * @returns the playback
 */
export function graphPlayback(): GraphPlayback {
    const order = ref<ReadonlyMap<string, number>>(new Map())
    const wires = ref<ReadonlySet<string>>(new Set())
    const current = ref<string | null>(null)
    let timers: ReturnType<typeof setTimeout>[] = []

    function stop(): void {
        timers.forEach(timer => clearTimeout(timer))
        timers = []
        order.value = new Map()
        wires.value = new Set()
        current.value = null
    }

    function reach(step: Step, number: number): void {
        order.value = new Map(order.value).set(step.node, number)
        if (step.wire) wires.value = new Set(wires.value).add(step.wire)
        current.value = step.node
    }

    function play(steps: readonly Step[]): void {
        stop()
        steps.forEach((step, index) => timers.push(setTimeout(() => reach(step, index + 1), step.at)))
        const end = Math.max(0, ...steps.map(step => step.at)) + LINGER_MILLIS
        timers.push(setTimeout(stop, end))
    }

    return {order, wires, current, play, stop}
}
