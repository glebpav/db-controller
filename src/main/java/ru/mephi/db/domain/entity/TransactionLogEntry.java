package ru.mephi.db.domain.entity;

import lombok.*;
import ru.mephi.db.domain.valueobject.LogOperationType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor()
public class TransactionLogEntry {

    private LogOperationType operationType;

    // Имя транзакции (опционально, для идентификации вложенных транзакций)
    private String transactionName;

    // Время выполнения операции
    private LocalDateTime timestamp;

    // Целевая таблица (если операция связана с таблицей)
    private String tableName;

    // Данные, связанные с операцией (например, значения для INSERT или условие для DELETE)
    private String data;

    // Метод для удобного форматирования записи в строку (например, для записи в файл)
    public String toLogString() {
        return String.format("[%s] %s %s %s %s",
                timestamp,
                operationType,
                transactionName != null ? transactionName : "",
                tableName != null ? tableName : "",
                data != null ? data : "");
    }
}