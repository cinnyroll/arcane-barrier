package cinnydev.arcanebarrier.barrier.events;

import java.util.EnumSet;

import cinnydev.arcanebarrier.ArcaneBarrier;
import cinnydev.arcanebarrier.Config;
import cinnydev.arcanebarrier.barrier.BarrierService;
import cinnydev.arcanebarrier.barrier.BarrierStage;
import elocindev.necronomicon.api.NecUtilsAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Handles spawn cancellation logic by comparing an entity's ID (resolved via
 * {@link NecUtilsAPI#getEntityIdentifier}) against the set of entity IDs that
 * are permitted to spawn at the current {@link BarrierStage}.
 *
 * <p>Entity–stage associations are sourced from two places:
 * <ul>
 *   <li>Entity type tag files under {@code data/arcanebarrier/tags/entity_types/}</li>
 *   <li>Config-supplied ID lists per stage ({@link Config#configuredStages})</li>
 * </ul>
 *
 * <p>Tag membership is tested with {@code entity.getType().is(tagKey)}, which
 * correctly resolves datapack-loaded tags at runtime (unlike the deprecated
 * {@code builtInRegistryHolder()} path used previously).
 */
public class SpawnEventHandler {

    private static final TagKey<EntityType<?>> TAG_PROTECTED  = createTag(BarrierStage.PROTECTED.id());
    private static final TagKey<EntityType<?>> TAG_DISTURBED  = createTag(BarrierStage.DISTURBED.id());
    private static final TagKey<EntityType<?>> TAG_BREACHED   = createTag(BarrierStage.BREACHED.id());
    private static final TagKey<EntityType<?>> TAG_CORRUPTED  = createTag(BarrierStage.CORRUPTED.id());
    private static final TagKey<EntityType<?>> TAG_COLLAPSE   = createTag(BarrierStage.COLLAPSE.id());

    private final BarrierService barrierService;

    public SpawnEventHandler(BarrierService barrierService) {
        this.barrierService = barrierService;
    }

    /**
     * Returns {@code true} when the entity's spawn should be cancelled for the
     * current barrier stage.
     *
     * @param entity the entity attempting to join the level
     * @param server the running {@link MinecraftServer}
     * @return {@code true} to cancel, {@code false} to allow
     */
    public boolean shouldCancelSpawn(Entity entity, MinecraftServer server) {
        ResourceLocation entityId = NecUtilsAPI.getEntityIdentifier(entity);
        EnumSet<BarrierStage> entityStages = getEntityStages(entity, entityId);

        if (entityStages.isEmpty()) {
            return false;
        }

        BarrierStage current = barrierService.getBarrierStage(server);
        EnumSet<BarrierStage> allowed = getAllowedStages(current);

        boolean intersects = entityStages.stream().anyMatch(allowed::contains);

        if (Config.debugEntitySpawns) {
            ArcaneBarrier.LOGGER.info(
                    "Spawn check for {}: entityStages={}, allowedStages={}, result={}",
                    entityId, entityStages, allowed, intersects ? "allow" : "deny");
        }

        return !intersects;
    }

    /**
     * Collects the set of {@link BarrierStage}s this entity belongs to, sourced
     * from entity type tags and the forge config.
     */
    private EnumSet<BarrierStage> getEntityStages(Entity entity, ResourceLocation entityId) {
        EnumSet<BarrierStage> stages = EnumSet.noneOf(BarrierStage.class);

        if (entity.getType().is(TAG_PROTECTED))  stages.add(BarrierStage.PROTECTED);
        if (entity.getType().is(TAG_DISTURBED))  stages.add(BarrierStage.DISTURBED);
        if (entity.getType().is(TAG_BREACHED))   stages.add(BarrierStage.BREACHED);
        if (entity.getType().is(TAG_CORRUPTED))  stages.add(BarrierStage.CORRUPTED);
        if (entity.getType().is(TAG_COLLAPSE))   stages.add(BarrierStage.COLLAPSE);

        stages.addAll(Config.configuredStages(entityId));

        return stages;
    }

    /**
     * Returns the set of stages whose entities are allowed to spawn at the given
     * current barrier stage. Higher-rank stages are always included as the barrier
     * weakens, meaning anything that was already allowed continues to spawn.
     */
    private static EnumSet<BarrierStage> getAllowedStages(BarrierStage stage) {
        return switch (stage) {
            case PROTECTED -> EnumSet.of(BarrierStage.PROTECTED);
            case DISTURBED -> EnumSet.of(BarrierStage.PROTECTED, BarrierStage.DISTURBED);
            case BREACHED  -> EnumSet.of(BarrierStage.PROTECTED, BarrierStage.DISTURBED, BarrierStage.BREACHED);
            case CORRUPTED -> EnumSet.of(BarrierStage.PROTECTED, BarrierStage.DISTURBED, BarrierStage.BREACHED, BarrierStage.CORRUPTED);
            case COLLAPSE  -> EnumSet.allOf(BarrierStage.class);
        };
    }

    @SuppressWarnings({"removal", "null"})
    private static TagKey<EntityType<?>> createTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(ArcaneBarrier.MODID, path));
    }
}

