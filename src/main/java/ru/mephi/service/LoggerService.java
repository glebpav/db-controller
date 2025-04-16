package ru.mephi.service;

import ru.mephi.db.model.log.TransactionLogEntry;

import java.util.List;
import java.util.Map;

public interface LoggerService {
    // Запись событий
    void logTransactionStart(String transactionName);
    void logTransactionCommit(String transactionName);
    void logTransactionRollback(String transactionName);
    void logOperation(String operationType, String tableName, Map<String, Object> data);
    void logError(String errorMessage);

    // Чтение и восстановление
    List<TransactionLogEntry> readTransactionLog();
    // void replayLogs(DatabaseService databaseService);

    // Управление файлом журнала
    void createLogFile(String databasePath);
    void clearLogFile();
    void deleteLogFile();
    boolean isLogFileCorrupted();

    // Дополнительные методы
    void setForcedFlush(boolean enabled);
    void archiveLogs(String archivePath);
    long getLogSize();
}
