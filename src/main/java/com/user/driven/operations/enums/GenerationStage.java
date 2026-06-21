package com.user.driven.operations.enums;

/**
 * Represents the stages in the project generation pipeline.
 * Each stage has an associated progress percentage.
 *
 * @author Jatin Raheja
 */
public enum GenerationStage {
    VALIDATION(10),
    STRUCTURE(30),
    ENTITIES(50),
    SECURITY(70),
    BUILD(90),
    ZIP(100);

    private final int progress;

    GenerationStage(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }
}
