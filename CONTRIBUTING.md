# Contributing

## Branches

| Branch | Holds | Pull requests go into |
|---|---|---|
| `main` | What is released, plus changes waiting for their release | |
| `feature/<name>` | One feature | `main`, squash-merged |
| `fix/<name>` | One fix | `main`, squash-merged |
| `chore/<name>` | Upkeep without a visible change: versions, build, CI | `main`, squash-merged |
| `renovate/<name>` | One dependency update by Renovate | `main` |

CI verifies a feature, fix or chore branch through its pull request only; pushing the branch alone runs nothing.
Open a draft pull request to have a branch verified before it is ready for review. Renovate branches, `main` and
version tags are verified on push.

## Testing on a server

`./gradlew :core:runServer` builds the plugin and starts a Paper server with it in `core/run/`.

`DB_PASSWORD=<password> docker compose -f .docker/compose.yaml up -d --build` starts BloodNight's web backend (for
now the editor service) on port 8080 with its database, the address the plugin uses by default. The service reads its settings from the
environment, see `ServiceConfig` in `web/backend`.

## Pull requests

The title says what the change does, as a short imperative sentence: `Add extension damage and death triggers`.
It becomes the commit message of the squash merge.

The description lists the changes as short bullets, written for someone who uses the plugin or the editor rather
than for someone reading the diff. Name the issues it closes (`Closes #4`).

Pull requests are labelled automatically: by the branch they come from (`t:feature`, `t:fix`, `t:deps`, `t:chore`,
`t:other`) and by what they change (`a:api`, `a:graph`, `a:nodes`, `a:core`, `a:editor`, `a:build`, `a:tests`,
`a:docs`, `ci`). The rules are in `.github/labeler.yml`.

## Writing

- Commit messages are short: one imperative subject line, a body of one or two sentences only when the reason is
  not obvious.
- Code documents itself. The only comments are doc comments on classes and methods, and `TODO` lines for real gaps.
- The em dash is not used anywhere in this project: not in code, comments, commit messages or documents. Use a
  comma, a colon, parentheses or a full stop, whichever the sentence needs.
