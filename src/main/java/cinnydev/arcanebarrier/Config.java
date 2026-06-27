package cinnydev.arcanebarrier;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cinnydev.arcanebarrier.barrier.BarrierStage;
import net.minecraft.resources.ResourceLocation;
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

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PROTECTED_ENTITY_TYPES = BUILDER
            .comment("Additional entity type IDs allowed to start spawning at the protected stage")
            .defineList("protectedEntityTypes", List.of(), Config::isValidEntityTypeId);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DISTURBED_ENTITY_TYPES = BUILDER
            .comment("Additional entity type IDs allowed to start spawning at the disturbed stage")
            .defineList("disturbedEntityTypes", List.of(), Config::isValidEntityTypeId);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BREACHED_ENTITY_TYPES = BUILDER
            .comment("Additional entity type IDs allowed to start spawning at the breached stage")
            .defineList("breachedEntityTypes", List.of(), Config::isValidEntityTypeId);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CORRUPTED_ENTITY_TYPES = BUILDER
            .comment("Additional entity type IDs allowed to start spawning at the corrupted stage")
            .defineList("corruptedEntityTypes", List.of(), Config::isValidEntityTypeId);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> COLLAPSE_ENTITY_TYPES = BUILDER
            .comment("Additional entity type IDs allowed to start spawning at the collapse stage")
            .defineList("collapseEntityTypes", List.of(), Config::isValidEntityTypeId);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static final Map<BarrierStage, List<String>> EMPTY_STAGE_ENTITY_TYPES = Collections.unmodifiableMap(new EnumMap<>(BarrierStage.class));
        private static final Map<ResourceLocation, EnumSet<BarrierStage>> EMPTY_ENTITY_STAGE_MAP = Collections.emptyMap();

    public static boolean debugEntitySpawns;
    public static boolean debugStageSync;
    public static boolean enableTransitions;
    public static int defaultBarrier;
    public static Map<BarrierStage, List<String>> configuredEntityTypesByStage = EMPTY_STAGE_ENTITY_TYPES;
        public static Map<ResourceLocation, EnumSet<BarrierStage>> configuredStagesByEntityType = EMPTY_ENTITY_STAGE_MAP;

    public static List<String> configuredEntityTypes(BarrierStage stage) {
        return configuredEntityTypesByStage.getOrDefault(stage, List.of());
    }

        public static EnumSet<BarrierStage> configuredStages(ResourceLocation entityTypeId) {
                EnumSet<BarrierStage> configuredStages = configuredStagesByEntityType.get(entityTypeId);
                if (configuredStages == null || configuredStages.isEmpty()) {
                        return EnumSet.noneOf(BarrierStage.class);
                }
                return EnumSet.copyOf(configuredStages);
        }

        /**
         * Copies configured values into static runtime fields when Forge reloads config.
         */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
                if (event.getConfig().getSpec() != SPEC) {
                        return;
                }

        debugEntitySpawns = DEBUG_ENTITY_SPAWNS.get();
        debugStageSync = DEBUG_STAGE_SYNC.get();
        enableTransitions = ENABLE_TRANSITIONS.get();
        defaultBarrier = DEFAULT_BARRIER.get();
                configuredEntityTypesByStage = loadConfiguredEntityTypesByStage();
                configuredStagesByEntityType = loadConfiguredStagesByEntityType(configuredEntityTypesByStage);
    }

        private static Map<BarrierStage, List<String>> loadConfiguredEntityTypesByStage() {
                EnumMap<BarrierStage, List<String>> configuredEntityTypes = new EnumMap<>(BarrierStage.class);
                configuredEntityTypes.put(BarrierStage.PROTECTED, normalizeEntityTypeIds(PROTECTED_ENTITY_TYPES.get()));
                configuredEntityTypes.put(BarrierStage.DISTURBED, normalizeEntityTypeIds(DISTURBED_ENTITY_TYPES.get()));
                configuredEntityTypes.put(BarrierStage.BREACHED, normalizeEntityTypeIds(BREACHED_ENTITY_TYPES.get()));
                configuredEntityTypes.put(BarrierStage.CORRUPTED, normalizeEntityTypeIds(CORRUPTED_ENTITY_TYPES.get()));
                configuredEntityTypes.put(BarrierStage.COLLAPSE, normalizeEntityTypeIds(COLLAPSE_ENTITY_TYPES.get()));
                return Collections.unmodifiableMap(configuredEntityTypes);
        }

        private static List<String> normalizeEntityTypeIds(List<? extends String> entityTypeIds) {
                return entityTypeIds.stream()
                                .map(ResourceLocation::tryParse)
                                .filter(Objects::nonNull)
                        .map(resourceLocation -> resourceLocation.toString())
                                .toList();
        }

        private static Map<ResourceLocation, EnumSet<BarrierStage>> loadConfiguredStagesByEntityType(
                        Map<BarrierStage, List<String>> configuredEntityTypesByStage
        ) {
                EnumMap<BarrierStage, List<String>> stageEntityTypes = new EnumMap<>(configuredEntityTypesByStage);
                Map<ResourceLocation, EnumSet<BarrierStage>> stagesByEntityType = new java.util.HashMap<>();

                for (BarrierStage stage : BarrierStage.values()) {
                        for (String entityTypeId : stageEntityTypes.getOrDefault(stage, List.of())) {
                                ResourceLocation resourceLocation = ResourceLocation.tryParse(Objects.requireNonNull(entityTypeId));
                                if (resourceLocation == null) {
                                        continue;
                                }
                                stagesByEntityType
                                                .computeIfAbsent(resourceLocation, ignored -> EnumSet.noneOf(BarrierStage.class))
                                                .add(stage);
                        }
                }

                return Collections.unmodifiableMap(stagesByEntityType);
        }

        private static boolean isValidEntityTypeId(Object value) {
                if (!(value instanceof String stringValue)) {
                        return false;
                }

                return ResourceLocation.tryParse(stringValue) != null;
        }
}
