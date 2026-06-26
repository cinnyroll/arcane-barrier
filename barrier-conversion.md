# Barrier System Conversion Documentation (KubeJS -> Java Forge 1.20.1)

## Purpose

This system implements a world-level "barrier" progression mechanic that:

- Tracks a global barrier strength from 0 to 100.
- Derives a world stage from that value.
- Applies that stage to all online players via GameStages.
- Triggers audiovisual/world feedback when the stage changes.
- Gates entity spawns by stage-based entity tags.
- Allows barrier mutation from gameplay events and admin commands.

This document captures the semantic behavior required to recreate the system in Java (Forge 1.20.1), independent of JavaScript syntax.

---

## Source Components

- kubejs/server_scripts/barrier_core.js
- kubejs/server_scripts/barrier_stages.js
- kubejs/server_scripts/barrier_events.js
- kubejs/server_scripts/barrier_command.js
- kubejs/server_scripts/entity_spawns.js
- kubejs/server_scripts/entity_tags.js
- kubejs/assets/barrier/sounds.json
- kubejs/server_scripts/barrier_rewards.js (currently empty)

---

## 1. Core Domain Model

### 1.1 Barrier Value

- Type: integer
- Range: 0..100 (hard clamped)
- Scope: world/server persistent state
- Default initialization: 100 if missing

### 1.2 Barrier Stages

Ordered stage ladder (from safest to worst):

1. protected
2. disturbed
3. breached
4. corrupted
5. collapse

Threshold mapping from barrier value:

- barrier > 80 -> protected
- barrier > 60 -> disturbed
- barrier > 40 -> breached
- barrier > 20 -> corrupted
- otherwise -> collapse

### 1.3 Persistent Keys

World-level keys used:

- barrier: int
- currentBarrierStage: string (transition baseline)
- firstSpellbookCrafted: boolean (one-time trigger flag)

---

## 2. Public Behavioral Interfaces to Recreate

These are the effective contracts your Java implementation should expose.

### 2.1 syncPlayerBarrierStage(player, server) -> void

Behavior:

- Resolve current stage from barrier.
- Remove all barrier stages from the player:
  - protected, disturbed, breached, corrupted, collapse
- Add exactly one stage: the resolved current stage.

Intent:
Guarantee exclusive stage assignment per player.

### 2.2 updateBarrierStage(server) -> void

Behavior:

- Iterate all online players.
- Call syncPlayerBarrierStage for each.

Intent:
Global stage synchronization pass.

### 2.3 changeBarrier(server, amount) -> void

Behavior:

1. Read barrier (default 100 if absent).
2. Add amount.
3. Clamp into 0..100.
4. Persist new barrier value.
5. Update player stages.
6. Check transition old stage -> new stage and fire transition logic.

Intent:
Single canonical mutation pipeline for barrier changes.

### 2.4 getBarrierStage(server) -> Stage

Behavior:
Return stage from threshold mapping above.

### 2.5 checkBarrierTransition(server) -> void

Behavior:

1. oldStage = persistent currentBarrierStage, default "protected" if absent.
2. newStage = getBarrierStage(server).
3. If oldStage != newStage:
   - call onBarrierStageChange(server, oldStage, newStage)
   - persist currentBarrierStage = newStage

Intent:
Edge-triggered transition events only.

### 2.6 onBarrierStageChange(server, oldStage, newStage) -> void

Behavior:

1. Determine direction:
   - falling if rank(newStage) < rank(oldStage)
   - otherwise mending
2. Select branch:
   - fall or mend
3. Load stage event entry by newStage.
4. Execute commands if present.

Command semantics:

- Prefix player: -> run for each online player.
- Prefix server: -> run once on server.
- No prefix -> treat as server command.

Execution order:

1. Execute all server-level commands first.
2. Execute all player-level commands for each player.

---

## 3. Lifecycle and Event Wiring

### 3.1 Server Load Initialization

On server loaded:

- If persistent barrier is null/missing, initialize barrier = 100.

### 3.2 Player Login

On player login:

- Sync that player's barrier stage immediately.

This prevents new/returning players from missing stage assignment.

### 3.3 Gameplay Trigger Example

Current implemented trigger:

- On crafting irons_spellbooks:diamond_spell_book
- If firstSpellbookCrafted is false/missing:
  - set firstSpellbookCrafted = true
  - changeBarrier(-5)

This is world-once behavior, not per-player once.

### 3.4 Admin Commands (OP level >= 2)

Root: /barrier

Subcommands:

- debug
  - shows barrier numeric value
  - shows derived stage
  - lists player GameStages
- set <value:int>
  - clamps value and writes barrier directly
  - syncs stages
- add <value:int>
  - changeBarrier(+value)
- remove <value:int>
  - changeBarrier(-value)
- raw
  - dumps full persistent world data JSON
- refresh
  - sync stages
  - forcibly sets currentBarrierStage = protected

---

## 4. Transition Event Data Model

### 4.1 Stage Rank for Direction Detection

- protected: 4
- disturbed: 3
- breached: 2
- corrupted: 1
- collapse: 0

Falling means moving to a smaller rank.
Mending means moving to a larger rank.

### 4.2 Event Table Shape

Transition events are defined as:

- branch: fall or mend
- branch contains per-target-stage entries
- each entry may include:
  - commands: list of command strings

The system currently uses this to display titles/subtitles, play sounds, apply effects, spawn particles, and change weather/time.

### 4.3 Audio Resource Dependency

Custom sound events required:

- barrier:barrier_tremble
- barrier:dimension_tremble
- barrier:dimension_tremble2
- barrier:dimension_tremble3

These are declared in kubejs/assets/barrier/sounds.json.

---

## 5. Spawn Gating Integration

Barrier stage also controls which tagged entities are allowed to spawn.

### 5.1 Entity Tag Assignment

Entity types are tagged by stage via entity_type tags in entity_tags.js:

- barrier:protected
- barrier:disturbed
- barrier:breached
- barrier:corrupted
- barrier:collapse

### 5.2 Allowed Tag Sets by Current Stage

- protected -> [protected]
- disturbed -> [protected, disturbed]
- breached -> [protected, disturbed, breached]
- corrupted -> [protected, disturbed, breached, corrupted]
- collapse -> [protected, disturbed, breached, corrupted, collapse]

### 5.3 Spawn Cancellation Rule

For each spawn event:

1. Resolve barrier tags present on entity/entity type.
2. If entity has no barrier tags, allow spawn.
3. Otherwise resolve current barrier stage.
4. Compute allowed tags for that stage.
5. Cancel spawn if entity tags do not intersect allowed tags.

Enforcement hooks:

- checkSpawn (early path)
- spawned (fallback for other spawn paths)

This dual-hook strategy ensures broad compatibility with varied spawn pipelines.

---

## 6. Java Design Blueprint (Forge 1.20.1)

## 6.1 Suggested Types

### BarrierStage (enum)

Fields:

- rank: int
- threshold policy (or static resolver)

Methods:

- static fromBarrier(int barrier): BarrierStage

### BarrierSavedData (SavedData)

Fields:

- int barrier = 100
- BarrierStage currentBarrierStage = PROTECTED (or nullable with default logic)
- boolean firstSpellbookCrafted = false

Methods:

- load/save NBT
- markDirty on mutation

### BarrierService

Responsibilities:

- getBarrier(server)
- setBarrierClamped(server, value)
- changeBarrier(server, delta)
- resolveStage(server)
- syncPlayerStage(player)
- syncAllPlayers(server)
- checkAndHandleTransition(server)

### BarrierTransitionService

Responsibilities:

- compare old/new stage
- determine fall/mend
- execute configured action list
- action execution context (server or per-player)

### BarrierSpawnGateService

Responsibilities:

- stage -> allowed tag map
- entity tag extraction
- spawn decision function shouldCancel(entity, server)

### BarrierCommandRegistrar

Responsibilities:

- register /barrier command tree
- wire to BarrierService

### BarrierEventHandlers

Responsibilities:

- server load initialization
- player login sync
- craft trigger mutation

---

## 7. Pseudocode of Critical Paths

### 7.1 Canonical Mutation Path

1. oldValue = data.barrier (default 100)
2. newValue = clamp(oldValue + delta, 0, 100)
3. data.barrier = newValue
4. syncAllPlayers(server)
5. oldStage = data.currentBarrierStage or PROTECTED
6. newStage = resolveStage(newValue)
7. if oldStage != newStage:
8.   runTransition(oldStage, newStage)
9.   data.currentBarrierStage = newStage
10. mark dirty

### 7.2 Spawn Gate Decision

1. tags = barrierTags(entity)
2. if tags is empty: return allow
3. stage = resolveStage(data.barrier)
4. allowed = allowedTagsByStage(stage)
5. if intersection(tags, allowed) empty: cancel
6. else allow

---

## 8. Notable Behavioral Quirks (Preserve or Intentionally Improve)

1. /barrier set currently bypasses transition check and transition events.
2. /barrier add and /barrier remove redundantly call stage sync after changeBarrier already synced.
3. /barrier refresh sets currentBarrierStage to protected regardless of actual barrier value, potentially desynchronizing baseline.
4. In mend.corrupted commands, one particle line lacks player:/server: prefix and runs as server command context.
5. currentBarrierStage is lazily defaulted to protected rather than explicitly initialized at load.
6. entity_spawns debug logging is enabled by default (DEBUG_ENTITY_SPAWNS = true).

When porting, choose whether to preserve these exact semantics for parity or normalize behavior for robustness.

---

## 9. Minimum Test Matrix for Java Port

1. Initialization:
   - fresh world has barrier=100 and stage protected.
2. Threshold boundaries:
   - verify values 81, 80, 61, 60, 41, 40, 21, 20.
3. Clamping:
   - large negative and positive deltas clamp to 0 and 100.
4. Transition direction:
   - fall vs mend branch selection is correct.
5. Stage exclusivity:
   - player never has multiple barrier stages simultaneously.
6. Login sync:
   - joining player receives correct stage immediately.
7. Trigger idempotency:
   - first spellbook craft mutates barrier once globally.
8. Spawn gating:
   - disallowed barrier-tagged entities are canceled in both spawn hooks.
9. Command behavior:
   - debug/set/add/remove/raw/refresh match chosen parity design.
10. Transition command execution:
   - server commands once, player commands per player, in defined order.

---

## 10. Recommended Next Implementation Steps

1. Implement BarrierStage enum and BarrierSavedData.
2. Implement BarrierService with canonical mutation pipeline.
3. Implement event subscribers (load/login/crafting/spawn hooks).
4. Implement command registration for /barrier.
5. Externalize transition actions and entity stage mappings to data for easy tuning.
6. Add automated tests for threshold, transition, and spawn gate behavior.