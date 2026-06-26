# Arcane Barrier Wiki

Arcane Barrier is a Forge 1.20.1 server-side gameplay system that tracks a world-wide barrier value from 0 to 100 and converts that value into one of five barrier stages.

As the barrier changes, the mod:
- synchronizes each player's GameStage,
- executes stage transition commands from JSON,
- gates entity spawns by stage-tagged entity type lists,
- applies barrier changes from gameplay events and admin commands.

## Start Here

- [Gameplay Overview](Gameplay-Overview)
- [Commands](Commands)
- [Configuration](Configuration)
- [Debugging](Debugging)
- [Architecture](Architecture)
- [Development Workflow](Development-Workflow)

## Quick Start by Audience

- Server/Admin: [Commands](Commands), [Configuration](Configuration), [Debugging](Debugging)
- Modpack/Config Author: [Gameplay Overview](Gameplay-Overview), [Configuration](Configuration)
- Contributor/Developer: [Architecture](Architecture), [Development Workflow](Development-Workflow)

## Common Tasks

- Inspect current state quickly: [Commands](Commands#core-commands)
- Enable or disable one event at runtime: [Commands](Commands#event-runtime-control-commands)
- Add a new barrier event definition: [Configuration](Configuration#event-definitions-eventsjson)
- Diagnose why an event did not fire: [Debugging](Debugging#common-troubleshooting-cases)
- Diagnose transition effect execution: [Configuration](Configuration#transition-definitions-transitionsjson)

## Repository References

- [README](https://github.com/cinnyroll/arcane-barrier/blob/main/README.md)
- [Documentation Index](https://github.com/cinnyroll/arcane-barrier/blob/main/docs/index.md)
- [Historical conversion notes](https://github.com/cinnyroll/arcane-barrier/blob/main/barrier-conversion.md)
