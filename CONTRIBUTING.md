# Contributing to GainExpDisplay

Thanks for helping out! Bug reports and ideas go in the
[issue tracker](https://github.com/PunJarrr/GainExpDisplay/issues). This page explains how to build the
plugin, test it and release it.

## How the project works

- One jar supports every server and version. It is compiled against the **oldest and smallest** API we
  support, Spigot 1.17.1, so anything that compiles also exists on Paper, Purpur, Folia and every newer
  version. The jar targets Java 16, which Minecraft 1.17 servers can run on.
- MiniMessage (part of [Adventure](https://docs.papermc.io/adventure/)) is bundled into the jar and
  renamed to `dev.punjarrr.gainExpDisplay.libs.kyori`, so it never clashes with the copy Paper ships.
- Messages are sent by a `Messenger` (`src/main/java/.../text/`):
  - `NativeMessenger` on Paper, Purpur and Folia, using the server's own Adventure text.
  - `BungeeMessenger` on plain Spigot, using colour codes and Spigot's BungeeCord chat API.
  The server log says which one is used: `Sending messages with ...`.
- `gradle.properties` holds the plugin version, the Minecraft versions shown on the store pages and the
  store page IDs.
- `store/` holds the store page descriptions (Modrinth, Hangar, SpigotMC).

Java 17 is needed to build. The test servers also need Java 21 (1.20.5 – 1.21.x) and Java 25 (26.x);
Gradle downloads any that are missing.

## Common tasks

Build the plugin into `build/libs/GainExpDisplay-<version>.jar` and run the unit tests:

```bash
./gradlew build
```

Run a test server with the plugin (the newest Paper, or a specific one):

```bash
./gradlew runServer
```

```bash
./gradlew runPaper_1_17_1
```

List every test server:

```bash
./gradlew -q printTestServers
```

Test servers are Paper 1.17.1 – 26.3, Purpur, Folia and Spigot at the oldest and newest versions. Each
runs in `run/<platform>-<version>/`. Spigot is built with Spigot's BuildTools the first time, which takes
a few minutes.

## Smoke test

`scripts/smoke-test.sh` boots each test server and checks that:

1. the plugin enables, using the right message path (Paper's Adventure, or BungeeCord chat on Spigot)
2. `/ged reload` over RCON answers "Configuration reloaded successfully."
3. a test bot ([Mineflayer](https://github.com/PrismarineJS/mineflayer), in `tools/smoke-bot/`) joins,
   picks up XP while holding a damaged Mending pickaxe and sees `Mending Repair: +10 (1071/1561)`, then
   picks up XP with an empty hand and sees `Gain XP: +5`
4. the server log has no errors.

Mineflayer can't join Minecraft versions newer than it supports (currently 26.2 and 26.3). Those servers
still get checks 1, 2 and 4, and the report says "bot skipped".

Running it accepts the [Minecraft EULA](https://aka.ms/MinecraftEULA) for the test servers. It needs
Python 3 and Node.js 18 or newer.

```bash
scripts/smoke-test.sh paper-1.21.11 spigot-1.17.1
```

```bash
scripts/smoke-test.sh all
```

Logs are written to `build/smoke-test/`. The GitHub `Build` workflow runs the smoke test on every server
for each push.

## In-game checklist

The smoke test can't see colours or check every setting. Before a release, join a test server
(`./gradlew runServer`, then connect to `localhost`) and check:

- [ ] Picking up XP shows `Gain XP: +<amount>` in green and yellow.
- [ ] Holding a damaged Mending item and picking up XP shows `Mending Repair: +<amount> (<durability>/<max>)`,
      and the durability never goes above the maximum.
- [ ] Set `lang: th_TH` (or another language) in `config.yml`, run `/ged reload`: the messages change language.
- [ ] Set `require-permission: true`, run `/ged reload`, then
      `/lp user <you> permission set gainexpdisplay.mending false` (with LuckPerms):
      the Mending message stops but the XP message still shows.
- [ ] Set `gain-xp.enabled: false`, run `/ged reload`: the XP message stops.
- [ ] A gradient message such as `<gradient:red:blue>Gain XP: +%amount%</gradient>` shows as a gradient,
      on Paper and on Spigot (`./gradlew runSpigot_1_21_11`).
- [ ] As a non-operator, `/ged reload` says you don't have permission.

## Pull requests

- Run `./gradlew build` (unit tests) and the smoke test for at least one Paper and one Spigot server.
- Only use API that exists in Spigot 1.17.1. If you need something newer, look it up at runtime the way
  `Durability` does.

## Releasing (maintainers)

1. Bump `version` in `gradle.properties` and add the changes to `CHANGELOG.md`.
2. Commit and push, and wait for the `Build` workflow to pass.
3. Create a GitHub release tagged `v<version>` (for example `v2.2.0`), with the changes as its description.

The `Publish` workflow then checks the tag matches the version, builds the jar, uploads it to
[Modrinth](https://modrinth.com/plugin/gainexpdisplay) and
[Hangar](https://hangar.papermc.io/PunJarrr/GainExpDisplay) with the release description as the
changelog, and attaches it to the GitHub release.

**SpigotMC has no upload API**, so upload the jar there by hand: on
[the resource page](https://www.spigotmc.org/resources/130682/), choose *Post Resource Update*, attach
the jar from the GitHub release, and paste the changelog.

The workflow needs two repository secrets (Settings → Secrets and variables → Actions):

- `MODRINTH_TOKEN`: a [Modrinth personal access token](https://modrinth.com/settings/pats) with the
  *Create versions* scope.
- `HANGAR_API_TOKEN`: a [Hangar API key](https://hangar.papermc.io/auth/settings/api-keys) with the
  *create_version* permission.

To check the Modrinth upload settings locally without uploading anything, run `./gradlew publishMods`
with no tokens set; it does a dry run.
