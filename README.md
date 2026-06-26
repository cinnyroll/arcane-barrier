# Arcane Barrier Mod

Arcane Barrier is a Forge 1.20.1 server-side gameplay system that tracks a world-wide barrier value from 0 to 100 and converts that value into one of five barrier stages.

As the barrier changes, the mod:
- synchronizes each player's GameStage,
- executes stage transition commands from JSON,
- gates entity spawns by stage-tagged entity type lists,
- applies barrier changes from gameplay events and admin commands.

## Documentation

Primary docs hub:
- [Documentation Index](docs/index.md)

### Quick Start by Audience
- Server/Admin:
[Commands](docs/commands.md), [Configuration](docs/configuration.md), [Debugging](docs/debugging.md)
- Modpack/Config Author:
[Gameplay Overview](docs/gameplay-overview.md), [Configuration](docs/configuration.md)
- Contributor/Developer:
[Architecture](docs/architecture.md), [Development Workflow](docs/development.md)

### Common Next Reads
- [Gameplay Overview](docs/gameplay-overview.md)
- [Commands](docs/commands.md)
- [Configuration](docs/configuration.md)

## Compatibility and Setup Notes

For base Forge MDK workspace setup and IDE run configuration, see:
- [README.txt](README.txt)

Historical conversion notes:
- [barrier-conversion.md](barrier-conversion.md)
