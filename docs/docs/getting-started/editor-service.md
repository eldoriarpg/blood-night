---
title: Your own editor service
sidebar_position: 3
---

# Your own editor service

The web editor runs at [bloodnight.eldoria.de](https://bloodnight.eldoria.de). BloodNight uses it by default, so
most servers never need this page.

Some owners like to run everything themselves. You can do that too.

## What you need

- A machine with [Docker](https://docs.docker.com/engine/install/) and Docker Compose.
- An address for it that your Minecraft server **and** your players can reach, like `https://editor.example.com`.

`localhost` does not work here. Your Minecraft server usually runs on another machine, and your players' browsers
always do.

## Set it up

1. Make a folder for the editor and go into it.
2. Download the two files:

   ```bash
   curl -o compose.yaml https://bloodnight.docs.eldoria.de/self-hosting/compose.yaml
   curl -o .env https://bloodnight.docs.eldoria.de/self-hosting/env.example
   ```

3. Open `.env` and fill it in:

   ```bash
   EDITOR_ADDRESS=https://editor.example.com
   DB_PASSWORD=a-long-random-password
   PORT=4000
   BLOODNIGHT_VERSION=latest
   ```

   | Setting | What it is |
   |---|---|
   | `EDITOR_ADDRESS` | The address everyone uses to reach the editor. No `/` at the end. |
   | `DB_PASSWORD` | A password for the database. Only the editor uses it. |
   | `PORT` | The port the editor listens on, on this machine. |
   | `BLOODNIGHT_VERSION` | Which editor version to run. `latest` is the newest. |

4. Start it:

   ```bash
   docker compose up -d
   ```

5. Make `EDITOR_ADDRESS` lead to this machine's `PORT`. Most people use a web server like
   [Caddy](https://caddyserver.com) or nginx in front of it. They also give you HTTPS.

## Connect your Minecraft server

Set the same address in your Minecraft server's `plugins/BloodNight/config.yml`:

```yaml
editor:
  url: https://editor.example.com
```

Then run `/bloodnight reload` and try `/bloodnight editor`.

You can also set it from outside, for example when the server runs in a container: the environment variable
`BLOODNIGHT_EDITOR_URL` or the Java option `-Dbloodnight.editor.url=...` win over the file.

## What runs inside

| Part | What it does |
|---|---|
| `frontend` | The editor website. It is the only part you open to the outside. It passes requests for `/api` on to `web`. |
| `web` | The editor service. It keeps the editor sessions. |
| `postgres` | The database for the sessions. |

## Updating

```bash
docker compose pull
docker compose up -d
```

Your sessions stay. They live in a Docker volume.

## Behind a proxy

The editor limits how many sessions one address opens per hour. To know who is asking, it trusts the addresses a
proxy passes on, but only from proxies it trusts. By default that is every private address, which covers the
frontend and a web server on the same machine. If your web server or a service like Cloudflare sits somewhere else,
add its addresses to `TRUSTED_PROXIES` in `.env`, separated by commas:

```bash
TRUSTED_PROXIES=172.16.0.0/12,10.0.0.0/8,192.168.0.0/16,127.0.0.1,::1,203.0.113.0/24
```
