# Arcane Barrier Mod

Arcane Barrier is a Forge 1.20.1 server-side gameplay system that tracks a world-wide barrier value from 0 to 100 and converts that value into one of five barrier stages.

As the barrier changes, the mod:
- synchronizes each player's GameStage,
- executes stage transition commands from JSON,
- gates entity spawns by stage-tagged entity type lists,
- applies barrier changes from gameplay events and admin commands.

## What the Mod Does

### Barrier Value and Stage Mapping
The barrier is an integer in the range 0..100.

Stage mapping:
- `> 80`: `protected`
- `> 60`: `disturbed`
- `> 40`: `breached`
- `> 20`: `corrupted`
- `<= 20`: `collapse`

Source reference:
- `BarrierStage.fromBarrier(...)` in `src/main/java/com/cinny/arcaneBarrier/barrier/BarrierStage.java`

### Stage Sync Behavior
Whenever barrier state is refreshed or changed, each online player is synchronized so they have exactly one barrier GameStage.

Source reference:
- `syncPlayerBarrierStage(...)` in `src/main/java/com/cinny/arcaneBarrier/barrier/BarrierService.java`

### Transition Effects (Fall or Mend)
On stage change, transition commands are loaded from `transitions.json` and executed.

Command routing rules:
- `server:<command>` runs once as server command source.
- `player:<command>` runs for each online player.
- no prefix is treated as a server command.

Sources:
- `src/main/resources/data/arcanebarrier/barrier/transitions.json`
- `src/main/java/com/cinny/arcaneBarrier/barrier/BarrierTransitionService.java`
- `src/main/java/com/cinny/arcaneBarrier/barrier/BarrierTransitionDefinitions.java`

### Spawn Gating
Entity spawn permission is determined by the current barrier stage and entity type tags under:
- `src/main/resources/data/arcanebarrier/tags/entity_types/`

Sources:
- `src/main/java/com/cinny/arcaneBarrier/barrier/BarrierSpawnGateService.java`

### Event-Driven Barrier Changes
The mod loads event definitions from:
- `src/main/resources/data/arcanebarrier/barrier/events.json`

Supported event types:
- `block_interact`
- `entity_kill`
- `dimension_change`
- `advancement`

Each event can be `first_time_only`, which is tracked per-player in world SavedData.

Sources:
- `src/main/java/com/cinny/arcaneBarrier/barrier/events/EventConfigLoader.java`
- `src/main/java/com/cinny/arcaneBarrier/barrier/events/EventService.java`
- `src/main/java/com/cinny/arcaneBarrier/barrier/BarrierSavedData.java`

## Commands

Root command: `/barrier`  
Permission: operator level 2+

### Core Commands
- `/barrier debug`
- `/barrier raw`
- `/barrier refresh`
- `/barrier set <value>`
- `/barrier add <value>`
- `/barrier remove <value>`

### Event Runtime Control Commands
- `/barrier event list`
- `/barrier event enable <event_id>`
- `/barrier event disable <event_id>`
- `/barrier event info <event_id>`

### Command Notes
- `set` clamps value into 0..100.
- `add` and `remove` route through the normal change pipeline and can trigger transitions.
- `raw` prints SavedData NBT for diagnostics.
- `refresh` resynchronizes player stages and resets transition baseline.

Source:
- `src/main/java/com/cinny/arcaneBarrier/commands/BarrierCommandRegistrar.java`

## Interaction and Configuration

### 1) Forge Common Config
File generated at runtime:
- `config/arcanebarrier-common.toml`

Current options:
- `debugEntitySpawns` (boolean): logs spawn gate decisions.
- `debugStageSync` (boolean): logs stage sync command execution.
- `enableTransitions` (boolean): enables transition command execution.
- `defaultBarrier` (int 0..100): default barrier value for new worlds.

Source:
- `src/main/java/com/cinny/arcaneBarrier/Config.java`

### 2) Event Definitions (`events.json`)
Location:
- `src/main/resources/data/arcanebarrier/barrier/events.json`

Schema (per event):
- `id` (string, unique)
- `type` (string)
- `enabled` (boolean, optional, default true)
- `barrier_change` (int, positive=mend, negative=fall)
- `first_time_only` (boolean, optional, default false)
- `description` (string, optional)
- `condition` (object, type-specific)

Condition examples:
- `block_interact`: `{"block":"minecraft:crying_obsidian","hand":"any"}`
- `entity_kill`: `{"entity_types":["minecraft:wither"]}`
- `dimension_change`: `{"from_dimension":"minecraft:overworld","to_dimension":"minecraft:the_nether"}`
- `advancement`: `{"advancement_id":"minecraft:story/mine_diamond"}`

### 3) Transition Definitions (`transitions.json`)
Location:
- `src/main/resources/data/arcanebarrier/barrier/transitions.json`

Top-level keys:
- `fall`
- `mend`

Each branch contains stage keys (`protected`, `disturbed`, `breached`, `corrupted`, `collapse`) with:
- `commands`: array of command strings

Execution details:
- stage changes choose `fall` or `mend` by stage rank direction,
- commands are selected using the new stage key.

### 4) Entity Spawn Tag Files
Location:
- `src/main/resources/data/arcanebarrier/tags/entity_types/`

Files:
- `protected.json`
- `disturbed.json`
- `breached.json`
- `corrupted.json`
- `collapse.json`

Each tag file controls which entities are permitted at that barrier stage and below.

## Debugging Guide

### Fast Health Checks
Use these commands first:
- `/barrier debug`
- `/barrier raw`
- `/barrier event list`
- `/barrier event info <event_id>`

### Enable Debug Logs
Set in `config/arcanebarrier-common.toml`:
- `debugEntitySpawns = true`
- `debugStageSync = true`

Then restart or reload to ensure config values are applied.

### Log Locations
Development runtime logs are under:
- `run/logs/latest.log`

Useful search terms:
- `arcanebarrier`
- `EventService`
- `Stage sync command`
- `Spawn check`

### Common Troubleshooting Cases
- `EventService not initialized` from commands:
  - server startup handlers likely have not completed yet.
  - verify startup log lines and try command after world fully loads.
- Event does not trigger:
  - confirm event exists with `/barrier event list`.
  - confirm it is enabled.
  - confirm `condition` keys and values match live IDs exactly.
- Transition effects do not run:
  - verify `enableTransitions=true`.
  - verify branch/stage key exists in `transitions.json`.
- Spawn gating appears wrong:
  - verify entity type tags are in the expected stage file.
  - enable `debugEntitySpawns` and inspect allow/deny output.

## Developer Notes

### Startup Wiring
The mod entrypoint `ArcaneBarrier` registers:
- config spec,
- lifecycle handlers (`BarrierEventHandlers`),
- command registrar (`BarrierCommandRegistrar`),
- event listeners for block, kill, dimension, and advancement triggers.

### Core Flow
1. Trigger occurs (command or gameplay event).
2. `BarrierService` mutates barrier and clamps to 0..100.
3. Player stages are synchronized.
4. Transition check compares old/new stage.
5. Transition commands execute (if enabled).

### Build Validation
Recommended compile checks:
- `./gradlew prepareRunClientCompile`
- `./gradlew prepareRunServerCompile`

## Legacy Forge Setup Docs
For base Forge MDK workspace setup and IDE run configuration, see:
- `README.txt`

## Additional Design Reference
Historical conversion notes:
- `barrier-conversion.md`
