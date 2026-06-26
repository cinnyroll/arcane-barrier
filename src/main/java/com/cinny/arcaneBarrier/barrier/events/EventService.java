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

/**
 * Runtime event registry that applies barrier changes from configured event triggers.
 */
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
     * Initializes runtime dependencies after server resources are loaded.
     */
    public void initialize(BarrierService barrierService) {
        this.barrierService = barrierService;
        LOGGER.info("EventService initialized with {} events", this.events.size());
    }

    /**
        * Triggers an event for a player and mutates barrier value when conditions are met.
     */
    public void triggerEvent(String eventId, Player player, MinecraftServer server) {
        LOGGER.info("EventService.triggerEvent called for event {}", eventId);
        
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

        LOGGER.info("EventService: About to call changeBarrier with amount {}", event.getBarrierChange());
        LOGGER.info("EventService: barrierService is {}", this.barrierService == null ? "NULL" : "NOT NULL");
        
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
     * Returns true when an event is currently enabled at runtime.
     */
    public boolean isEventEnabled(String eventId) {
        return this.enabledStates.getOrDefault(eventId, false);
    }

    /**
     * Enables or disables an event at runtime.
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
     * Returns an event definition by id when present.
     */
    public Optional<EventDefinition> getEvent(String eventId) {
        return Optional.ofNullable(this.events.get(eventId));
    }

    /**
     * Returns a defensive copy of all loaded events.
     */
    public Map<String, EventDefinition> getAllEvents() {
        return new HashMap<>(this.events);
    }

    /**
     * Returns ids of events currently enabled at runtime.
     */
    public java.util.Collection<String> getEnabledEventIds() {
        return this.enabledStates.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }
}
