package com.cinny.arcaneBarrier.events;

import com.cinny.arcaneBarrier.ArcaneBarrier;
import com.cinny.arcaneBarrier.barrier.BarrierSavedData;
import com.cinny.arcaneBarrier.barrier.BarrierService;
import com.cinny.arcaneBarrier.barrier.BarrierSpawnGateService;
import com.cinny.arcaneBarrier.barrier.events.EventConfigLoader;
import com.cinny.arcaneBarrier.barrier.events.EventDefinition;
import com.cinny.arcaneBarrier.barrier.events.EventService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.List;

public class BarrierEventHandlers {
    private static final ResourceLocation DIAMOND_SPELL_BOOK = new ResourceLocation("irons_spellbooks", "diamond_spell_book");

    private final BarrierService barrierService;
    private final BarrierSpawnGateService spawnGateService;

    public BarrierEventHandlers(BarrierService barrierService) {
        this.barrierService = barrierService;
        this.spawnGateService = new BarrierSpawnGateService(barrierService);
    }

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

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            barrierService.syncPlayerBarrierStage(serverPlayer, serverPlayer.server);
        }
    }

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

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getLevel().getServer() == null) {
            return;
        }

        if (spawnGateService.shouldCancel(event.getEntity(), event.getLevel().getServer())) {
            event.setCanceled(true);
        }
    }
}
