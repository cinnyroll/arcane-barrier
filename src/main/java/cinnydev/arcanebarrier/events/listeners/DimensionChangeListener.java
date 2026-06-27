package cinnydev.arcanebarrier.events.listeners;

import com.google.gson.JsonObject;

import cinnydev.arcanebarrier.ArcaneBarrier;
import cinnydev.arcanebarrier.barrier.events.EventService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Matches player dimension transitions against configured dimension_change events.
 */
@Mod.EventBusSubscriber(modid = "arcanebarrier", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class DimensionChangeListener {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Triggers configured events when from/to dimensions match event conditions.
     */
    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        MinecraftServer server = ((net.minecraft.server.level.ServerLevel)player.level()).getServer();
        
        EventService eventService = ArcaneBarrier.barrierService().getEventService();
        if (eventService == null) {
            return;
        }

        // Get the new dimension
        ResourceLocation newDimension = player.level().dimension().location();
        
        // For "from" dimension, we need to track what they came from
        ResourceLocation fromDimension = event.getFrom().location();

        // Check each event to see if it's a dimension_change type that matches this transition
        for (var eventEntry : eventService.getAllEvents().entrySet()) {
            String eventId = eventEntry.getKey();
            var eventDef = eventEntry.getValue();
            
            if (!"dimension_change".equals(eventDef.getType())) {
                continue;
            }

            JsonObject condition = eventDef.getCondition();
            if (!condition.has("from_dimension") || !condition.has("to_dimension")) {
                continue;
            }

            String configFromDim = condition.get("from_dimension").getAsString();
            String configToDim = condition.get("to_dimension").getAsString();
            
            ResourceLocation fromDimRL = new ResourceLocation(configFromDim);
            ResourceLocation toDimRL = new ResourceLocation(configToDim);

            if (fromDimension.equals(fromDimRL) && newDimension.equals(toDimRL)) {
                eventService.triggerEvent(eventId, player, server);
            }
        }
    }
}
