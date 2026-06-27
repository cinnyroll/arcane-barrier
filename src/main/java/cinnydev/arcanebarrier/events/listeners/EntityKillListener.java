package cinnydev.arcanebarrier.events.listeners;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cinnydev.arcanebarrier.ArcaneBarrier;
import cinnydev.arcanebarrier.barrier.events.EventService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Matches player kill events against configured entity_kill event conditions.
 */
@Mod.EventBusSubscriber(modid = "arcanebarrier", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class EntityKillListener {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Triggers matching entity_kill events when a player kills a configured entity type.
     */
    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        LivingEntity killedEntity = event.getEntity();
        
        // Only trigger if killed by a player
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (killedEntity.level().isClientSide) {
            return;
        }

        MinecraftServer server = ((net.minecraft.server.level.ServerLevel)killedEntity.level()).getServer();

        EventService eventService = ArcaneBarrier.barrierService().getEventService();
        if (eventService == null) {
            return;
        }

        ResourceLocation killedEntityType = BuiltInRegistries.ENTITY_TYPE.getKey(killedEntity.getType());
        if (killedEntityType == null) {
            return;
        }

        // Check each event to see if it's an entity_kill type that matches this entity
        for (var eventEntry : eventService.getAllEvents().entrySet()) {
            String eventId = eventEntry.getKey();
            var eventDef = eventEntry.getValue();
            
            if (!"entity_kill".equals(eventDef.getType())) {
                continue;
            }

            JsonObject condition = eventDef.getCondition();
            if (!condition.has("entity_types")) {
                continue;
            }

            JsonArray entityTypesArray = condition.getAsJsonArray("entity_types");
            for (var element : entityTypesArray) {
                String entityTypeName = element.getAsString();
                ResourceLocation entityTypeRL = new ResourceLocation(entityTypeName);
                
                if (entityTypeRL.equals(killedEntityType)) {
                    eventService.triggerEvent(eventId, player, server);
                    break;
                }
            }
        }
    }
}
