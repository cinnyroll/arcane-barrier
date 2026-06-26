package com.cinny.arcaneBarrier.barrier.events;

import com.cinny.arcaneBarrier.barrier.BarrierSavedData;
import com.cinny.arcaneBarrier.barrier.BarrierService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EventService {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private final Map<String, EventDefinition> events;
    private final Map<String, Boolean> enabledStates;
    private BarrierService barrierService;

    public EventService(List<EventDefinition> eventDefinitions) {
        this.events = new HashMap<>();
        this.enabledStates = new HashMap<>();
        
        for (EventDefinition eventDef : eventDefinitions) {
            this.events.put(eventDef.getId(), eventDef);
            this.enabledStates.put(eventDef.getId(), eventDef.isEnabled());
        }
    }

    /**
     * Initialize the EventService with required dependencies
     */
    public void initialize(BarrierService barrierService) {
        this.barrierService = barrierService;
        LOGGER.info("EventService initialized with {} events", this.events.size());
    }

    /**
     * Trigger an event for a player
     */
    public void triggerEvent(String eventId, Player player, MinecraftServer server) {
        if (!this.events.containsKey(eventId)) {
            LOGGER.warn("Attempted to trigger unknown event: {}", eventId);
            return;
        }

        if (!this.isEventEnabled(eventId)) {
            LOGGER.debug("Event {} is disabled, skipping trigger", eventId);
            return;
        }

        EventDefinition event = this.events.get(eventId);
        BarrierSavedData savedData = BarrierSavedData.get(server);

        // Check first-time-only condition
        if (event.isFirstTimeOnly()) {
            if (savedData.hasPlayerTriggeredEvent(eventId, player.getUUID())) {
                LOGGER.debug("Player {} has already triggered first-time-only event {}, skipping", 
                        player.getName().getString(), eventId);
                return;
            }
        }

        // Apply barrier change
        this.barrierService.changeBarrier(server, event.getBarrierChange());
        LOGGER.info("Event {} triggered for player {}: barrier changed by {}", 
                eventId, player.getName().getString(), event.getBarrierChange());

        // Mark as triggered if first-time-only
        if (event.isFirstTimeOnly()) {
            savedData.markEventTriggered(eventId, player.getUUID());
        }
    }

    /**
     * Check if an event is currently enabled
     */
    public boolean isEventEnabled(String eventId) {
        return this.enabledStates.getOrDefault(eventId, false);
    }

    /**
     * Enable an event at runtime
     */
    public void setEventEnabled(String eventId, boolean enabled) {
        if (!this.events.containsKey(eventId)) {
            LOGGER.warn("Attempted to enable/disable unknown event: {}", eventId);
            return;
        }
        this.enabledStates.put(eventId, enabled);
        LOGGER.info("Event {} is now {}", eventId, enabled ? "enabled" : "disabled");
    }

    /**
     * Get an event definition by ID
     */
    public Optional<EventDefinition> getEvent(String eventId) {
        return Optional.ofNullable(this.events.get(eventId));
    }

    /**
     * Get all events
     */
    public Map<String, EventDefinition> getAllEvents() {
        return new HashMap<>(this.events);
    }

    /**
     * Get all enabled event IDs
     */
    public java.util.Collection<String> getEnabledEventIds() {
        return this.enabledStates.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }
}
