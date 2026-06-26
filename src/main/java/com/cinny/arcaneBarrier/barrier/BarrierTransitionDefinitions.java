package com.cinny.arcaneBarrier.barrier;

import com.cinny.arcaneBarrier.ArcaneBarrier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BarrierTransitionDefinitions {
    public static final ResourceLocation TRANSITIONS_RESOURCE = new ResourceLocation(ArcaneBarrier.MODID, "barrier/transitions.json");

    private BarrierTransitionDefinitions() {
    }

    public static TransitionDefinitions load(MinecraftServer server) {
        Optional<Resource> resource = server.getResourceManager().getResource(TRANSITIONS_RESOURCE);
        if (resource.isEmpty()) {
            return TransitionDefinitions.empty();
        }

        try (InputStreamReader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            Map<BarrierTransitionService.Direction, Map<BarrierStage, List<String>>> table = new EnumMap<>(BarrierTransitionService.Direction.class);
            table.put(BarrierTransitionService.Direction.FALL, parseBranch(root.getAsJsonObject("fall")));
            table.put(BarrierTransitionService.Direction.MEND, parseBranch(root.getAsJsonObject("mend")));
            return new TransitionDefinitions(table);
        } catch (Exception exception) {
            ArcaneBarrier.LOGGER.error("Failed to load barrier transitions from {}", TRANSITIONS_RESOURCE, exception);
            return TransitionDefinitions.empty();
        }
    }

    private static Map<BarrierStage, List<String>> parseBranch(JsonObject branch) {
        if (branch == null) {
            return Collections.emptyMap();
        }

        Map<BarrierStage, List<String>> byStage = new EnumMap<>(BarrierStage.class);
        for (BarrierStage stage : BarrierStage.values()) {
            JsonObject stageObject = branch.getAsJsonObject(stage.id());
            if (stageObject == null || !stageObject.has("commands")) {
                continue;
            }
            JsonArray commands = stageObject.getAsJsonArray("commands");
            List<String> parsedCommands = new ArrayList<>();
            for (JsonElement element : commands) {
                parsedCommands.add(element.getAsString());
            }
            byStage.put(stage, parsedCommands);
        }
        return byStage;
    }

    public record TransitionDefinitions(Map<BarrierTransitionService.Direction, Map<BarrierStage, List<String>>> table) {
        public static TransitionDefinitions empty() {
            return new TransitionDefinitions(Collections.emptyMap());
        }

        public List<String> commandsFor(BarrierTransitionService.Direction direction, BarrierStage stage) {
            return table.getOrDefault(direction, Collections.emptyMap()).getOrDefault(stage, List.of());
        }
    }
}
