# Development Workflow

[Back to README](../README.md) | [Back to Docs Index](index.md)

## Build Validation Commands

Run compile checks after behavior or docs updates:
- ./gradlew prepareRunClientCompile
- ./gradlew prepareRunServerCompile

## Recommended Validation Pass

1. Compile checks pass.
2. In-game command checks:
- /barrier debug
- /barrier raw
- /barrier event list
- /barrier event info <event_id>
3. Trigger at least one configured event and confirm barrier mutation.
4. Confirm stage synchronization updates GameStage assignment.
5. Confirm transition commands fire on stage crossing.

## Contributor Safety Checklist

- Keep barrier changes routed through BarrierService.
- Keep event state and first_time_only tracking in SavedData/EventService.
- Keep command syntax and docs aligned with BarrierCommandRegistrar.
- Update docs when adding new event types, commands, or config keys.

## Related References

- Entry page: [README](../README.md)
- Docs hub: [Docs Index](index.md)
- Legacy Forge setup: [README.txt](../README.txt)
- Historical conversion: [barrier-conversion.md](../barrier-conversion.md)
