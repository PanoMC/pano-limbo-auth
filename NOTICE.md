# NOTICE

This project, **pano-limbo-auth**, is a **derivative work** of **LimboAuth**.

- **Original work:** LimboAuth — <https://github.com/Elytrium/LimboAuth>
- **Original author / copyright owner:** Elytrium — Copyright (C) 2021 - 2025 Elytrium
- **License:** GNU Affero General Public License, version 3 (AGPL-3.0)

LimboAuth is licensed under the AGPL-3.0. This fork is distributed under the **same license**, which
is preserved unchanged (see [`LICENSE`](LICENSE)) along with the original per-file copyright headers
(see `HEADER.txt`). All original credit for LimboAuth belongs to Elytrium.

## Modifications made in this fork

Maintained by the [Pano](https://panocms.com) project (github.com/PanoMC/pano-limbo-auth). The
functional change is a public integration API that lets external plugins delegate password
verification and manage accounts — capabilities upstream LimboAuth does not expose.

### Added integration API

- **`net.elytrium.limboauth.api.ExternalPasswordProvider`** — callback interface whose
  `verifyPassword(...)` may return `TRUE` (accept), `FALSE` (reject), or `null` (fall back to
  LimboAuth's own stored-password check), allowing an external authority to be the source of truth.
- **New public `LimboAuth` methods:**
  - `setExternalPasswordProvider(provider)` / `getExternalPasswordProvider()` — register/clear/read
    the external password provider.
  - `isRegistered(nickname)` — check whether an account exists.
  - `registerPlayer(nickname, uuid, ip, password)` — create a password-backed account.
  - `provisionExternalPlayer(nickname, uuid, ip)` — create an externally-authenticated account whose
    stored hash is a non-verifiable placeholder (only the provider can authenticate it).
  - `unregisterPlayer(nickname)` — delete an account and evict it from the session cache.
  - `changePlayerPassword(nickname, newPassword)` — change an existing account's password.

  The verify hook additionally short-circuits placeholder (externally-managed) rows so they can never
  be authenticated by LimboAuth's own local hash check.

The plugin id remains `limboauth`, so the fork stays a drop-in replacement for the original.

### Branding, packaging & CI changes

- Renamed the produced jar base name to `pano-limbo-auth` (`settings.gradle`); package names and the
  `@Plugin` id are unchanged.
- Rewrote `README.md` as a fork notice crediting Elytrium and documenting the added API.
- Removed the Elytrium-owned publishing/distribution targets that this fork must not push to: the
  "Upload to Modrinth" CI steps (Modrinth project `4iChqdl8`) in both GitHub workflows, and the
  `elytrium-repo` Maven publishing repository in `build.gradle`. Removed the upstream bStats
  (project `13700`) and Modrinth badges from the README.
- Added `jitpack.yml` so the fork can be built and consumed via JitPack.

### Reproducible / offline build

Upstream LimboAuth resolves LimboAPI, ElytriumCommons and a custom `velocity-proxy` build only from
`maven.elytrium.net`, so its build breaks whenever that host is unreachable. `scripts/fetch-deps.sh`
reconstructs those three dependency sets from reachable public sources (the LimboAPI GitHub release
asset, ElytriumCommons built from source, and the Velocity proxy jar from PaperMC's download API) into
a git-ignored `libs/` directory that `build.gradle` consumes via `flatDir`. `maven.elytrium.net`
remains declared as a fallback, so nothing changes when it is up. All three are still their original
projects under their original licenses; they are fetched, not re-published.

No copyright headers, license text, or original authorship attribution have been removed or altered.
