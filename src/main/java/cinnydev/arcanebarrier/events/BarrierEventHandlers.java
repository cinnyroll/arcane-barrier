package cinnydev.arcanebarrier.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.List;

import cinnydev.arcanebarrier.ArcaneBarrier;
import cinnydev.arcanebarrier.barrier.BarrierSavedData;
import cinnydev.arcanebarrier.barrier.BarrierService;
import cinnydev.arcanebarrier.barrier.events.EventConfigLoader;
import cinnydev.arcanebarrier.barrier.events.EventDefinition;
import cinnydev.arcanebarrier.barrier.events.EventService;
import cinnydev.arcanebarrier.barrier.events.SpawnEventHandler;

/**
 * Forge lifecycle handlers for server startup, player sync, craft trigger, and spawn gating.
 */
public class BarrierEventHandlers {
    private static final ResourceLocation DIAMOND_SPELL_BOOK = new ResourceLocation("irons_spellbooks", "diamond_spell_book");

    private final BarrierService barrierService;
    private final SpawnEventHandler spawnEventHandler;

    public BarrierEventHandlers(BarrierService barrierService) {
        this.barrierService = barrierService;
        this.spawnEventHandler = new SpawnEventHandler(barrierService);
    }

    /**
     * Initializes barrier baseline and event service after server startup.
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        barrierService.getData(event.getServer());
        barrierService.updateBarrierStage(event.getServer());
        barrierService.refreshBaseline(event.getServer());
        
        // Initialize EventService
        List<EventDefinition> events = EventConfigLoader.loadEvents(event.getServer().getResourceManager());
        EventService eventService = new EventService(events);
        eventService.initialize(barrierService);
        barrierService.setEventService(eventService);
        ArcaneBarrier.LOGGER.info("EventService loaded with {} events", events.size());
    }

    /**
     * Synchronizes the joining player's stage assignment.
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            barrierService.syncPlayerBarrierStage(serverPlayer, serverPlayer.server);
        }
    }

    /**
     * Applies one-time barrier reduction when the configured spellbook is first crafted.
     */
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ResourceLocation craftedId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(event.getCrafting().getItem());
        if (!DIAMOND_SPELL_BOOK.equals(craftedId)) {
            return;
        }

        BarrierSavedData data = barrierService.getData(serverPlayer.server);
        if (!data.isFirstSpellbookCrafted()) {
            data.setFirstSpellbookCrafted(true);
            barrierService.changeBarrier(serverPlayer.server, -5);
        }
    }

    /**
     * Cancels entity spawn when blocked by stage-based spawn gate rules.
     */
    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getLevel().getServer() == null) {
            return;
        }

        if (spawnEventHandler.shouldCancelSpawn(event.getEntity(), event.getLevel().getServer())) {
            event.setCanceled(true);
        }
    }
}
