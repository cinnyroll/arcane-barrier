# Development Workflow

[Home](Home) | [Architecture](Architecture) | [Debugging](Debugging)

## Build Validation Commands

Run compile checks after behavior or docs updates:
- `./gradlew prepareRunClientCompile`
- `./gradlew prepareRunServerCompile`

## Recommended Validation Pass

1. Compile checks pass.
2. In-game command checks:
   - `/barrier debug`
   - `/barrier raw`
   - `/barrier event list`
   - `/barrier event info <event_id>`
3. Trigger at least one configured event and confirm barrier mutation.
4. Confirm stage synchronization updates GameStage assignment.
5. Confirm transition commands fire on stage crossing.

## Contributor Safety Checklist

- Keep barrier changes routed through `BarrierService`
- Keep event state and `first_time_only` tracking in `SavedData` and `EventService`
- Keep command syntax and docs aligned with `BarrierCommandRegistrar`
- Update docs when adding new event types, commands, or config keys

## Related References

- [Repository README](https://github.com/cinnyroll/arcane-barrier/blob/main/README.md)
- [Documentation Index](https://github.com/cinnyroll/arcane-barrier/blob/main/docs/index.md)
- [Historical conversion notes](https://github.com/cinnyroll/arcane-barrier/blob/main/barrier-conversion.md)
