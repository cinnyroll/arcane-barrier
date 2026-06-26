package com.cinny.arcaneBarrier;

import com.cinny.arcaneBarrier.barrier.BarrierService;
import com.cinny.arcaneBarrier.commands.BarrierCommandRegistrar;
import com.cinny.arcaneBarrier.events.BarrierEventHandlers;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArcaneBarrier.MODID)
public class ArcaneBarrier {
    public static final String MODID = "arcanebarrier";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final BarrierService BARRIER_SERVICE = new BarrierService();

    public ArcaneBarrier() {
        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        MinecraftForge.EVENT_BUS.register(new BarrierEventHandlers(BARRIER_SERVICE));
        MinecraftForge.EVENT_BUS.register(new BarrierCommandRegistrar(BARRIER_SERVICE));

        LOGGER.info("Arcane Barrier initialized");
    }

    public static BarrierService barrierService() {
        return BARRIER_SERVICE;
    }
}
