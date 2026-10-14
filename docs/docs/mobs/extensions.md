---
title: Riders and mounts
sidebar_position: 4
---

# Riders and mounts

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

A special mob can come with a second mob. We call it the **extension**.

```yaml
extension:
  type: RIDER
  entityType: SPIDER
  equipment: null
```

| `type` | What it means |
|---|---|
| `NONE` | No extension. |
| `RIDER` | The special mob rides on the extension. A zombie riding a spider. |
| `CARRIER` | The extension rides on the special mob. A phantom carrying a blaze. |

`equipment` works like the mob's own [equipment](./custom-mobs.md#equipment).

## They share one life

The special mob and its extension act as one:

- Damage to the extension hurts the special mob instead.
- The extension's health always follows the special mob's health.
- The extension gets the same maximum health as the special mob.
- When the special mob dies, the extension dies too.
- When the blood night ends, both go away.

In a graph, the "Extension" node gives you the extension. The triggers "On Extension Damage" and
"On Extension Death" react to what happens to it.
