/*
 This script contains all events related to barrier stages and the events that notify the player
 changes to the barrier stage.
*/


// definition of order of barrier stage changes
const barrierStageOrder = {
    protected: 4,
    disturbed: 3,
    breached: 2,
    corrupted: 1,
    collapse: 0
}

// single event definition table for barrier stage changes
// each stage change can have a set of commands that will be run when the stage is entered
// commands can be run on the server or per player
// commands are nested under the "fall" or "mend" keys to determine if the barrier is falling or mending
const barrierStageEvents = {
    fall: {
        disturbed: {
            commands: [
                'player:title @s title {"text":"The First Stirring","color":"yellow"}',
                'player:title @s subtitle {"text":"Ancient seals begin to strain."}',
                'player:execute as @s at @s run playsound barrier:barrier_tremble master @s ~ ~ ~',
                'server:weather rain'
            ]
        },

        breached: {
            commands: [
                'player:title @s title {"text":"The First Crack","color":"red"}',
                'player:title @s subtitle {"text":"Forgotten things find a path home."}',
                'player:execute as @s at @s run playsound barrier:dimension_tremble master @s ~ ~ ~',
                'player:effect give @s minecraft:nausea 5 1 true',
                'player:effect give @s minecraft:darkness 3 0 true',
                'player:particle minecraft:soul ~ ~1 ~ 0.5 1 0.5 0.05 100 force @s'
            ]
        },

        corrupted: {
            commands: [
                'player:title @s title {"text":"The Sundering","color":"dark_purple"}',
                'player:title @s subtitle {"text":"Ancient horros scent freedom."}',
                'player:execute as @s at @s run playsound barrier:dimension_tremble2 master @s ~ ~ ~',
                'player:effect give @s minecraft:nausea 8 1 true',
                'player:effect give @s minecraft:darkness 6 0 true',
                'player:particle minecraft:soul ~ ~1 ~ 0.5 1 0.5 0.05 100 force @s'
            ]
        },

        collapse: {
            commands: [
                'player:title @s title {"text":"The Fall","color":"dark_red"}',
                'player:title @s subtitle {"text":"The ancient ward has failed."}',
                'player:execute as @s at @s run playsound barrier:dimension_tremble3 master @s ~ ~ ~',
                'player:effect give @s minecraft:nausea 9 1 true',
                'player:effect give @s minecraft:darkness 6 0 true',
                'player:effect give @s minecraft:weakness 30 0 true',
                'player:particle minecraft:ash ~ ~1 ~ 1 1 1 0.01 200 force @s',
                'player:particle minecraft:soul_fire_flame ~ ~1 ~ 0.5 1 0.5 0.02 75 force @s',
                'server:time set night'
            ]
        }
    },

    mend: {
        protected: {
            commands: [
                'player:title @s title {"text":"The Barrier Holds","color":"green"}',
                'player:title @s subtitle {"text":"The ancient seal stands firm once more."}',
                'player:execute as @s at @s run playsound minecraft:block.beacon.activate master @s ~ ~ ~',
                'player:particle minecraft:end_rod ~ ~1 ~ 0.5 1 0.5 0.02 150 force @s',
                'player:particle minecraft:glow ~ ~1 ~ 0.5 1 0.5 0.02 100 force @s',
                'server: weather clear'
            ]
        },
        disturbed: {
            commands: [
                'player:title @s title {"text":"The Barrier Recovers","color":"yellow"}',
                'player:title @s subtitle {"text":"Ancient wards strengthen once more."}',
                'player:execute as @s at @s run playsound minecraft:block.amethyst_block.step master @s ~ ~ ~',
                'player:particle minecraft:glow ~ ~1 ~ 0.5 1 0.5 0.02 75 force @s'
            ]
        },

        breached: {
            commands: [
                'player:title @s title {"text":"The Barrier Mends","color":"red"}',
                'player:title @s subtitle {"text":"The dead lose their foothold."}',
                'player:execute as @s at @s run playsound minecraft:block.bell.resonate master @s ~ ~ ~',
                'player:particle minecraft:end_rod ~ ~1 ~ 0.5 1 0.5 0.02 150 force @s',
                'server:weather clear'
            ]
        },

        corrupted: {
            commands: [
                'player:title @s title {"text":"The Barrier Endures","color":"dark_purple"}',
                'player:title @s subtitle {"text":"The darkness retreats, if only slightly."}',
                'player:execute as @s at @s run playsound minecraft:block.beacon.activate master @s ~ ~ ~',
                'particle minecraft:end_rod ~ ~1 ~ 0.5 1 0.5 0.02 100 force @s'
            ]
        }
    }
}



// barrier game stage definitions used for determining what stages players are in based on the barrier stat
global.getBarrierStage = server => {
     let b = server.persistentData.barrier

     if (b > 80) return 'protected'
     if (b > 60) return 'disturbed'
     if (b > 40) return 'breached'
     if (b > 20) return 'corrupted'

     return 'collapse'
}

// helper function to check barrier stage transitions
global.checkBarrierTransition = server => {
    let oldStage = server.persistentData.currentBarrierStage ?? "protected"
    let newStage = global.getBarrierStage(server)

    if (oldStage != newStage) {
        
        global.onBarrierStageChange(server, oldStage, newStage)
        server.persistentData.currentBarrierStage = newStage
    }
}



// barrier stage change notifications event handler
global.onBarrierStageChange = (
    server,
    oldStage,
    newStage
) => {

    let isFalling =
        barrierStageOrder[newStage] <
        barrierStageOrder[oldStage]

    const branch = isFalling ? 'fall' : 'mend'
    let eventData = barrierStageEvents[branch] && barrierStageEvents[branch][newStage]

    if (!eventData)
        return

    // If the event defines a `commands` array, run those commands.
    // Command strings that start with `player:` will be executed per-player
    // using `player.runCommandSilent(...)`. Strings that start with
    // `server:` (or have no prefix) will be executed once on the server
    // using `server.runCommandSilent(...)`.
    if (Array.isArray(eventData.commands) && eventData.commands.length > 0) {
        // Run server-level commands first
        eventData.commands.forEach(cmd => {
            if (typeof cmd !== 'string') return
            if (cmd.startsWith('player:')) return
            const actual = cmd.startsWith('server:') ? cmd.slice(7).trim() : cmd
            if (actual.length) server.runCommandSilent(actual)
        })

        // Run player-level commands for each player
        server.players.forEach(player => {
            eventData.commands.forEach(cmd => {
                if (typeof cmd !== 'string') return
                if (!cmd.startsWith('player:')) return
                const actual = cmd.slice(7).trim()
                if (actual.length) player.runCommandSilent(actual)
            })
        })

        return
    }

}