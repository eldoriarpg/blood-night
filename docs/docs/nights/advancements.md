---
title: Advancements
sidebar_position: 6
---

# Advancements

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks
what you type. This page shows the files, so you know what the editor writes for you.
:::

Blood nights come with their own advancement tab, "Blood Night". Players see it after their first blood night. Each
new advancement pops up in the corner of the screen, like the ones of the game.

| Advancement | How to get it |
|---|---|
| Still Breathing | Be alive in the world when a blood night ends. |
| Night Slayer | Kill 10 special mobs in one blood night. |
| Bestiary | Kill every special mob of the [store](../mobs/store.md) once. The tab shows how many are left. |
| Warded | Put on armor with [Night Ward V](../mobs/drops-and-items.md#blood-night-enchantments). |
| Dragon's Blood | Be close by when the Blood Dragon dies. |

Mobs you make yourself don't count for Bestiary, but they do count for Night Slayer.

The tab comes from BloodNight's own data pack. It loads by itself.

## Switching them off

Set `advancements` in `config.yml`:

```yaml
advancements: false
```

Players then earn nothing new. What they already have stays.
