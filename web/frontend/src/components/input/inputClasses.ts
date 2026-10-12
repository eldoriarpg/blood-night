/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
/**
 * The class list of the standard bordered field (text, number, select). Its edge is an inset ring rather than a
 * border, one painted shape that leaves no seams at the rounded corners on scaled displays or zoomed canvases, and
 * the system's own widget look is switched off. Every visible field that is not a search bar takes its base classes
 * from here, so edge, focus, padding and the disabled state stay in lock-step across the form library. Layout (width,
 * height) is up to the caller.
 */
export const BORDERED_INPUT_CLASSES =
    'px-3 py-2 rounded-control appearance-none ring-1 ring-inset ring-line bg-surface text-ink text-sm' +
    ' transition-colors duration-150 outline-none' +
    ' hover:ring-ink-faint focus:ring-focus focus-visible:outline-none' +
    ' disabled:opacity-50 disabled:cursor-not-allowed'

/**
 * The inline borderless variant: the same focus and disabled language as
 * {@link BORDERED_INPUT_CLASSES} without a border, for editing in place inside a row or a node.
 */
export const BORDERLESS_INPUT_CLASSES =
    'px-2 py-1 bg-transparent text-ink text-sm transition-colors duration-150 outline-none' +
    ' rounded-control border border-transparent hover:bg-hover focus:border-focus focus:bg-surface' +
    ' focus-visible:outline-none disabled:opacity-50 disabled:cursor-not-allowed'

/**
 * The small bordered variant for fields drawn inside a graph node: one line of extra-small text, so a value fits
 * beside the pin it belongs to. Same focus and disabled language as {@link BORDERED_INPUT_CLASSES}.
 */
export const COMPACT_INPUT_CLASSES =
    'h-6 px-1.5 rounded-control appearance-none ring-1 ring-inset ring-line bg-ground text-ink text-xs' +
    ' transition-colors duration-150 outline-none hover:ring-ink-faint focus:ring-focus focus-visible:outline-none' +
    ' disabled:opacity-50 disabled:cursor-not-allowed'
