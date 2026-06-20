# OGDuels ⚔️

An advanced, high-performance practice and duels core for modern Minecraft PvP networks. Built with an asynchronous SQLite engine, it features dynamic queuing, custom kits, a comprehensive matchmaking GUI, and robust performance optimization to handle high-concurrency player environments smoothly.

---

## ⚡ Features

* **Asynchronous Database Architecture:** Flat-file SQLite data and player profile handling run entirely off the main server thread to guarantee stable TPS during heavy combat.
* **Dynamic Killstreaks & Bounties:** Tracks real-time player killstreaks, scales monetary or point bounties, and hooks into milestone actions.
* **Kit Refresher Engine:** Automatically handles instant kit updates, item replenishments, and health restoration upon killing a player.
* **PlaceholderAPI Integration:** Exposes comprehensive player statistics to external scoreboards, tablists, and chat formatting systems.

---

## 🛠️ Project Structure

```text
├── src/main/
│   ├── java/itzjatinog/ogduels/
│   │   ├── commands/       # Player and administrative commands
│   │   ├── config/         # File managers and YAML configurations
│   │   ├── engine/         # Core PvP logic and combat logging mechanics
│   │   ├── gui/            # Interactive inventory menus and leaderboards
│   │   └── managers/       # Thread-safe managers (KillStreaks, Parties, Queues)
│   └── resources/          # Configuration files (config.yml, guis.yml, streak.yml)
├── build.gradle.kts        # Build dependency architecture
└── gradlew                 # Gradle wrapper script

---

## ⚠️ Terms of Use & Warning

* **Intellectual Property:** You are strictly prohibited from re-uploading, distributing, or selling this plugin (or its modified source code) under your own name or as your own commercial product. 
* **Usage:** You are free to modify the source code for personal use on your own server or network. However, public redistribution of altered versions claiming original authorship is a direct violation of the project's terms.

---