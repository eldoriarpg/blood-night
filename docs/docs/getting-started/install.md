---
title: Install
sidebar_position: 1
---

# Install BloodNight

## What you need

- A [Paper](https://papermc.io) server, version 26.3 or newer.
- Java 25.

BloodNight does not run on Spigot or Folia.

## Steps

1. Download `BloodNight.jar`.
2. Put it into your server's `plugins` folder.
3. Start the server.

That's it. BloodNight creates its folder at `plugins/BloodNight`.

## What is in the folder

| File | What it holds |
|---|---|
| `config.yml` | Settings for the whole server |
| `worlds/<world id>.yml` | Settings for one world. Each world gets its own file. |
| `global_drops.yml` | Drops all worlds can use |
| `mobs.yml` | Your special mobs |
| `items.yml` | Items you registered or made in the editor |
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
