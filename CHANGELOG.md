# Changelog

## 2.3.0

### Added
- Messages in each player's own language. With the new `per-player-language` option, players see the
  translation that matches their Minecraft language setting (for example Thai when their game is set to
  ภาษาไทย). A player whose language has no translation, and the console, see the language set in `lang`.
  Changing the game language takes effect right away, without rejoining.

### Changed
- `per-player-language` is on for new installs. Servers updating from 2.2.0 or older get the option
  added to their `config.yml` switched off, so nothing changes until they turn it on.

## 2.2.0

### Added
- Spigot support. GainExpDisplay now works on Spigot as well as Paper and Purpur.
- Folia support (1.19.4 and newer).
- Minecraft 26.2 and 26.3 support.

### Fixed
- `gainexpdisplay.mending` now controls the Mending message. Before, the Mending message checked
  `gainexpdisplay.gainexp` instead.
- The Mending message no longer shows more durability than the item's maximum (for example 251/250).
- Items with a custom maximum durability (1.20.5 and newer) show the right maximum.
- No Mending message when another plugin cancels the repair, and no "+0" XP message.
- A fresh install no longer warns "Language en_US not found".
- The bundled MiniMessage no longer clashes with the copy Paper ships.

## 2.1.0

- Added 6 new languages.
- Reworked the configuration system for more flexibility, with a config updater that keeps your settings.
- Added permissions to control who sees the messages (`require-permission`).

## 2.0.2

- Added `/ged reload` and custom messages.

## 2.0.1

- Added 1.17 – 1.18.x support.
- New config with toggles for the XP and Mending messages.

## 2.0.0

- Customizable text.
- Support for newer Minecraft versions.
