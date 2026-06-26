# Debugging

[Back to README](../README.md) | [Back to Docs Index](index.md)

## Fast Health Checks

Run these first:
- /barrier debug
- /barrier raw
- /barrier event list
- /barrier event info <event_id>

Reference:
- [Commands](commands.md)

## Enable Debug Logs

In config/arcanebarrier-common.toml:
- debugEntitySpawns = true
- debugStageSync = true

Then restart or reload so config values are applied.

## Log Locations and Search Terms

Dev runtime logs:
- run/logs/latest.log

Useful search terms:
- arcanebarrier
- EventService
- Stage sync command
- Spawn check

## Common Troubleshooting Cases

EventService not initialized:
- Server startup may not have completed.
- Wait for full world load and retry command.
- Confirm startup initialization path in [BarrierEventHandlers](../src/main/java/com/cinny/arcaneBarrier/events/BarrierEventHandlers.java).

Event did not trigger:
- Confirm event exists with /barrier event list.
- Confirm event is enabled.
- Confirm condition values match runtime IDs exactly.
- Confirm listener type matches configured event type.

Transition commands not executing:
- Confirm enableTransitions = true.
- Confirm fall/mend branch and stage key exist in transitions.json.
- Confirm command strings are valid server or player commands.

Spawn gating seems incorrect:
- Confirm entity type is in the expected stage tag file.
- Enable debugEntitySpawns and inspect allow/deny logs.
- Confirm current barrier stage with /barrier debug.
