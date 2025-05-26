package ru.mephi.db.domain.valueobject;

public enum QueryType {
    SELECT,
    INSERT,
    DELETE,
    BEGIN_TRANSACTION,
    COMMIT,
    ROLLBACK,
    SHOW_FILES,
    SHOW_TABLES
}