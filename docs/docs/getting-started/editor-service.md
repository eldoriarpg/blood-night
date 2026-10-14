---
title: Your own editor service
sidebar_position: 3
---

# Your own editor service

The web editor runs at [bloodnight.eldoria.de](https://bloodnight.eldoria.de). BloodNight uses it by default, so
most servers never need this page.

Some owners like to run everything themselves. You can do that too.

## How it works

The editor is a small website with its own server, the **editor service**. Your Minecraft server sends its mobs and
settings there. You edit them in the browser. Then your Minecraft server fetches the result.

Your Minecraft server finds the editor service with these settings in `config.yml`:

```yaml
editor:
  url: https://bloodnight.eldoria.de
  timeoutSeconds: 30
```

- `url` is the address of the editor service.
- `timeoutSeconds` is how long your server waits for an answer.

## Run it with Docker

The editor service needs a PostgreSQL database. Docker starts everything for you. In the BloodNight repository run:

```bash
DB_PASSWORD=choose-a-password ./toolchain.sh docker-up
```

This starts three parts:

| Part | Address |
|---|---|
| Database | inside Docker only |
| Editor service | `http://localhost:8080` |
| Editor website | `http://localhost:4000` |

Now set `editor.url` to `http://localhost:8080` and run `/bloodnight reload`.

## Let other players in

If players on other computers should use your editor, they must be able to reach it. Set these before you start it:

| Setting | What it does |
|---|---|
| `EDITOR_URL` | The link players get. `{id}` and `{token}` are filled in. Example: `https://editor.example.com/session/{id}#{token}` |
| `ALLOWED_ORIGINS` | The address of your editor website, so browsers may talk to the service |
