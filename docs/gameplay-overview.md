# Gameplay Overview

[Back to README](../README.md) | [Back to Docs Index](index.md)

Arcane Barrier is a server-side world mechanic with a single global barrier value that influences player stage assignment, transition effects, and spawn gating.

## Barrier Value and Stage Mapping

The barrier is an integer in the range 0..100.

Stage mapping:
- > 80: protected
- > 60: disturbed
- > 40: breached
- > 20: corrupted
- <= 20: collapse

Source:
- [BarrierStage](../src/main/java/com/cinny/arcaneBarrier/barrier/BarrierStage.java)

## Stage Synchronization

When barrier state changes, the mod syncs all online players so each player has exactly one barrier GameStage.

Source:
- [BarrierService](../src/main/java/com/cinny/arcaneBarrier/barrier/BarrierService.java)

## Transition Effects

On stage change, transition commands are loaded and executed from resource JSON.

Command routing rules:
- server:<command> runs once as server source.
- player:<command> runs for each online player.
- no prefix defaults to server execution.

Sources:
- [transitions.json](../src/main/resources/data/arcanebarrier/barrier/transitions.json)
- [BarrierTransitionService](../src/main/java/com/cinny/arcaneBarrier/barrier/BarrierTransitionService.java)
- [BarrierTransitionDefinitions](../src/main/java/com/cinny/arcaneBarrier/barrier/BarrierTransitionDefinitions.java)

## Spawn Gating

Entity spawn allowance is controlled by barrier-stage entity type tags under resource tags.

Source:
- [BarrierSpawnGateService](../src/main/java/com/cinny/arcaneBarrier/barrier/BarrierSpawnGateService.java)

## Event-Driven Barrier Changes

The mod loads gameplay event definitions from events.json and applies barrier changes through the runtime event service.

Supported event types:
- block_interact
- entity_kill
- dimension_change
- advancement

first_time_only events are tracked per player in world SavedData.

Sources:
- [events.json](../src/main/resources/data/arcanebarrier/barrier/events.json)
- [EventConfigLoader](../src/main/java/com/cinny/arcaneBarrier/barrier/events/EventConfigLoader.java)
- [EventService](../src/main/java/com/cinny/arcaneBarrier/barrier/events/EventService.java)
- [BarrierSavedData](../src/main/java/com/cinny/arcaneBarrier/barrier/BarrierSavedData.java)
