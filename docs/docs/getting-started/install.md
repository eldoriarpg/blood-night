---
title: Install
sidebar_position: 1
---

# Install BloodNight

:::tip[Use the web editor]
The easiest way to change these settings is the [web editor](../editor/sessions.md). It shows every option and checks what you
type. This page shows the files, so you know what the editor writes for you.
:::

## What you need

- A [Paper](https://papermc.io) or [Folia](https://papermc.io/software/folia) server, version 26.3 or newer.
- Java 25.

The same jar runs on Paper and on Folia. BloodNight does not run on Spigot.

## Steps

1. Download `BloodNight.jar`.
2. Put it into your server's `plugins` folder.
3. Start the server.

BloodNight creates its folder at `plugins/BloodNight`. No world has blood nights yet. The setup turns them on.

## Set it up

Run `/bloodnight setup`. You get a link to the [web editor](../editor/sessions.md). It walks you through a few
questions:

1. **Old BloodNight.** Only when the old plugin's files are there. You can take them over, see
   [Coming from the old BloodNight](coming-from-legacy.md).
2. **Worlds.** Which worlds get blood nights.
3. **How often.** Rarely, sometimes, often, or with the moon.
4. **How hard.** Gentle, normal or brutal.
5. **Look and sound.** Red sky, heartbeat, and whether players get the resource pack.
6. **Mobs.** Pick mobs from the [store](../mobs/store.md). The `classic` pack is ticked.

Click **Finish**. Then run `/bloodnight editor apply` on the server. Your blood nights start.

Until the setup is done, the console and operators who join are reminded of it. You can run the setup again at any
time. It starts from your current settings.

## What is in the folder

| File | What it holds |
|---|---|
| `config.yml` | Settings for the whole server |
| `worlds/<world id>.yml` | Settings for one world. Each world gets its own file. |
| `mobs.yml` | Your special mobs |
| `items.yml` | Items you registered or made in the editor |
| `loot_tables.yml` | Your [loot tables](../mobs/loot-tables.md) |
| `macros.yml` | Macros: pieces of behaviour you can reuse |
| `backups/` | Copies of all files, made before the editor changes anything |

The world files are named after the world's ID, not its name. The ID is a long code like
`288eafe9-3057-42e3-9fce-11dcc622705e`. You find it in the file `uid.dat` in the world's folder. The easier way is
the [web editor](../editor/sessions.md): it shows every world by its name.

:::tip
You can change the files while the server runs. Use `/bloodnight reload` afterwards.
:::

When BloodNight loads a settings file, it adds keys that are missing, with their default value. It never touches
`mobs.yml`, `items.yml` or `macros.yml` this way. Those are yours.

## New versions

BloodNight looks for a new version when the server starts and every 12 hours after that. When there is one, it
says so in the console. Players with the permission `bloodnight.updates` see a message when they join. Operators
have it at first. BloodNight never downloads anything by itself.

To turn this off, set this in `config.yml`:

```yaml
updates:
  notice: false
```

## Usage numbers

BloodNight sends anonymous numbers to [bStats](https://bstats.org/plugin/bukkit/BloodNight/34688): how many worlds
have blood nights, how they pick them, how many mobs you have and which plugins it works with. No names, no worlds,
no players. To switch it off for all plugins, set `enabled: false` in `plugins/bStats/config.yml`.
