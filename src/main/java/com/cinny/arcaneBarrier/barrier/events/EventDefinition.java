package com.cinny.arcaneBarrier.barrier.events;

import com.google.gson.JsonObject;

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

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getBarrierChange() {
        return barrierChange;
    }

    public boolean isFirstTimeOnly() {
        return firstTimeOnly;
    }

    public String getDescription() {
        return description;
    }

    public JsonObject getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return String.format("EventDefinition{id='%s', type='%s', enabled=%b, barrierChange=%d, firstTimeOnly=%b}",
                id, type, enabled, barrierChange, firstTimeOnly);
    }
}
