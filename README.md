# Pano LimboAuth

> **This is a fork of [Elytrium/LimboAuth](https://github.com/Elytrium/LimboAuth).**
> Original developer and copyright owner: **Elytrium**. Licensed under **AGPL-3.0**, which this fork
> respects and preserves in full (see [`LICENSE`](LICENSE)). All credit for LimboAuth belongs to
> Elytrium — this fork only adds an integration API on top; changes are recorded in
> [`NOTICE.md`](NOTICE.md).

LimboAuth is an authentication system built inside a virtual server (Limbo) for Velocity proxies.
This Pano fork stays a **drop-in replacement** — the plugin id is still `limboauth` — and adds a
small, public API so external plugins can delegate password verification and manage accounts.

## Why this fork exists

Upstream LimboAuth has no supported way to:

- delegate password verification to an **external authority** (for example a website / CMS account
  system), or
- **create, update, or remove accounts** from another plugin through a stable public API.

[Pano](https://panocms.com) needs both so a Minecraft server can authenticate players against its
website accounts — the same way plugins integrate with AuthMe-Reloaded on Spigot. The additions are
generic and reusable by **any** Velocity plugin, not just Pano.

## What is extended

### `net.elytrium.limboauth.api.ExternalPasswordProvider`

A callback interface you register to take over password checks:

- **`verifyPassword(String lowercaseNickname, String password)`** — return `TRUE` to accept the
  login, `FALSE` to reject it, or `null` to fall back to LimboAuth's own stored-password check. This
  lets an external system be the source of truth while still allowing purely local accounts. Note
  that accounts provisioned as externally managed have no local password, so they are never locally
  verifiable — the provider must answer `TRUE`/`FALSE` for them (returning `null` would leave them
  unable to log in).

### New public `LimboAuth` methods

- **`setExternalPasswordProvider(ExternalPasswordProvider provider)`** — register (or clear, with
  `null`) the external password provider above.
- **`getExternalPasswordProvider()`** — return the currently registered provider (or `null`).
- **`isRegistered(String nickname)`** — check whether an account already exists.
- **`registerPlayer(String nickname, UUID uuid, String ip, String password)`** — create a normal,
  password-backed account.
- **`provisionExternalPlayer(String nickname, UUID uuid, String ip)`** — create an account
  authenticated externally (no local password), for players whose credentials live in the external
  authority.
- **`unregisterPlayer(String nickname)`** — delete an account.
- **`changePlayerPassword(String nickname, String newPassword)`** — set a new password for an
  existing account.

## How to use

Grab the running LimboAuth plugin instance from Velocity's `PluginManager`, then register your
provider and/or call the account methods:

```java
import com.velocitypowered.api.plugin.PluginContainer;
import net.elytrium.limboauth.LimboAuth;
import java.util.UUID;

LimboAuth limboAuth = (LimboAuth) proxyServer.getPluginManager()
    .getPlugin("limboauth")
    .flatMap(PluginContainer::getInstance)
    .orElseThrow();

// Delegate password checks to your own authority.
// verifyPassword(String lowercaseNickname, String password) -> Boolean
limboAuth.setExternalPasswordProvider((lowercaseNickname, password) -> {
  if (!myAccountSystem.knows(lowercaseNickname)) {
    return null;                                              // null = fall back to LimboAuth's local check
  }
  return myAccountSystem.verify(lowercaseNickname, password); // TRUE = accept, FALSE = reject
});

// High-level account management from your plugin.
UUID uuid = player.getUniqueId();
String ip = player.getRemoteAddress().getAddress().getHostAddress();
if (!limboAuth.isRegistered("Notch")) {
  // Externally managed: no local password is stored, so it is never locally
  // verifiable — your ExternalPasswordProvider is always consulted for it.
  limboAuth.provisionExternalPlayer("Notch", uuid, ip);
}
```

Because the plugin id is unchanged (`limboauth`), this fork is a **drop-in replacement** for the
original jar — existing configs, databases, and dependents keep working.

## See also

- [LimboAPI](https://github.com/Elytrium/LimboAPI) — library by Elytrium for sending players to virtual servers (called limbo).
- [LimboFilter](https://github.com/Elytrium/LimboFilter) — bot-filtering solution by Elytrium, built on LimboAPI.

## Features (inherited from LimboAuth)

- Supports [H2](https://www.h2database.com/html/main.html), [MySQL](https://www.mysql.com/about/), [PostgreSQL](https://www.postgresql.org/about/) [databases](https://en.wikipedia.org/wiki/Database);
- [Geyser](https://wiki.geysermc.org) [Floodgate](https://wiki.geysermc.org/floodgate/) support;
- Hybrid ([Floodgate](https://wiki.geysermc.org/floodgate/)/Online/Offline) mode support;
- Uses [BCrypt](https://en.wikipedia.org/wiki/Bcrypt) - the best [hashing algorithm](https://en.wikipedia.org/wiki/Cryptographic_hash_function) for password;
- Ability to migrate from [AuthMe](https://www.spigotmc.org/resources/authmereloaded.6269/)-alike plugins;
- Ability to block weak passwords;
- [TOTP](https://en.wikipedia.org/wiki/Time-based_one-time_password) [2FA](https://en.wikipedia.org/wiki/Help:Two-factor_authentication) support;
- Ability to set [UUID](https://minecraft.wiki/w/Universally_unique_identifier) from [database](https://en.wikipedia.org/wiki/Database);
- Highly customisable config - you can change all the messages the plugin sends, or just disable them;
- [MCEdit](https://www.mcedit.net/about.html) schematic world loading;
- And more...

## Commands and permissions

### Player

- ***limboauth.commands.destroysession* | /destroysession** - Destroy Account Auth Session Command
- ***limboauth.commands.premium* | /license or /premium** - Command Makes Account Premium
- ***limboauth.commands.unregister* | /unregister** - Unregister Account Command
- ***limboauth.commands.changepassword* | /changepassword** - Change Account Password Command
- ***limboauth.commands.totp* | /totp** - 2FA Management Command
- ***limboauth.commands.***\* - Gives All Player Permissions

### Admin

- ***limboauth.admin.forceunregister* | /forceunregister** - Force Unregister Account Command
- ***limboauth.admin.forcechangepassword* | /forcechangepassword** - Force Change Account Password Command
- ***limboauth.admin.forceregister* | /forceregister** - Force Registration Account Command
- ***limboauth.admin.forcelogin* | /forcelogin** - Force Login Account Command
- ***limboauth.admin.reload* | /lauth reload** - Reload Plugin Command
- ***limboauth.admin.***\* - Gives All Admin Permissions

## Credits & license

LimboAuth was created by **Elytrium** and is licensed under **AGPL-3.0**. This fork preserves that
license (see [`LICENSE`](LICENSE)) and documents its modifications in [`NOTICE.md`](NOTICE.md). If
LimboAuth is useful to you, please consider supporting the original developer through the
[upstream repository](https://github.com/Elytrium/LimboAuth).
