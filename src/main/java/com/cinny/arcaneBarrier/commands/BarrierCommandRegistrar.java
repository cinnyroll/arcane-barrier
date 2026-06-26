package com.cinny.arcaneBarrier.commands;

import com.cinny.arcaneBarrier.barrier.BarrierSavedData;
import com.cinny.arcaneBarrier.barrier.BarrierService;
import com.cinny.arcaneBarrier.barrier.events.EventService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BarrierCommandRegistrar {
    private final BarrierService barrierService;

    public BarrierCommandRegistrar(BarrierService barrierService) {
        this.barrierService = barrierService;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("barrier")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("debug").executes(this::debug))
                        .then(Commands.literal("raw").executes(this::raw))
                        .then(Commands.literal("refresh").executes(this::refresh))
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(context -> set(context, IntegerArgumentType.getInteger(context, "value")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(context -> add(context, IntegerArgumentType.getInteger(context, "value")))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(context -> remove(context, IntegerArgumentType.getInteger(context, "value")))))
                        .then(Commands.literal("event")
                                .then(Commands.literal("list").executes(this::eventList))
                                .then(Commands.literal("enable")
                                        .then(Commands.argument("event_id", StringArgumentType.word())
                                                .executes(context -> eventEnable(context, StringArgumentType.getString(context, "event_id")))))
                                .then(Commands.literal("disable")
                                        .then(Commands.argument("event_id", StringArgumentType.word())
                                                .executes(context -> eventDisable(context, StringArgumentType.getString(context, "event_id")))))
                                .then(Commands.literal("info")
                                        .then(Commands.argument("event_id", StringArgumentType.word())
                                                .executes(context -> eventInfo(context, StringArgumentType.getString(context, "event_id"))))))
        );
    }

    private int debug(CommandContext<CommandSourceStack> context) {
        int barrier = barrierService.getBarrier(context.getSource().getServer());
        String stage = barrierService.getBarrierStage(context.getSource().getServer()).id();

        context.getSource().sendSuccess(() -> Component.literal("Barrier value: " + barrier), false);
        context.getSource().sendSuccess(() -> Component.literal("Barrier stage: " + stage), false);
        context.getSource().sendSuccess(
                () -> Component.literal("Online players synced: " + context.getSource().getServer().getPlayerList().getPlayers().size()),
                false
        );
        return barrier;
    }

    private int set(CommandContext<CommandSourceStack> context, int value) {
        barrierService.setBarrier(context.getSource().getServer(), value);
        int barrier = barrierService.getBarrier(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("Barrier set to " + barrier), true);
        return barrier;
    }

    private int add(CommandContext<CommandSourceStack> context, int value) {
        barrierService.changeBarrier(context.getSource().getServer(), value);
        int barrier = barrierService.getBarrier(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("Barrier increased to " + barrier), true);
        return barrier;
    }

    private int remove(CommandContext<CommandSourceStack> context, int value) {
        barrierService.changeBarrier(context.getSource().getServer(), -value);
        int barrier = barrierService.getBarrier(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("Barrier reduced to " + barrier), true);
        return barrier;
    }

    private int raw(CommandContext<CommandSourceStack> context) {
        BarrierSavedData data = barrierService.getData(context.getSource().getServer());
        CompoundTag dump = data.save(new CompoundTag());
        context.getSource().sendSuccess(() -> Component.literal(dump.toString()), false);
        return 1;
    }

    private int refresh(CommandContext<CommandSourceStack> context) {
        barrierService.updateBarrierStage(context.getSource().getServer());
        barrierService.refreshBaseline(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("Barrier stages refreshed"), true);
        return 1;
    }

    private int eventList(CommandContext<CommandSourceStack> context) {
        EventService eventService = barrierService.getEventService();
        if (eventService == null) {
            context.getSource().sendFailure(Component.literal("EventService not initialized"));
            return 0;
        }

        var allEvents = eventService.getAllEvents();
        if (allEvents.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No events loaded"), false);
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("Loaded events:"), false);
        for (var event : allEvents.values()) {
            String status = eventService.isEventEnabled(event.getId()) ? "ENABLED" : "DISABLED";
            String message = String.format("  - %s [%s] - change: %d", event.getId(), status, event.getBarrierChange());
            context.getSource().sendSuccess(() -> Component.literal(message), false);
        }
        return 1;
    }

    private int eventEnable(CommandContext<CommandSourceStack> context, String eventId) {
        EventService eventService = barrierService.getEventService();
        if (eventService == null) {
            context.getSource().sendFailure(Component.literal("EventService not initialized"));
            return 0;
        }

        if (!eventService.getEvent(eventId).isPresent()) {
            context.getSource().sendFailure(Component.literal("Event not found: " + eventId));
            return 0;
        }

        eventService.setEventEnabled(eventId, true);
        context.getSource().sendSuccess(() -> Component.literal("Event enabled: " + eventId), true);
        return 1;
    }

    private int eventDisable(CommandContext<CommandSourceStack> context, String eventId) {
        EventService eventService = barrierService.getEventService();
        if (eventService == null) {
            context.getSource().sendFailure(Component.literal("EventService not initialized"));
            return 0;
        }

        if (!eventService.getEvent(eventId).isPresent()) {
            context.getSource().sendFailure(Component.literal("Event not found: " + eventId));
            return 0;
        }

        eventService.setEventEnabled(eventId, false);
        context.getSource().sendSuccess(() -> Component.literal("Event disabled: " + eventId), true);
        return 1;
    }

    private int eventInfo(CommandContext<CommandSourceStack> context, String eventId) {
        EventService eventService = barrierService.getEventService();
        if (eventService == null) {
            context.getSource().sendFailure(Component.literal("EventService not initialized"));
            return 0;
        }

        var eventOpt = eventService.getEvent(eventId);
        if (!eventOpt.isPresent()) {
            context.getSource().sendFailure(Component.literal("Event not found: " + eventId));
            return 0;
        }

        var event = eventOpt.get();
        String status = eventService.isEventEnabled(eventId) ? "ENABLED" : "DISABLED";
        String fto = event.isFirstTimeOnly() ? "YES" : "NO";
        
        context.getSource().sendSuccess(() -> Component.literal("Event: " + eventId), false);
        context.getSource().sendSuccess(() -> Component.literal("  Type: " + event.getType()), false);
        context.getSource().sendSuccess(() -> Component.literal("  Status: " + status), false);
        context.getSource().sendSuccess(() -> Component.literal("  Barrier Change: " + event.getBarrierChange()), false);
        context.getSource().sendSuccess(() -> Component.literal("  First Time Only: " + fto), false);
        if (!event.getDescription().isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("  Description: " + event.getDescription()), false);
        }
        return 1;
    }
}
