package com.cinny.arcaneBarrier.events.listeners;

import com.cinny.arcaneBarrier.ArcaneBarrier;
import com.cinny.arcaneBarrier.barrier.events.EventService;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Matches advancement progress events against configured advancement event definitions.
 */
@Mod.EventBusSubscriber(modid = "arcanebarrier", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class AdvancementListener {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Triggers matching advancement events for the current player and advancement id.
     */
    @SubscribeEvent
    public static void onAdvancementProgressEvent(AdvancementEvent.AdvancementProgressEvent event) {
        Player player = event.getEntity();
        MinecraftServer server = ((net.minecraft.server.level.ServerLevel)player.level()).getServer();
        
        EventService eventService = ArcaneBarrier.barrierService().getEventService();
        if (eventService == null) {
            return;
        }

        // Get the advancement ID
        ResourceLocation advancementId = event.getAdvancement().getId();

        // Check each event to see if it's an advancement type that matches this advancement
        for (var eventEntry : eventService.getAllEvents().entrySet()) {
            String eventId = eventEntry.getKey();
            var eventDef = eventEntry.getValue();
            
            if (!"advancement".equals(eventDef.getType())) {
                continue;
            }

            JsonObject condition = eventDef.getCondition();
            if (!condition.has("advancement_id")) {
                continue;
            }

            String configAdvancementId = condition.get("advancement_id").getAsString();
            ResourceLocation configAdvRL = new ResourceLocation(configAdvancementId);

            if (advancementId.equals(configAdvRL)) {
                eventService.triggerEvent(eventId, player, server);
            }
        }
    }
}
