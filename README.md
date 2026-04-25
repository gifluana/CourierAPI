# CourierAPI

**CourierAPI** is a Fabric mod library for Minecraft 1.21.11 that lets any mod send
animated HUD notifications to players — server-to-client, or purely client-side.

![Minecraft 1.21.11](https://img.shields.io/badge/Minecraft-1.21.11-green)
![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow)

---

## Features

- Animated slide-in / slide-out notification cards in the top-right corner of the HUD
- Fully customizable colors (title, description, background, border accent)
- Configurable display duration
- Up to 4 notifications visible simultaneously; extras queue automatically
- **Server → Client**: send notifications via the Java API or JSON files — no coding required
- **Client-only**: display notifications locally from another client mod without a server
- Drop-in `/courier` command for operators to trigger notifications in-game
- Auto-creates a sample `couriernotifications.json` on first server start

---

## Installation (for mod developers)

Add the LunazStudios Maven repository and declare the dependency in your `build.gradle`:

```gradle
repositories {
    maven {
        name = "LunazStudios"
        url  = "https://maven.lunazstudios.com/releases"
    }
}

dependencies {
    // Use modImplementation if CourierAPI will be installed separately by the player:
    modImplementation "com.lunazstudios:CourierAPI:1.0+1.21.11"

    // Or bundle it inside your own jar with 'include' (no separate install needed):
    include modImplementation("com.lunazstudios:CourierAPI:1.0+1.21.11")
}
```

> **Tip:** If your mod targets both server and client, `modImplementation` is recommended
> so server admins install CourierAPI once and all compatible mods share it.
> Use `include` only if CourierAPI is an internal implementation detail of your mod.

---

## Usage

### Server-side API

Import `CourierAPI` and `Notification` from the `com.lunazstudios.courierapi.api` package.
These classes are safe to use on both the server and the client.

```java
import com.lunazstudios.courierapi.api.CourierAPI;
import com.lunazstudios.courierapi.api.Notification;

// Build a notification
Notification n = Notification.builder("Event Started!", "The PvP event has begun.")
        .durationSeconds(6)
        .titleColor(0xFFFFFF55)        // opaque yellow
        .descriptionColor(0xFFFFFFFF)  // opaque white
        .backgroundColor(0xCC000000)  // 80% opaque black
        .borderColor(0xFFFF4444)       // opaque red accent
        .build();

// Send to a single player
CourierAPI.send(player, n);

// Broadcast to every online player
CourierAPI.broadcast(server, n);
```

Colors are **ARGB integers**.  
- 8-digit hex: `0xAARRGGBB` — full control over opacity.  
- 6-digit hex: `0xRRGGBB` — alpha defaults to fully opaque (`0xFF`).

---

### Client-side API

For notifications that should appear only on the local machine — useful in pure client mods
or singleplayer utilities — use `CourierClientAPI`.

> ⚠️ **This class must only be called from client-side code.**
> Calling it on a dedicated server will crash.

```java
import com.lunazstudios.courierapi.client.CourierClientAPI;
import com.lunazstudios.courierapi.api.Notification;

Notification n = Notification.builder("Waypoint Reached", "You arrived at your destination.")
        .durationSeconds(4)
        .build();

CourierClientAPI.showLocal(n);
```

---

### Notification fields

| Field | Type | Default | Description |
|---|---|---|---|
| `title` | `String` | *(required)* | Short title displayed prominently on the card |
| `description` | `String` | `""` | Body text shown below the title |
| `durationTicks` | `int` | `100` (5 s) | How long the card stays visible (20 ticks = 1 second) |
| `titleColor` | `int` (ARGB) | `0xFFFFFF55` | Title text color |
| `descriptionColor` | `int` (ARGB) | `0xFFFFFFFF` | Description text color |
| `backgroundColor` | `int` (ARGB) | `0xCC000000` | Card background color |
| `borderColor` | `int` (ARGB) | `0xFFFFFF55` | Left accent bar and progress bar color |

---

## JSON Configuration (no coding required)

CourierAPI exposes two JSON files in the **server's root directory** that allow you to
trigger and define notifications without writing any code.

### `couriernotifications.json` — named definitions

Defines reusable notifications that can be triggered by ID via the `/courier` command
or referenced from the queue file.

Created automatically with a sample entry on first server start.

```json
{
  "restart_warning": {
    "title": "Server Restart",
    "description": "The server restarts in 5 minutes.",
    "duration": 8,
    "title_color": "#FFFF55",
    "description_color": "#FFFFFF",
    "background_color": "#CC000000",
    "border_color": "#FF5555"
  },
  "event_start": {
    "title": "Event Started!",
    "description": "Head to the arena — the PvP event has begun.",
    "duration": 6
  }
}
```

All fields except `title` are optional and fall back to their defaults.  
Colors accept both `#RRGGBB` (fully opaque) and `#AARRGGBB` formats.

---

### `couriernotifications_queue.json` — async dispatch

CourierAPI polls this file every **5 seconds**. Add entries and they will be sent
automatically; the file is reset to `[]` after processing.

This is useful for external scripts, RCON wrappers, or server management panels
that need to trigger notifications without direct access to the Java API.

```json
[
  { "id": "restart_warning" },
  { "id": "event_start", "target": "Steve" },
  {
    "title": "Inline Notice",
    "description": "A one-off notification without a saved definition.",
    "duration": 5,
    "target": "all"
  }
]
```

| Field | Description |
|---|---|
| `id` | References a named definition from `couriernotifications.json` |
| `title`, `description`, etc. | Inline notification fields (used when `id` is absent) |
| `target` | Player name to send to, or `"all"` to broadcast (default: `"all"`) |

---

## In-game Commands

All subcommands require **permission level 2** (operator).

| Command | Description |
|---|---|
| `/courier send <id>` | Broadcasts the named notification to all online players. Tab-completion lists available IDs. |
| `/courier reload` | Reloads `couriernotifications.json` from disk without restarting the server. |

---

## License

CourierAPI is released under the [MIT License](LICENSE.txt).  
You are free to use it in any mod, open-source or commercial.
