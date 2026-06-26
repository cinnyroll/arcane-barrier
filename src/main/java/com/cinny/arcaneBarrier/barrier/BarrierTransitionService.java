package com.cinny.arcaneBarrier.barrier;

import com.cinny.arcaneBarrier.ArcaneBarrier;
import com.cinny.arcaneBarrier.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes transition commands when the barrier stage changes.
 */
public class BarrierTransitionService {
    /**
     * Transition direction inferred from old/new stage rank.
     */
    public enum Direction {
        FALL,
        MEND
    }

    /**
     * Runs configured transition commands for the new stage.
     * Server commands run once, then player commands run for each online player.
     */
    public void onBarrierStageChange(MinecraftServer server, BarrierStage oldStage, BarrierStage newStage) {
        if (!Config.enableTransitions) {
            return;
        }

        Direction direction = newStage.rank() < oldStage.rank() ? Direction.FALL : Direction.MEND;
        List<String> commands = BarrierTransitionDefinitions.load(server).commandsFor(direction, newStage);
        if (commands.isEmpty()) {
            return;
        }

        List<String> serverCommands = new ArrayList<>();
        List<String> playerCommands = new ArrayList<>();
        for (String command : commands) {
            if (command.startsWith("player:")) {
                playerCommands.add(command.substring("player:".length()).trim());
            } else if (command.startsWith("server:")) {
                serverCommands.add(command.substring("server:".length()).trim());
            } else {
                serverCommands.add(command.trim());
            }
        }

        CommandSourceStack serverSource = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        for (String command : serverCommands) {
            server.getCommands().performPrefixedCommand(serverSource, command);
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            CommandSourceStack playerSource = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            for (String command : playerCommands) {
                server.getCommands().performPrefixedCommand(playerSource, command);
            }
        }

        ArcaneBarrier.LOGGER.info("Barrier stage transition {} -> {} ({})", oldStage.id(), newStage.id(), direction.name().toLowerCase());
    }
}
