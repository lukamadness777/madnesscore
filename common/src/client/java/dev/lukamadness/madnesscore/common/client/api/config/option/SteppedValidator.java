package dev.lukamadness.madnesscore.common.client.api.config.option;

import java.util.function.Supplier;

public interface SteppedValidator extends Validator<Integer> {
    int min();

    int max();

    int step();

    default boolean isValueValid(int value) {
        int min = this.min();
        return value >= min && value <= this.max() && (value - min) % this.step() == 0;
    }

    @Override
    default Integer getValidatedValue(Integer value, Supplier<Integer> defaultValueSupplier) {
        if (this.isValueValid(value)) {
            return value;
        }
        return defaultValueSupplier.get();
    }
}
