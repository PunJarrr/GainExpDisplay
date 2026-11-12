# <p align="center">GainExpDisplay</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg" alt="paper">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg" alt="purpur">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg" alt="spigot">
  <br>
  <a href="https://modrinth.com/plugin/gainexpdisplay">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/modrinth_vector.svg" alt="modrinth">
  </a>
  <a href="https://hangar.papermc.io/PunJarrr/GainExpDisplay">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/hangar_vector.svg" alt="hangar">
  </a>
  <a href="https://www.spigotmc.org/resources/130682/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/spigot_vector.svg" alt="spigot">
  </a>
  <a href="https://github.com/PunJarrr/GainExpDisplay">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/github_vector.svg" alt="github">
  </a>
</p>

##

Ever wondered how much XP that orb was worth, or how much Mending just fixed? **GainExpDisplay** shows it in the action bar, right above your hotbar.

## ✨ Features

- **XP gained** in the action bar, like `Gain XP: +7`
- **Mending repairs** with the item's durability, like `Mending Repair: +10 (1071/1561)`
- **Your own messages** with colours, gradients and formatting ([MiniMessage](https://docs.papermc.io/adventure/minimessage/format/))
- **7 languages:** English, Spanish, German, French, Chinese, Japanese and Thai, or add your own
- **Permissions** to choose who sees each message
- **Paper, Purpur, Spigot and Folia**, Minecraft **1.17 – 26.3**, one jar for all of them
- **Lightweight:** only reacts to XP and Mending, nothing runs in the background

<details>
<summary>📸 Showcase</summary>

- **XP Gained**

  ![Gaining XP](https://cdn.modrinth.com/data/cached_images/e1980b2fd3375d78be801b417ac189cf309616db.png)

- **Mending Repair**

  ![Mending Repair](https://cdn.modrinth.com/data/cached_images/4d0413ecf810d9a9bedac8b0fc6b99a30e8c19c9.png)
</details>

## 📦 Supported Versions

| Server | Minecraft |
|---|---|
| Paper | 1.17 – 26.3 |
| Purpur | 1.17 – 26.3 |
| Spigot | 1.17 – 26.3 |
| Folia | 1.19.4 – 26.3 |

## 📄 Installation

1. Download the jar (the same file works on every server and version above)
2. Put it in your server's `plugins` folder
3. Restart the server

Only the server needs it. Players don't install anything.

## ⚙️ Configuration

- Turn each message on or off, and require permissions, in `config.yml`.
- Change the messages in the **translation files** (`translations/<lang>.yml`) using [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/). Placeholders: `%amount%`, `%durability%`, `%max%`.
- Run `/ged reload` after changing anything.

<details>
<summary>📝 config.yml</summary>

```yaml
# Configuration version, DO NOT MODIFY THIS VALUE!
config-version: 3.0.0

######################################################
#                     Language                       #
######################################################

# Language setting (available: en_US, es_ES, de_DE, fr_FR, zh_CN, ja_JP, th_TH)
# If the specified language file is missing, it will fall back to en_US.
lang: en_US

# Adding custom language
# 1. Create a new file in the translations directory
# 2. Use en_US.yml as a template
# 3. Modify the file as needed
# 4. Set language to your custom language file name

######################################################
#                    Permissions                     #
######################################################

# When set to true, players require permissions to see any message.
# This setting applies globally to all message types.
# See below for a list of permissions.
require-permission: false

# Permissions for displaying messages:
#
#  gainexpdisplay.gainexp - Display text when gaining experience
#  gainexpdisplay.mending - Display text when repairing items with Mending

######################################################
#                      Settings                      #
######################################################

# When set to true, players will see text when gaining exp.
gain-xp:
  enabled: true

# When set to true, players will see text when repairing items with Mending.
mending-repair:
  enabled: true
```
</details>

## 📚 Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/ged reload` | `gainexpdisplay.reload` | Reload the config and messages (operators by default). |

| Permission | Description | Default |
|---|---|---|
| `gainexpdisplay.gainexp` | See the XP message (when `require-permission` is `true`). | `true` |
| `gainexpdisplay.mending` | See the Mending message (when `require-permission` is `true`). | `true` |

<details>
<summary>❓ FAQ</summary>

**Does it work on Spigot?**
Yes, from 2.2.0. Paper, Purpur and Folia work too.

**I'm updating from an older version. Do I lose my settings?**
No. Old config files are updated automatically, your settings and messages are kept, and a backup is saved in `plugins/GainExpDisplay/backup/`.

**Can I hide the messages for some players?**
Set `require-permission: true` and take away `gainexpdisplay.gainexp` or `gainexpdisplay.mending` with a permissions plugin like LuckPerms.
</details>

## 💬 Feedback

Found a bug or have an idea? Open an issue on [GitHub](https://github.com/PunJarrr/GainExpDisplay/issues).

## ❤️ Credits

Bundles [Adventure and MiniMessage](https://github.com/KyoriPowered/adventure) by KyoriPowered (MIT License).
