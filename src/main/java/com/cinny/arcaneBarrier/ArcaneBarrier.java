package com.cinny.arcaneBarrier;

import com.cinny.arcaneBarrier.barrier.BarrierService;
import com.cinny.arcaneBarrier.commands.BarrierCommandRegistrar;
import com.cinny.arcaneBarrier.events.BarrierEventHandlers;
import com.cinny.arcaneBarrier.events.listeners.BlockInteractListener;
import com.cinny.arcaneBarrier.events.listeners.EntityKillListener;
import com.cinny.arcaneBarrier.events.listeners.DimensionChangeListener;
import com.cinny.arcaneBarrier.events.listeners.AdvancementListener;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Mod entrypoint responsible for wiring shared services and Forge event listeners.
 */
@Mod(ArcaneBarrier.MODID)
public class ArcaneBarrier {
    public static final String MODID = "arcanebarrier";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final BarrierService BARRIER_SERVICE = new BarrierService();

    /**
     * Registers config, core lifecycle handlers, command handlers, and event listeners.
     */
    public ArcaneBarrier() {
        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        MinecraftForge.EVENT_BUS.register(new BarrierEventHandlers(BARRIER_SERVICE));
        MinecraftForge.EVENT_BUS.register(new BarrierCommandRegistrar(BARRIER_SERVICE));
        
        // Register event listeners
        MinecraftForge.EVENT_BUS.register(BlockInteractListener.class);
        MinecraftForge.EVENT_BUS.register(EntityKillListener.class);
        MinecraftForge.EVENT_BUS.register(DimensionChangeListener.class);
        MinecraftForge.EVENT_BUS.register(AdvancementListener.class);

        LOGGER.info("Arcane Barrier initialized");
    }

    /**
     * Returns the singleton barrier service used by listeners and command handlers.
     */
    public static BarrierService barrierService() {
        return BARRIER_SERVICE;
    }
}
