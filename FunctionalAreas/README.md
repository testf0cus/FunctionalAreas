# Functional Areas - Minecraft Fabric Mod

## 🎯 What does this mod do?

Allows server-side creation of rectangular regions in the world (functional areas) that, when entered by players:
- ✅ Execute server commands (e.g., `give`, `say`, `tp`, etc.).
- ✅ Display an optional **hologram (title)** to the player.
- ✅ Fully server-side – no client mods required.

---

## 🔧 Available Commands

| Command                                      | Description                                                              |
|---------------------------------------------|--------------------------------------------------------------------------|
| `/fahelp`                                    | Shows all mod commands.                                                  |
| `/faset`                                     | Enter edit mode and receive the Area Selector.                           |
| `/faset <name>`                              | Create an area with only a name.                                         |
| `/faset <name> <command>`                    | Create an area with a command but no hologram.                           |
| `/faset <name> #<hologram>`                  | Create an area with a hologram but no command.                           |
| `/faset <name> <command> #<hologram>`        | Create an area with both command and hologram.                           |
| `/facancel`                                  | Exit edit mode and restore inventory.                                    |
| `/fadelete <name>`                           | Delete a region by name.                                                 |
| `/faview`                                    | List all created regions with their info.                                |

---

## 🎮 Edit Mode

- Activated via `/faset`.
- Grants a **Breeze Rod** (Area Selector) in slot 0.
  - 🔒 Cannot be moved, dropped, duplicated, or stored.
  - 🔁 Restored automatically if lost or displaced.
- Left-click with selector → Point A.
- Right-click → Point B + particle cube shown.
- Then use `/faset <name> ...` to finalize region.
- Exit with `/facancel` or by disconnecting.

---

## 📦 Files Overview

| File                   | Responsibility                                                                    |
|------------------------|-----------------------------------------------------------------------------------|
| `FAEntryPoint.java`    | Main entry point; registers events and loads/saves areas.                         |
| `FACommandHandler.java`| Registers and handles all mod commands.                                           |
| `FAManager.java`       | Core logic: region management, creation, tracking, and persistence.               |
| `FARegion.java`        | Region data class: name, points, command, hologram.                               |
| `FAEventHandler.java`  | Handles interactions like clicks, drops, edit mode rules, etc.                    |
| `FAEffects.java`       | Displays visual effects like particle cubes for areas.                            |

---

## 🗂 Region Storage

- Saved to:
  ```
  /world/functionalareas/regions.json
  ```
- Automatically loaded on server start, saved on stop.

---

## ⚙️ Technical Info

- Version: **Minecraft 1.21.1**
- Loader: **Fabric**
- Mappings: **Yarn v2**
- Server-only: ✅ Yes

---

## 🧠 Notes

- Commands execute **once per entry** into a region (not per block).
- Holograms support Minecraft color codes using `&` (e.g., `&aWelcome!`).
- `#` is used as a delimiter when creating regions to define the hologram text.
