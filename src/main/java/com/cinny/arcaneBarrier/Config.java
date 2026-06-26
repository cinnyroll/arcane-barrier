package com.cinny.arcaneBarrier;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Forge config definition and runtime mirror fields for Arcane Barrier.
 */
@Mod.EventBusSubscriber(modid = ArcaneBarrier.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue DEBUG_ENTITY_SPAWNS = BUILDER
            .comment("Enable barrier spawn gate debug logging")
            .define("debugEntitySpawns", false);

    private static final ForgeConfigSpec.BooleanValue DEBUG_STAGE_SYNC = BUILDER
            .comment("Enable debug logging for player stage sync commands")
            .define("debugStageSync", false);

    private static final ForgeConfigSpec.BooleanValue ENABLE_TRANSITIONS = BUILDER
            .comment("Enable transition command execution on barrier stage changes")
            .define("enableTransitions", true);

    public static final ForgeConfigSpec.IntValue DEFAULT_BARRIER = BUILDER
            .comment("Default barrier value for new worlds")
            .defineInRange("defaultBarrier", 100, 0, 100);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean debugEntitySpawns;
    public static boolean debugStageSync;
    public static boolean enableTransitions;
    public static int defaultBarrier;

        /**
         * Copies configured values into static runtime fields when Forge reloads config.
         */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        debugEntitySpawns = DEBUG_ENTITY_SPAWNS.get();
        debugStageSync = DEBUG_STAGE_SYNC.get();
        enableTransitions = ENABLE_TRANSITIONS.get();
        defaultBarrier = DEFAULT_BARRIER.get();
    }
}
