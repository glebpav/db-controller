package ru.mephi.db.application.adapter.db;

import ru.mephi.db.domain.entity.TransactionLogEntry;
import ru.mephi.db.exception.LogUnableWriteTransactionException;

import java.io.IOException;
import java.util.List;

public interface TransactionLogger {
    
    /**
     * Записывает запись в журнал транзакций
     */
    void logEntry(TransactionLogEntry entry) throws LogUnableWriteTransactionException;
    
    /**
     * Записывает начало транзакции
     */
    void logBeginTransaction(String transactionId, String transactionName) throws LogUnableWriteTransactionException;
    
    /**
     * Записывает коммит транзакции
     */
    void logCommitTransaction(String transactionId) throws LogUnableWriteTransactionException;
    
    /**
     * Записывает откат транзакции
     */
    void logRollbackTransaction(String transactionId) throws LogUnableWriteTransactionException;
    
    /**
     * Записывает создание таблицы
     */
    void logCreateTable(String transactionId, String tableName, List<String> schema) throws LogUnableWriteTransactionException;
    
    /**
     * Записывает удаление таблицы
     */
    void logDropTable(String transactionId, String tableName) throws LogUnableWriteTransactionException;
    
    /**
     * Записывает вставку записи
     */
    void logInsertRecord(String transactionId, String tableName, List<Object> values) throws LogUnableWriteTransactionException;
    
    /**
     * Записывает удаление записи
     */
    void logDeleteRecord(String transactionId, String tableName, int recordIndex) throws LogUnableWriteTransactionException;
    
    /**
     * Восстанавливает состояние из журнала
     */
    void recover() throws IOException;
    
    /**
     * Получает все незавершенные транзакции
     */
    List<String> getUnfinishedTransactions() throws IOException;
    
    /**
     * Очищает журнал до контрольной точки
     */
    void truncateLog() throws IOException;
} 