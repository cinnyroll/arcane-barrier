package cinnydev.arcanebarrier.barrier;

import java.util.Locale;

/**
 * Ordered barrier stages used for gameplay gating and transition direction checks.
 */
public enum BarrierStage {
    PROTECTED("protected", 4),
    DISTURBED("disturbed", 3),
    BREACHED("breached", 2),
    CORRUPTED("corrupted", 1),
    COLLAPSE("collapse", 0);

    private final String id;
    private final int rank;

    BarrierStage(String id, int rank) {
        this.id = id;
        this.rank = rank;
    }

    /**
     * Returns lowercase stage identifier used in tags, commands, and JSON keys.
     */
    public String id() {
        return this.id;
    }

    /**
     * Returns stage rank where larger is safer and smaller is more degraded.
     */
    public int rank() {
        return this.rank;
    }

    /**
     * Converts a barrier value to its stage based on configured thresholds.
     */
    public static BarrierStage fromBarrier(int barrier) {
        if (barrier > 80) {
            return PROTECTED;
        }
        if (barrier > 60) {
            return DISTURBED;
        }
        if (barrier > 40) {
            return BREACHED;
        }
        if (barrier > 20) {
            return CORRUPTED;
        }
        return COLLAPSE;
    }

    /**
     * Parses a stage id and falls back to PROTECTED when value is missing or invalid.
     */
    public static BarrierStage fromString(String value) {
        if (value == null || value.isBlank()) {
            return PROTECTED;
        }

        String normalized = value.toLowerCase(Locale.ROOT);
        for (BarrierStage stage : values()) {
            if (stage.id.equals(normalized)) {
                return stage;
            }
        }
        return PROTECTED;
    }
}
