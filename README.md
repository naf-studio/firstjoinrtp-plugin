# NAF Studio - FirstJoinRTP Plugin

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4_--_1.21.x-brightgreen.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-17_--_25-orange.svg)](https://adoptium.net/)
[![Modrinth](https://img.shields.io/badge/Modrinth-FirstJoinRTP-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/plugin/firstjoinrtp)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Lightweight Bukkit/Paper plugin that safely teleports new players to a random location upon their first join into a designated world and executes configurable commands to register their spawnpoint or home. Designed for compatibility across Spigot, Paper, Purpur, Gale, and LeafMC servers.

---

## 1. Architectural Overview & System Design

```text
firstjoinrtp-plugin/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   ├── workflows/
│   └── pull_request_template.md
├── .mvn/
│   └── wrapper/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
├── .editorconfig
├── .gitattributes
├── .gitignore
├── CONTRIBUTING.md
├── LICENSE
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

### Engineering Decisions & Standards

- Standardizes on the Spigot API abstraction to guarantee binary compatibility across Spigot, Paper, Purpur, Gale, and LeafMC without vendor lock-in.
- Targets Java 17 bytecode format (`--release 17`), enabling out-of-the-box operation on Java 17, Java 21, and Java 25 environments.
- Declares `api-version: 1.20` in `plugin.yml`, allowing clean loading on Minecraft 1.20.4, 1.20.5, 1.20.6, and 1.21.x servers without version rejection warnings.
- Tracks player first-join status using native Bukkit `PersistentDataContainer` with namespaced keys, eliminating the need for external databases or flat-file storage.
- Shields players during chunk loading and teleport dispatch by applying temporary invulnerability, hiding visibility, and cancelling mob targeting across both pending and active stages.
- Bundles the official Maven Wrapper (`mvnw`, `mvnw.cmd`), allowing compilation on any system with a JDK installed without requiring global Maven installation.

---

## 2. Compatibility & Supported Platforms

- Minecraft Versions: 1.20.4, 1.20.5, 1.20.6, 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4+
- Server Software: Spigot, Paper, Purpur, Gale, LeafMC, and downstream Bukkit forks
- Java Runtimes: OpenJDK 17, 21, and 25

---

## 3. Features

- Triggers customizable random teleport commands exclusively when a player enters the target world for the first time.
- Supports downstream authentication setups (e.g., Limbo or Lobby servers) by intercepting world-change transitions into the survival world.
- Executes configurable commands at the landing location to assign native spawnpoints or external homes (e.g., vanilla `/spawnpoint` or CMI `/cmi sethome`).
- Protects players from fall damage, suffocation, and hostile mob targeting during chunk loading.
- Reverts visibility and restores interaction states gracefully upon completion or safety timeouts.

---

## 4. Configuration

A default `config.yml` is generated automatically upon first startup:

```yaml
# Target world to check for first join RTP
target-world: "world"

# Whether to enable the custom Random Teleport command execution
rtp-command-enabled: true

# Command to execute for Random Teleport
# Used when the player enters the target world for the first time.
# Placeholders: %player%, %world%
# Default (Vanilla): "spreadplayers 0 0 150 10000 false %player%"
# CMI Recommendation: "cmi rt %player% %world%"
rtp-command: "spreadplayers 0 0 150 10000 false %player%"

# Whether to enable the custom spawnpoint command execution
spawnpoint-command-enabled: true

# Command to set the player's spawnpoint or home after successful RTP.
# Placeholders: %player%, %world%, %x%, %y%, %z%
# Default (Vanilla): "spawnpoint %player% %x% %y% %z%"
# CMI Recommendation: "cmi sethome firstspawn %player% -p -l:%world%;%x%;%y%;%z% -overwrite"
spawnpoint-command: "spawnpoint %player% %x% %y% %z%"

# Delay in ticks before restoring player visibility after teleportation
# 20 ticks = 1 second
delay-after-teleport-ticks: 20
```

---

## 5. Installation

1. Download the latest `FirstJoinRTP.jar` from [Modrinth](https://modrinth.com/plugin/firstjoinrtp) or [GitHub Releases](https://github.com/naf-studio/firstjoinrtp-plugin/releases).
2. Place the `.jar` file into your server's `plugins/` directory.
3. Restart the server to generate `plugins/FirstJoinRTP/config.yml`.
4. Configure teleport commands according to your server setup.
5. Reload or restart the server to apply configuration changes.

---

## 6. Building from Source

### Prerequisites

- Java Development Kit (JDK) 17, 21, or 25 installed and available in PATH.

### Compilation

Build the plugin JAR using the bundled Maven Wrapper:

```bash
./mvnw clean package
```

On Windows:

```cmd
.\mvnw.cmd clean package
```

The compiled artifact will be generated at `target/FirstJoinRTP-1.6.jar`.

---

## 7. Contributing

Contributions must follow the standards outlined in [CONTRIBUTING.md](CONTRIBUTING.md).

---

## 8. License

This project is licensed under the [MIT License](LICENSE). Copyright &copy; 2026 [naipret](https://github.com/naipret).
