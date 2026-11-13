<div align="center">

<img src="https://cdn.modrinth.com/data/rhuzdOze/ac7003a550e56f5ec7a61adc96ed5b5e456ab588_96.webp" alt="GainExpDisplay icon" width="96">

# GainExpDisplay

**See how much XP you gain, and how much Mending repairs, right above your hotbar.**

[![Available on Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/gainexpdisplay)
[![Available on Hangar](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/hangar_vector.svg)](https://hangar.papermc.io/PunJarrr/GainExpDisplay)
[![Available on Spigot](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/spigot_vector.svg)](https://www.spigotmc.org/resources/130682/)

![Supports Paper](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg)
![Supports Purpur](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg)
![Supports Spigot](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg)

<img src="https://cdn.modrinth.com/data/rhuzdOze/images/e1980b2fd3375d78be801b417ac189cf309616db.png" alt="The XP gain message in the action bar">

</div>

## About

GainExpDisplay is a small server plugin. Whenever a player picks up experience, the action bar (the
line of text above the hotbar) shows how much they gained. When Mending repairs an item, it shows how
much was repaired and the item's durability.

- **XP gained**, for example `Gain XP: +7`.
- **Mending repairs**, for example `Mending Repair: +10 (1071/1561)`.
- **Your own messages**: colours, gradients and formatting with [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/).
- **7 languages**: English, Spanish, German, French, Chinese, Japanese and Thai, plus your own.
- **Each player's own language**: players see the messages in the language their game is set to.
- **Permissions** to choose who sees the messages.
- **Lightweight**: it only reacts to XP and Mending, nothing runs in the background.

## Download

Get it from [Modrinth](https://modrinth.com/plugin/gainexpdisplay),
[Hangar](https://hangar.papermc.io/PunJarrr/GainExpDisplay),
[SpigotMC](https://www.spigotmc.org/resources/130682/) or
[GitHub releases](https://github.com/PunJarrr/GainExpDisplay/releases).
One jar works on every supported server and version. Put it in your server's `plugins` folder and restart.

| Server | Minecraft |
|---|---|
| Paper | 1.17 – 26.3 |
| Purpur | 1.17 – 26.3 |
| Spigot | 1.17 – 26.3 |
| Folia | 1.19.4 – 26.3 |

Only the server needs the plugin. Players don't install anything.

## Configuration

`plugins/GainExpDisplay/config.yml`:

| Option | Default | What it does |
|---|---|---|
| `lang` | `en_US` | Which file in `translations/` to use for the console, and for players when `per-player-language` is off. |
| `per-player-language` | `true` | Show each player the translation that matches their Minecraft language, when there is one. |
| `require-permission` | `false` | When `true`, players only see messages they have the permission for. |
| `gain-xp.enabled` | `true` | Show the XP message. |
| `mending-repair.enabled` | `true` | Show the Mending message. |

The messages themselves are in `plugins/GainExpDisplay/translations/<lang>.yml`. They use
[MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) and these placeholders:

| Placeholder | Meaning |
|---|---|
| `%amount%` | XP gained, or durability repaired |
| `%durability%` | The item's durability after the repair |
| `%max%` | The item's maximum durability |

To add a language, copy `en_US.yml`, translate it, and name it after the Minecraft language code, such
as `pt_BR.yml` for Brazilian Portuguese. With `per-player-language` on, players using that language get it
right away. To make it the server's language, also set `lang` to the file's name.
Run `/ged reload` after changing anything.

## Commands and permissions

| Command | Permission | Default | What it does |
|---|---|---|---|
| `/ged reload` | `gainexpdisplay.reload` | operators | Reloads the config and messages. |

| Permission | Default | What it does |
|---|---|---|
| `gainexpdisplay.gainexp` | everyone | See the XP message (only checked when `require-permission` is `true`). |
| `gainexpdisplay.mending` | everyone | See the Mending message (only checked when `require-permission` is `true`). |

## FAQ

**Does it work on Spigot?**
Yes, from version 2.2.0. Paper, Purpur and Folia are supported too.

**I'm updating from an older version. Do I lose my settings?**
No. The plugin updates old config files automatically, keeps your settings and messages, and saves a
backup of the old file in `plugins/GainExpDisplay/backup/`.

**Can players see the messages in their own language?**
Yes, from 2.3.0. With `per-player-language: true`, each player sees the translation for their Minecraft
language. Players whose language has no translation see the `lang` language. Servers updating from an older
version have this option added switched off, so turn it on in `config.yml` and run `/ged reload`.

**Can I turn it off for some players?**
Set `require-permission: true` and take away `gainexpdisplay.gainexp` or `gainexpdisplay.mending`
with a permissions plugin such as [LuckPerms](https://luckperms.net/).

## Contributing

Bug reports, ideas and translations are welcome on the
[issue tracker](https://github.com/PunJarrr/GainExpDisplay/issues). To build the plugin or change its
code, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Credits

GainExpDisplay bundles [Adventure and MiniMessage](https://github.com/KyoriPowered/adventure) by
KyoriPowered (MIT License). See [credits.txt](credits.txt), which is also included in the jar.
Badges by [devins-badges](https://github.com/intergrav/devins-badges).

## License

[MIT](LICENSE)
