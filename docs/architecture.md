# Architecture

[Back to README](../README.md) | [Back to Docs Index](index.md)

## Startup Wiring

Mod entrypoint registers:
- config spec,
- lifecycle handlers,
- command registrar,
- dedicated event listeners.

Source:
- [ArcaneBarrier](../src/main/java/com/cinny/arcaneBarrier/ArcaneBarrier.java)

## Main Runtime Components

- BarrierService:
Owns barrier mutation, stage synchronization, and transition checks.

- BarrierSavedData:
Persists barrier value, current stage baseline, and first_time_only per-player tracking.

- EventService:
Holds loaded event definitions and runtime enable/disable state.

- BarrierTransitionService:
Executes transition command sets from transitions.json.

- BarrierSpawnGateService:
Evaluates entity type tags against current barrier stage.

## Event Trigger Pipeline

1. Forge event listener receives gameplay trigger.
2. Matching logic checks EventDefinition.condition.
3. EventService.triggerEvent applies first_time_only gating.
4. BarrierService.changeBarrier mutates state and synchronizes stages.
5. Transition check executes fall/mend commands when stage changed.

Listener sources:
- [BlockInteractListener](../src/main/java/com/cinny/arcaneBarrier/events/listeners/BlockInteractListener.java)
- [EntityKillListener](../src/main/java/com/cinny/arcaneBarrier/events/listeners/EntityKillListener.java)
- [DimensionChangeListener](../src/main/java/com/cinny/arcaneBarrier/events/listeners/DimensionChangeListener.java)
- [AdvancementListener](../src/main/java/com/cinny/arcaneBarrier/events/listeners/AdvancementListener.java)

## Extension Points

Add a new event type:
- Extend events.json schema for the new type condition.
- Add matching logic in a listener or new listener class.
- Keep barrier mutation routed through EventService and BarrierService.

Add new transition effects:
- Edit transitions.json for fall/mend and stage target.
- Use server: or player: prefixes intentionally.

Adjust spawn control:
- Update entity type tag files under data/arcanebarrier/tags/entity_types.
