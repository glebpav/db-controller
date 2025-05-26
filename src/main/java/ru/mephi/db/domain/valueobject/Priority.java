package ru.mephi.db.domain.valueobject;

import lombok.Getter;

@Getter
public enum Priority {
    HIGHEST(0),
    HIGH(1),
    MEDIUM(2),
    LOW(3),
    LOWEST(4);

    private final int value;

    Priority(int value) {
        this.value = value;
    }
}
