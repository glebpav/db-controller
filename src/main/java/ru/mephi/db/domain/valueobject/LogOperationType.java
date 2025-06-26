package ru.mephi.db.domain.valueobject;

public enum LogOperationType {
    BEGIN,
    COMMIT,
    ROLLBACK,
    CREATE_TABLE,
    DROP_TABLE,
    INSERT_RECORD,
    DELETE_RECORD,
}
