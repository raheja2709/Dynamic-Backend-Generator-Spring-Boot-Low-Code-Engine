package com.user.driven.operations.generator.module.dto;

import com.user.driven.operations.enums.DtoStrategy;
import org.springframework.stereotype.Component;

/**
 * Factory that resolves a DtoStrategy enum to its corresponding generator implementation.
 *
 * @author Jatin Raheja
 */
@Component
public class DtoStrategyFactory {

    private final IdOnlyStrategyGenerator idOnlyGenerator;
    private final SummaryStrategyGenerator summaryGenerator;
    private final NestedStrategyGenerator nestedGenerator;
    private final IgnoreStrategyGenerator ignoreGenerator;

    public DtoStrategyFactory(IdOnlyStrategyGenerator idOnlyGenerator,
                              SummaryStrategyGenerator summaryGenerator,
                              NestedStrategyGenerator nestedGenerator,
                              IgnoreStrategyGenerator ignoreGenerator) {
        this.idOnlyGenerator = idOnlyGenerator;
        this.summaryGenerator = summaryGenerator;
        this.nestedGenerator = nestedGenerator;
        this.ignoreGenerator = ignoreGenerator;
    }

    /**
     * Returns the appropriate DtoStrategyGenerator for the given strategy.
     * Defaults to ID_ONLY if strategy is null.
     *
     * @param strategy the DTO strategy enum value
     * @return the corresponding generator
     */
    public DtoStrategyGenerator getGenerator(DtoStrategy strategy) {
        if (strategy == null) {
            return idOnlyGenerator;
        }
        return switch (strategy) {
            case ID_ONLY -> idOnlyGenerator;
            case SUMMARY -> summaryGenerator;
            case NESTED -> nestedGenerator;
            case IGNORE -> ignoreGenerator;
        };
    }
}
