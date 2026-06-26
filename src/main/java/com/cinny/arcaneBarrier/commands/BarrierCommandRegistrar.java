package com.cinny.arcaneBarrier.commands;

import com.cinny.arcaneBarrier.barrier.BarrierSavedData;
import com.cinny.arcaneBarrier.barrier.BarrierService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
}
