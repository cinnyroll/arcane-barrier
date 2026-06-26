# Commands

[Back to README](../README.md) | [Back to Docs Index](index.md)

Root command: /barrier  
Permission: operator level 2+

Source:
- [BarrierCommandRegistrar](../src/main/java/com/cinny/arcaneBarrier/commands/BarrierCommandRegistrar.java)

## Core Commands

- /barrier debug
Shows current barrier value, current stage, and online player count.

- /barrier raw
Prints raw SavedData NBT for diagnostics.

- /barrier refresh
Resynchronizes player stage assignments and refreshes transition baseline.

- /barrier set <value>
Sets barrier to an absolute value (clamped to 0..100).

- /barrier add <value>
Adds value to barrier through the standard mutation pipeline.

- /barrier remove <value>
Subtracts value from barrier through the standard mutation pipeline.

## Event Runtime Control Commands

- /barrier event list
Lists all loaded events with enabled/disabled state and barrier change value.

- /barrier event enable <event_id>
Enables a specific event at runtime.

- /barrier event disable <event_id>
Disables a specific event at runtime.

- /barrier event info <event_id>
Shows detailed event metadata and current runtime state.

## Operator Examples

- Set barrier directly:
/barrier set 65

- Apply a fall event manually:
/barrier remove 10

- Re-sync everyone after config changes:
/barrier refresh

- Confirm one event is loaded and active:
/barrier event info nether_enter
