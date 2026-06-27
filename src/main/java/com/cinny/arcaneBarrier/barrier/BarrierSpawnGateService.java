package com.cinny.arcaneBarrier.barrier;

import java.util.EnumSet;
import java.util.Objects;

import com.cinny.arcaneBarrier.ArcaneBarrier;
import com.cinny.arcaneBarrier.Config;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Enforces stage-based spawn gating using entity type tags.
 */
public class BarrierSpawnGateService {
    private static final TagKey<EntityType<?>> PROTECTED = createTag(BarrierStage.PROTECTED.id());
    private static final TagKey<EntityType<?>> DISTURBED = createTag(BarrierStage.DISTURBED.id());
    private static final TagKey<EntityType<?>> BREACHED = createTag(BarrierStage.BREACHED.id());
    private static final TagKey<EntityType<?>> CORRUPTED = createTag(BarrierStage.CORRUPTED.id());
    private static final TagKey<EntityType<?>> COLLAPSE = createTag(BarrierStage.COLLAPSE.id());

    private final BarrierService barrierService;

    public BarrierSpawnGateService(BarrierService barrierService) {
        this.barrierService = barrierService;
    }

    /**
     * Returns true when an entity spawn should be denied for the current barrier stage.
     */
    public boolean shouldCancel(Entity entity, MinecraftServer server) {
        EnumSet<BarrierStage> entityStages = getEntityStages(entity);
        if (entityStages.isEmpty()) {
            return false;
        }

        BarrierStage current = barrierService.getBarrierStage(server);
        EnumSet<BarrierStage> allowed = allowedStages(current);

        boolean intersects = entityStages.stream().anyMatch(allowed::contains);
        if (Config.debugEntitySpawns) {
            EntityType<?> entityType = Objects.requireNonNull(entity.getType());
            ArcaneBarrier.LOGGER.info(
                    "Spawn check for {} stages={}, allowed={}, result={}",
                    EntityType.getKey(entityType), entityStages, allowed, intersects ? "allow" : "deny"
            );
        }
        return !intersects;
    }

    /**
     * Collects all barrier stage tags attached to an entity type.
     */
    @SuppressWarnings({"deprecation", "null"})
    private EnumSet<BarrierStage> getEntityStages(Entity entity) {
        EnumSet<BarrierStage> stages = EnumSet.noneOf(BarrierStage.class);
        EntityType<?> entityType = Objects.requireNonNull(entity.getType());
        if (entityType.builtInRegistryHolder().is(PROTECTED)) {
            stages.add(BarrierStage.PROTECTED);
        }
        if (entityType.builtInRegistryHolder().is(DISTURBED)) {
            stages.add(BarrierStage.DISTURBED);
        }
        if (entityType.builtInRegistryHolder().is(BREACHED)) {
            stages.add(BarrierStage.BREACHED);
        }
        if (entityType.builtInRegistryHolder().is(CORRUPTED)) {
            stages.add(BarrierStage.CORRUPTED);
        }
        if (entityType.builtInRegistryHolder().is(COLLAPSE)) {
            stages.add(BarrierStage.COLLAPSE);
        }

        ResourceLocation entityTypeId = EntityType.getKey(entityType);
        if (entityTypeId != null) {
            stages.addAll(Config.configuredStages(entityTypeId));
        }

        return stages;
    }

    /**
     * Returns the stage set currently allowed to spawn for a world stage.
     */
    private static EnumSet<BarrierStage> allowedStages(BarrierStage stage) {
        return switch (stage) {
            case PROTECTED -> EnumSet.of(BarrierStage.PROTECTED);
            case DISTURBED -> EnumSet.of(BarrierStage.PROTECTED, BarrierStage.DISTURBED);
            case BREACHED -> EnumSet.of(BarrierStage.PROTECTED, BarrierStage.DISTURBED, BarrierStage.BREACHED);
            case CORRUPTED -> EnumSet.of(BarrierStage.PROTECTED, BarrierStage.DISTURBED, BarrierStage.BREACHED, BarrierStage.CORRUPTED);
            case COLLAPSE -> EnumSet.allOf(BarrierStage.class);
        };
    }

    /**
     * Builds a tag key under data/arcanebarrier/tags/entity_types.
     */
    @SuppressWarnings({"removal", "null"})
    private static TagKey<EntityType<?>> createTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(ArcaneBarrier.MODID, path));
    }
}
