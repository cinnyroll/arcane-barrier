package cinnydev.arcanebarrier.barrier;

import cinnydev.arcanebarrier.ArcaneBarrier;
import cinnydev.arcanebarrier.Config;
import cinnydev.arcanebarrier.barrier.events.EventService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Central service that owns barrier mutation, stage synchronization, and transition checks.
 */
public class BarrierService {
    private final BarrierTransitionService transitionService = new BarrierTransitionService();
    private EventService eventService;

    /**
     * Returns the persisted barrier value in the inclusive range 0..100.
     */
    public int getBarrier(MinecraftServer server) {
        return BarrierSavedData.get(server).getBarrier();
    }

    /**
     * Resolves the current barrier stage from the persisted barrier value.
     */
    public BarrierStage getBarrierStage(MinecraftServer server) {
        return BarrierStage.fromBarrier(getBarrier(server));
    }

    /**
     * Applies a relative barrier delta, clamps the result, then updates stage and transitions.
     */
    public void changeBarrier(MinecraftServer server, int amount) {
        BarrierSavedData data = BarrierSavedData.get(server);
        int nextBarrier = clamp(data.getBarrier() + amount);
        applyBarrier(server, data, nextBarrier, true);
    }

    /**
     * Sets an absolute barrier value, then updates stage and transitions.
     */
    public void setBarrier(MinecraftServer server, int value) {
        BarrierSavedData data = BarrierSavedData.get(server);
        applyBarrier(server, data, clamp(value), true);
    }

    /**
     * Assigns exactly one GameStage to a player by clearing all barrier stages first.
     */
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

    /**
     * Synchronizes barrier stage for every online player.
     */
    public void updateBarrierStage(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncPlayerBarrierStage(player, server);
        }
    }

    /**
     * Detects stage changes and invokes transition actions when stage differs.
     */
    public void checkBarrierTransition(MinecraftServer server) {
        BarrierSavedData data = BarrierSavedData.get(server);
        BarrierStage oldStage = data.getCurrentBarrierStage();
        BarrierStage newStage = getBarrierStage(server);

        if (oldStage != newStage) {
            transitionService.onBarrierStageChange(server, oldStage, newStage);
            data.setCurrentBarrierStage(newStage);
        }
    }

    /**
     * Resets transition baseline to the currently derived stage.
     */
    public void refreshBaseline(MinecraftServer server) {
        BarrierSavedData data = BarrierSavedData.get(server);
        data.setCurrentBarrierStage(getBarrierStage(server));
    }

    /**
     * Exposes SavedData for admin/debug commands.
     */
    public BarrierSavedData getData(MinecraftServer server) {
        return BarrierSavedData.get(server);
    }

    /**
     * Returns runtime event service, initialized on server start.
     */
    public EventService getEventService() {
        return this.eventService;
    }

    /**
     * Injects runtime event service used by listeners and command controls.
     */
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
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
