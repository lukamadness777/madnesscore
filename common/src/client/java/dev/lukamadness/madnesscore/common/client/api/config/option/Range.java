package dev.lukamadness.madnesscore.common.client.api.config.option;

public record Range(int min, int max, int step) implements SteppedValidator {
    public Range {
        if (min > max) {
            throw new IllegalArgumentException("Min must be less than or equal to max");
        }
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be greater than 0");
        }
    }
}
