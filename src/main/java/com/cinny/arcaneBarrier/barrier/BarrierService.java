package com.cinny.arcaneBarrier.barrier;

import com.cinny.arcaneBarrier.ArcaneBarrier;
import com.cinny.arcaneBarrier.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class BarrierService {
    private final BarrierTransitionService transitionService = new BarrierTransitionService();

    public int getBarrier(MinecraftServer server) {
        return BarrierSavedData.get(server).getBarrier();
    }

    public BarrierStage getBarrierStage(MinecraftServer server) {
        return BarrierStage.fromBarrier(getBarrier(server));
    }

    public void changeBarrier(MinecraftServer server, int amount) {
        BarrierSavedData data = BarrierSavedData.get(server);
        int nextBarrier = clamp(data.getBarrier() + amount);
        applyBarrier(server, data, nextBarrier, true);
    }

    public void setBarrier(MinecraftServer server, int value) {
        BarrierSavedData data = BarrierSavedData.get(server);
        applyBarrier(server, data, clamp(value), true);
    }

    public void syncPlayerBarrierStage(ServerPlayer player, MinecraftServer server) {
        BarrierStage stage = getBarrierStage(server);
        CommandSourceStack source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();

        for (BarrierStage knownStage : BarrierStage.values()) {
            String removeCommand = "gamestage remove " + player.getScoreboardName() + " " + knownStage.id();
            server.getCommands().performPrefixedCommand(source, removeCommand);
            if (Config.debugStageSync) {
                ArcaneBarrier.LOGGER.info("Stage sync command: {}", removeCommand);
            }
        }

        String addCommand = "gamestage add " + player.getScoreboardName() + " " + stage.id();
        server.getCommands().performPrefixedCommand(source, addCommand);
        if (Config.debugStageSync) {
            ArcaneBarrier.LOGGER.info("Stage sync command: {}", addCommand);
        }
    }

    public void updateBarrierStage(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncPlayerBarrierStage(player, server);
        }
    }

    public void checkBarrierTransition(MinecraftServer server) {
        BarrierSavedData data = BarrierSavedData.get(server);
        BarrierStage oldStage = data.getCurrentBarrierStage();
        BarrierStage newStage = getBarrierStage(server);

        if (oldStage != newStage) {
            transitionService.onBarrierStageChange(server, oldStage, newStage);
            data.setCurrentBarrierStage(newStage);
        }
    }

    public void refreshBaseline(MinecraftServer server) {
        BarrierSavedData data = BarrierSavedData.get(server);
        data.setCurrentBarrierStage(getBarrierStage(server));
    }

    public BarrierSavedData getData(MinecraftServer server) {
        return BarrierSavedData.get(server);
    }

    private void applyBarrier(MinecraftServer server, BarrierSavedData data, int clamped, boolean checkTransition) {
        data.setBarrier(clamped);
        updateBarrierStage(server);
        if (checkTransition) {
            checkBarrierTransition(server);
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
