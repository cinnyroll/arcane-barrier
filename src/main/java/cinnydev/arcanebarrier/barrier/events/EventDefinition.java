package cinnydev.arcanebarrier.barrier.events;

import com.google.gson.JsonObject;

/**
 * Immutable definition of one barrier-affecting gameplay event loaded from JSON.
 */
public class EventDefinition {
    private final String id;
    private final String type;
    private final boolean enabled;
    private final int barrierChange;
    private final boolean firstTimeOnly;
    private final String description;
    private final JsonObject condition;

    public EventDefinition(String id, String type, boolean enabled, int barrierChange, 
                          boolean firstTimeOnly, String description, JsonObject condition) {
        this.id = id;
        this.type = type;
        this.enabled = enabled;
        this.barrierChange = barrierChange;
        this.firstTimeOnly = firstTimeOnly;
        this.description = description;
        this.condition = condition;
    }

    /**
     * Builds an event definition from one element in events.json.
     */
    public static EventDefinition fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String type = json.get("type").getAsString();
        boolean enabled = json.has("enabled") ? json.get("enabled").getAsBoolean() : true;
        int barrierChange = json.get("barrier_change").getAsInt();
        boolean firstTimeOnly = json.has("first_time_only") ? json.get("first_time_only").getAsBoolean() : false;
        String description = json.has("description") ? json.get("description").getAsString() : "";
        JsonObject condition = json.has("condition") ? json.getAsJsonObject("condition") : new JsonObject();

        return new EventDefinition(id, type, enabled, barrierChange, firstTimeOnly, description, condition);
    }

    /**
     * Unique event id used by commands and runtime tracking.
     */
    public String getId() {
        return id;
    }

    /**
     * Event trigger type, such as block_interact or advancement.
     */
    public String getType() {
        return type;
    }

    /**
     * Default enabled state loaded from config.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Barrier delta applied when this event triggers.
     */
    public int getBarrierChange() {
        return barrierChange;
    }

    /**
     * Returns true if this event can trigger only once per player.
     */
    public boolean isFirstTimeOnly() {
        return firstTimeOnly;
    }

    /**
     * Human-readable description shown by command output.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Type-specific condition object used by listeners to match triggers.
     */
    public JsonObject getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return String.format("EventDefinition{id='%s', type='%s', enabled=%b, barrierChange=%d, firstTimeOnly=%b}",
                id, type, enabled, barrierChange, firstTimeOnly);
    }
}
