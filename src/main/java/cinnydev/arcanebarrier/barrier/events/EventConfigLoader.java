package cinnydev.arcanebarrier.barrier.events;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads event definitions from data/arcanebarrier/barrier/events.json.
 */
public class EventConfigLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String NAMESPACE = "arcanebarrier";
    private static final String PATH = "barrier/events.json";

    /**
     * Parses all configured events and returns an empty list when the file is missing.
     */
    public static List<EventDefinition> loadEvents(ResourceManager resourceManager) {
        List<EventDefinition> events = new ArrayList<>();
        
        try {
            ResourceLocation location = new ResourceLocation(NAMESPACE, PATH);
            var resource = resourceManager.getResource(location);
            
            if (resource.isEmpty()) {
                LOGGER.warn("Events configuration file not found at {}", PATH);
                return events;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    resource.get().open(), StandardCharsets.UTF_8))) {
                
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray eventsArray = root.getAsJsonArray("events");
                
                if (eventsArray != null) {
                    for (var element : eventsArray) {
                        try {
                            JsonObject eventJson = element.getAsJsonObject();
                            EventDefinition eventDef = EventDefinition.fromJson(eventJson);
                            events.add(eventDef);
                            LOGGER.debug("Loaded event: {}", eventDef);
                        } catch (Exception e) {
                            LOGGER.error("Failed to parse event definition", e);
                        }
                    }
                }
                
                LOGGER.info("Successfully loaded {} barrier events", events.size());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load events configuration", e);
        }
        
        return events;
    }
}
