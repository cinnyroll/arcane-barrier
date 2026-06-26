# Architecture

[Home](Home) | [Gameplay Overview](Gameplay-Overview) | [Development Workflow](Development-Workflow)

## Startup Wiring

Mod entrypoint registers:
- config spec
- lifecycle handlers
- command registrar
- dedicated event listeners

Source:
- [ArcaneBarrier](https://github.com/cinnyroll/arcane-barrier/blob/main/src/main/java/com/cinny/arcaneBarrier/ArcaneBarrier.java)

## Main Runtime Components

- `BarrierService`: owns barrier mutation, stage synchronization, and transition checks
- `BarrierSavedData`: persists barrier value, current stage baseline, and `first_time_only` per-player tracking
- `EventService`: holds loaded event definitions and runtime enable/disable state
- `BarrierTransitionService`: executes transition command sets from `transitions.json`
- `BarrierSpawnGateService`: evaluates entity type tags against current barrier stage

## Event Trigger Pipeline

1. Forge event listener receives gameplay trigger.
2. Matching logic checks `EventDefinition.condition`.
3. `EventService.triggerEvent` applies `first_time_only` gating.
4. `BarrierService.changeBarrier` mutates state and synchronizes stages.
5. Transition check executes fall/mend commands when the stage changed.

Listener sources:
- [BlockInteractListener](https://github.com/cinnyroll/arcane-barrier/blob/main/src/main/java/com/cinny/arcaneBarrier/events/listeners/BlockInteractListener.java)
- [EntityKillListener](https://github.com/cinnyroll/arcane-barrier/blob/main/src/main/java/com/cinny/arcaneBarrier/events/listeners/EntityKillListener.java)
- [DimensionChangeListener](https://github.com/cinnyroll/arcane-barrier/blob/main/src/main/java/com/cinny/arcaneBarrier/events/listeners/DimensionChangeListener.java)
- [AdvancementListener](https://github.com/cinnyroll/arcane-barrier/blob/main/src/main/java/com/cinny/arcaneBarrier/events/listeners/AdvancementListener.java)

## Extension Points

Add a new event type:
- extend the `events.json` schema for the new type condition
- add matching logic in a listener or a new listener class
- keep barrier mutation routed through `EventService` and `BarrierService`

Add new transition effects:
- edit `transitions.json` for fall/mend and stage target
- use `server:` or `player:` prefixes intentionally

Adjust spawn control:
- update entity type tag files under `data/arcanebarrier/tags/entity_types`
