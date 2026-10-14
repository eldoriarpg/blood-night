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

## Toolchain

Every command goes through `./toolchain.sh`, which runs it in the project's nix environment (`shell.nix`; run
`direnv allow` once, or have `nix-shell` available). Without nix it runs commands directly; then it needs a JDK 17 or
newer to start Gradle (Gradle downloads JDK 25 for the build itself), Node.js 24 (`web/frontend/.nvmrc`) and Docker.
`./toolchain.sh doctor` shows which way commands run and checks these; without nix, run `fe-install` and
`fe-e2e-install` once. `./toolchain.sh help` lists everything; the main ones:

- `./toolchain.sh verify`: what CI checks, Java and frontend.
- `./toolchain.sh be-server`: Paper test server with the plugin in `core/run/`.
- `./toolchain.sh docker-up`: database, web backend on port 8080 and frontend on port 4000.
- `./toolchain.sh fe-dev`: frontend dev server on port 4000, against the backend on port 8080.
- `./toolchain.sh api-types`: regenerates the payload schema, the API description and the frontend types after a
  change to a record or settings class the editor sees; tests and linters fail while they are stale.

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
