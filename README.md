# AngelBounty

A Minecraft 1.21 plugin that adds a **wanted and bounty system** to your server. Players gain wanted stars for killing too many players, their wanted status is shown in TAB and above their head, and anyone who takes down a wanted player can claim the bounty reward.

***

## Features

- Kill-based wanted progression from 1 to 6 stars
- TAB display for each player's current wanted level
- Nametag star display above player heads using scoreboard teams
- Sidebar scoreboard showing your wanted level and current bounty
- Optional bounty payout through Vault economy integration
- Persistent player wanted data stored in `wanted-data.yml`
- Wanted decay over time, fully configurable in `config.yml`
- Admin commands to set, add, clear, reload, and view top wanted players

***

## Commands

| Command | Description |
|---|---|
| `/wanted` | Check your own wanted status |
| `/wanted <player>` | Check another player's wanted status |
| `/wanted top` | View the most wanted players |
| `/wanted set <player> <stars>` | Set a player's wanted stars (admin) |
| `/wanted add <player> <stars>` | Add wanted stars to a player (admin) |
| `/wanted clear <player>` | Clear a player's wanted level (admin) |
| `/wanted reload` | Reload the plugin config (admin) |

***

## Permissions

| Permission | Default | Description |
|---|---|---|
| `angelbounty.admin` | OP | Access to admin wanted commands |
| `angelbounty.bypass` | False | Prevent gaining wanted level from killing players |
| `angelbounty.view.others` | True | View other players' wanted status |

***

## How the Wanted System Works

Every player kill can increase the killer's wanted level:

- Every configured number of player kills adds more wanted stars
- Wanted level is capped at **6 stars**
- Wanted players have their stars shown in **TAB** and above their head
- If a wanted player dies, their wanted stars can be cleared and their bounty is claimed
- Only players who are actually wanted have their stars cleared on death
- Wanted level can decay over time if enabled in the config

This means repeated player killers become visible targets, while bounty hunters are rewarded for taking them down.[1][2]

***

## Configuration

`config.yml` is generated on first run. Key settings:

```yaml
wanted:
  kills-per-star: 2
  max-stars: 6
  decay:
    enabled: true
    interval-seconds: 300
    stars-per-interval: 1
  lose-all-on-death: true
  reset-kills-on-death: true
  reward-base: 100.0
  reward-per-star: 75.0
  save-interval-seconds: 60

display:
  scoreboard-sidebar: true
  tab-prefix: true
  nametag-prefix: true
  below-name: false
```

To limit the system to specific worlds, add entries under `wanted.worlds-enabled`:

```yaml
wanted:
  worlds-enabled:
    - world
    - pvp_world
  use-all-worlds-when-empty: false
```

To enable real money bounty rewards, install Vault and an economy plugin, then enable economy support:

```yaml
economy:
  enabled: true
  vault-required: false
  fallback-message-reward: true
```

Paper plugins can declare optional integrations through `softdepend`, which is how Vault support is typically handled in `plugin.yml`.[3][4][5]

***

## Installation

1. Drop the compiled `.jar` into your server's `plugins/` folder
2. Start or restart your server
3. Edit `plugins/AngelBounty/config.yml` to customize wanted stars, bounty values, and display settings
4. If you want paid rewards, install Vault plus a compatible economy plugin
5. Reload with `/reload confirm` or restart the server

***

## Building

Requires Java 21 and Gradle.

```bash
./gradlew jar
```

Output jar will be in `build/libs/`.

***

## Dependencies

- [Paper API 1.21.11](https://docs.papermc.io) — provided at runtime by the server[3]
- [Vault API](https://dev.bukkit.org/projects/vault) — optional, used for bounty payouts when economy support is enabled[5]
- Paper scoreboard APIs are used for sidebar, nametag, and player list displays.[1][2]
