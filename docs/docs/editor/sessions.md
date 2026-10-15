---
title: Open the editor
sidebar_position: 1
---

# Open the editor

The web editor lets you change mobs, macros, items and settings in your browser. Several people can work in it at
the same time.

It runs at [bloodnight.eldoria.de](https://bloodnight.eldoria.de). You don't need to set anything up. If you want,
you can also [run your own](../getting-started/editor-service.md).

## The steps

1. Run `/bloodnight editor` in game or in the console.
2. Click the link you get. The editor opens in your browser.
3. Make your changes. They are saved by themselves.
4. Click **Mark as done** in the editor.
5. Run `/bloodnight editor apply` on the server.

Nothing changes on your server before step 5. After that the session stays open: keep editing, mark it as done again
and apply again, as often as you like.

## Working together

While a session is open, everyone who runs `/bloodnight editor` joins it with their own link.

When someone works on a part, like one mob or the settings of one world, that part is theirs for a while. Others see
"is editing this" and can only look at it. This way nobody overwrites someone else's work.

## Who may change what

Everyone in a session sees everything. What they may change depends on their permissions:

| Permission | May change |
|---|---|
| `bloodnight.editor.mobs` | Mobs |
| `bloodnight.editor.macros` | Macros |
| `bloodnight.editor.items` | Registered items |
| `bloodnight.editor.worlds` | The settings of the worlds |
| `bloodnight.editor.server` | The server-wide settings |
| `bloodnight.editor.drops` | The global drops |

The rest is read only for them. `bloodnight.command.editor` includes all six, so admins can change everything. To let
a builder edit only mobs, give them `bloodnight.command.editor` and set the other five to `false`.

## Revisions

Every save is a **revision**. In the Revisions tab you see all of them. Click **Restore** to go back to an older one.
Restoring adds a new revision, so nothing is ever lost.

## Applying

`/bloodnight editor apply` checks the result first:

- Every mob has a valid name and kind.
- Every item a mob uses exists.
- No file on the server changed since the session opened, or since its last apply.

If something is wrong, nothing changes and the session stays open. Fix it in the editor and try again.

If someone changed a file on the server in the meantime, `apply` stops. `/bloodnight editor apply force` applies
anyway and replaces those changes.

Before applying, BloodNight copies all files into `backups/` with the date and time. Then it replaces the files,
reloads and builds every mob again.

### After applying

The session stays open. The editor shows when it was last applied and how many changes came since. To apply those
changes, mark the session as done again and run `/bloodnight editor apply` once more. If nothing changed since the
last apply, `apply` tells you so and does nothing.

## Other commands

| Command | What it does |
|---|---|
| `/bloodnight editor new` | Closes the open session and starts a fresh one |
| `/bloodnight editor cancel` | Closes the session |

A session ends when you cancel it, when you start a new one, or after 7 days without use.
