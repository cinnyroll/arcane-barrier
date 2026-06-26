# Configuration

[Home](Home) | [Gameplay Overview](Gameplay-Overview) | [Debugging](Debugging)

## Forge Common Config

Runtime file:
- `config/arcanebarrier-common.toml`

Keys:
- `debugEntitySpawns` (boolean): enables spawn gate decision logs
- `debugStageSync` (boolean): logs stage sync command execution
- `enableTransitions` (boolean): toggles transition command execution
- `defaultBarrier` (int, 0..100): default barrier for new worlds

Source:
- [Config](https://github.com/cinnyroll/arcane-barrier/blob/main/src/main/java/com/cinny/arcaneBarrier/Config.java)

## Event Definitions (events.json)

Location:
- [events.json](https://github.com/cinnyroll/arcane-barrier/blob/main/src/main/resources/data/arcanebarrier/barrier/events.json)

Schema for each event object:
- `id` (string, unique)
- `type` (string)
- `enabled` (boolean, optional, default true)
- `barrier_change` (int, positive = mend, negative = fall)
- `first_time_only` (boolean, optional, default false)
- `description` (string, optional)
- `condition` (object, type-specific)

Condition examples:
- `block_interact`: `{"block":"minecraft:crying_obsidian","hand":"any"}`
- `entity_kill`: `{"entity_types":["minecraft:wither"]}`
- `dimension_change`: `{"from_dimension":"minecraft:overworld","to_dimension":"minecraft:the_nether"}`
- `advancement`: `{"advancement_id":"minecraft:story/mine_diamond"}`

## Transition Definitions (transitions.json)

Location:
- [transitions.json](https://github.com/cinnyroll/arcane-barrier/blob/main/src/main/resources/data/arcanebarrier/barrier/transitions.json)

Top-level keys:
- `fall`
- `mend`

Each branch contains stage keys `protected`, `disturbed`, `breached`, `corrupted`, `collapse` with:
- `commands`: array of command strings

Execution behavior:
- Branch chosen by transition direction (stage rank fall or mend)
- Commands selected from the new stage key
- `server:` and `player:` prefixes determine execution context

## Entity Spawn Tag Files

Location:
- [entity_types tags](https://github.com/cinnyroll/arcane-barrier/tree/main/src/main/resources/data/arcanebarrier/tags/entity_types)

Files:
- `protected.json`
- `disturbed.json`
- `breached.json`
- `corrupted.json`
- `collapse.json`

These tags define which entity types are allowed to spawn as the barrier degrades.
