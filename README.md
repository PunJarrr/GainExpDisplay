# <p align="center">GainExpDisplay</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg" alt="paper">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg" alt="spigot">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg" alt="purpur">
  <a href="https://modrinth.com/plugin/gainexpdisplay/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/modrinth_vector.svg" alt="modrinth">
  </a>
  <a href="https://www.spigotmc.org/resources/gainexpdisplay-1-17-1-21-11.130682/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/spigot_vector.svg" alt="spigot">
  </a>
  <a href="https://hangar.papermc.io/PunJarrr/GainExpDisplay/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/hangar_vector.svg" alt="hangar">
  </a>
  <a href="https://github.com/PunJarrr/GainExpDisplay/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/github_vector.svg" alt="github">
  </a>
</p>

##

A lightweight Minecraft plugin that shows useful feedback in the action bar whenever you gain experience or when items are repaired by **Mending**.

## ✨ Features

- Displays XP gained directly in the action bar
- Shows Mending repair amount and durability progress
- Fully customizable messages using MiniMessage
- 7 languages supported
- Lightweight and performance-friendly

<details>
<summary>📸 Showcase</summary>
  
- **XP Gained**
  
  ![Gaining XP](https://cdn.modrinth.com/data/cached_images/e1980b2fd3375d78be801b417ac189cf309616db.png)
  
- **Mending Repair**
  
  ![Mending Repair](https://cdn.modrinth.com/data/cached_images/4d0413ecf810d9a9bedac8b0fc6b99a30e8c19c9.png)
</details>

## ⚙️ Configuration

- Permission requirement and messages toggle are fully configurable in ```config.yml```.
- All messages are fully customizable in **translation** files using [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/#standard-tags) format.

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

## 📚 Command & Permissions

- Command

| Command           | Permission                  | Description         |
| ----------------- | --------------------------- | ------------------- |
| ```/ged reload``` | ```gainexpdisplay.reload``` | Reload config file. |

- Permissions

| Permission                   | Description                                     | Default    |
| ---------------------------- | ----------------------------------------------- | ---------- |
| ```gainexpdisplay.gainxp```  | Display text when gaining experience.           | ```true``` |
| ```gainexpdisplay.mending``` | Display text when repairing items with Mending. | ```true``` |
