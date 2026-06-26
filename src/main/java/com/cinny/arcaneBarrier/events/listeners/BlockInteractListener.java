package com.cinny.arcaneBarrier.events.listeners;

import com.cinny.arcaneBarrier.ArcaneBarrier;
import com.cinny.arcaneBarrier.barrier.events.EventService;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Matches right-click block interactions against configured block_interact events.
 */
@Mod.EventBusSubscriber(modid = "arcanebarrier", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class BlockInteractListener {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Triggers matching block_interact events for right-clicked blocks.
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }

        Player player = event.getEntity();
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        MinecraftServer server = ((net.minecraft.server.level.ServerLevel)event.getLevel()).getServer();
        
        LOGGER.info("BlockInteractListener: Player {} right-clicked block {}", player.getName().getString(), block);
        
        EventService eventService = ArcaneBarrier.barrierService().getEventService();
        if (eventService == null) {
            LOGGER.warn("BlockInteractListener: EventService is null!");
            return;
        }

        // Check each event to see if it's a block_interact type that matches this block
        for (var eventEntry : eventService.getAllEvents().entrySet()) {
            String eventId = eventEntry.getKey();
            var eventDef = eventEntry.getValue();
            
            if (!"block_interact".equals(eventDef.getType())) {
                continue;
            }

            JsonObject condition = eventDef.getCondition();
            if (!condition.has("block")) {
                continue;
            }

            String blockName = condition.get("block").getAsString();
            ResourceLocation blockRL = new ResourceLocation(blockName);
            Block targetBlock = BuiltInRegistries.BLOCK.get(blockRL);
            
            LOGGER.info("BlockInteractListener: Checking event {} - target block: {}, clicked block: {}, match: {}", 
                    eventId, blockName, block, targetBlock == block);

            if (targetBlock == block) {
                LOGGER.info("BlockInteractListener: MATCH FOUND! Triggering event {}", eventId);
                eventService.triggerEvent(eventId, player, server);
            }
        }
    }
}
