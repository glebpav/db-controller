package ru.mephi.db.infrastructure.db;

import ru.mephi.db.application.adapter.db.TransactionLogger;
import ru.mephi.db.domain.entity.TransactionLogEntry;
import ru.mephi.db.domain.valueobject.LogOperationType;
import ru.mephi.db.exception.LogUnableWriteTransactionException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionLoggerImpl implements TransactionLogger {
    
    private final Path logFilePath;
    private BufferedWriter logWriter;
    private final Map<String, String> activeTransactions = new ConcurrentHashMap<>();
    
    public TransactionLoggerImpl(Path dbPath) throws IOException {
        this.logFilePath = dbPath.resolve(".log");

        this.logWriter = Files.newBufferedWriter(
            logFilePath, 
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }
    
    @Override
    public void logEntry(TransactionLogEntry entry) throws LogUnableWriteTransactionException {
        try {
            String logLine = entry.toLogString();
            synchronized (logWriter) {
                logWriter.write(logLine);
                logWriter.newLine();
                logWriter.flush(); // Принудительная запись на диск
            }
        } catch (IOException e) {
            throw new LogUnableWriteTransactionException("Failed to write to transaction log: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void logBeginTransaction(String transactionId, String transactionName) throws LogUnableWriteTransactionException {
        TransactionLogEntry entry = TransactionLogEntry.builder()
                .operationType(LogOperationType.BEGIN)
                .transactionName(transactionName != null ? transactionName : transactionId)
                .timestamp(LocalDateTime.now())
                .data(transactionId)
                .build();
        
        logEntry(entry);
        activeTransactions.put(transactionId, transactionName != null ? transactionName : transactionId);
    }
    
    @Override
    public void logCommitTransaction(String transactionId) throws LogUnableWriteTransactionException {
        TransactionLogEntry entry = TransactionLogEntry.builder()
                .operationType(LogOperationType.COMMIT)
                .transactionName(activeTransactions.get(transactionId))
                .timestamp(LocalDateTime.now())
                .data(transactionId)
                .build();
        
        logEntry(entry);
        activeTransactions.remove(transactionId);
    }
    
    @Override
    public void logRollbackTransaction(String transactionId)throws LogUnableWriteTransactionException {
        TransactionLogEntry entry = TransactionLogEntry.builder()
                .operationType(LogOperationType.ROLLBACK)
                .transactionName(activeTransactions.get(transactionId))
                .timestamp(LocalDateTime.now())
                .data(transactionId)
                .build();
        
        logEntry(entry);
        activeTransactions.remove(transactionId);
    }
    
    @Override
    public void logCreateTable(String transactionId, String tableName, List<String> schema) throws LogUnableWriteTransactionException {
        TransactionLogEntry entry = TransactionLogEntry.builder()
                .operationType(LogOperationType.CREATE_TABLE)
                .transactionName(activeTransactions.get(transactionId))
                .timestamp(LocalDateTime.now())
                .tableName(tableName)
                .data(String.join(",", schema))
                .build();
        
        logEntry(entry);
    }
    
    @Override
    public void logDropTable(String transactionId, String tableName) throws LogUnableWriteTransactionException {
        TransactionLogEntry entry = TransactionLogEntry.builder()
                .operationType(LogOperationType.DROP_TABLE)
                .transactionName(activeTransactions.get(transactionId))
                .timestamp(LocalDateTime.now())
                .tableName(tableName)
                .build();
        
        logEntry(entry);
    }
    
    @Override
    public void logInsertRecord(String transactionId, String tableName, List<Object> values) throws LogUnableWriteTransactionException {
        TransactionLogEntry entry = TransactionLogEntry.builder()
                .operationType(LogOperationType.INSERT_RECORD)
                .transactionName(activeTransactions.get(transactionId))
                .timestamp(LocalDateTime.now())
                .tableName(tableName)
                .data(values.toString())
                .build();
        
        logEntry(entry);
    }
    
    @Override
    public void logDeleteRecord(String transactionId, String tableName, int recordIndex) throws LogUnableWriteTransactionException {
        TransactionLogEntry entry = TransactionLogEntry.builder()
                .operationType(LogOperationType.DELETE_RECORD)
                .transactionName(activeTransactions.get(transactionId))
                .timestamp(LocalDateTime.now())
                .tableName(tableName)
                .data(String.valueOf(recordIndex))
                .build();
        
        logEntry(entry);
    }
    
    @Override
    public void recover() throws IOException {
        if (!Files.exists(logFilePath)) {
            return;
        }
        
        System.out.println("Starting recovery from WAL...");
        
        try (BufferedReader reader = Files.newBufferedReader(logFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                TransactionLogEntry entry = parseLogEntry(line);
                if (entry != null) {
                    processLogEntry(entry);
                }
            }
        }
        
        System.out.println("Recovery completed. Active transactions: " + activeTransactions.keySet());
    }
    
    @Override
    public List<String> getUnfinishedTransactions() throws IOException {
        return new ArrayList<>(activeTransactions.keySet());
    }
    
    @Override
    public void truncateLog() throws IOException {
        // Создаем новый файл журнала
        synchronized (logWriter) {
            logWriter.close();
        }
        
        // Переименовываем старый файл
        Path backupPath = logFilePath.resolveSibling("wal.log.backup");
        if (Files.exists(logFilePath)) {
            Files.move(logFilePath, backupPath);
        }
        
        // Создаем новый файл
        this.logWriter = Files.newBufferedWriter(
            logFilePath, 
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }
    
    private TransactionLogEntry parseLogEntry(String line) {
        try {
            // Формат: [timestamp] operation transactionName tableName data
            if (!line.startsWith("[") || !line.contains("]")) {
                return null;
            }
            
            int endBracket = line.indexOf("]");
            String timestampStr = line.substring(1, endBracket);
            String rest = line.substring(endBracket + 1).trim();
            
            String[] parts = rest.split("\\s+", 4);
            if (parts.length < 1) {
                return null;
            }
            
            LogOperationType operationType = LogOperationType.valueOf(parts[0]);
            String transactionName = parts.length > 1 ? parts[1] : null;
            String tableName = parts.length > 2 ? parts[2] : null;
            String data = parts.length > 3 ? parts[3] : null;
            
            return TransactionLogEntry.builder()
                    .operationType(operationType)
                    .transactionName(transactionName)
                    .timestamp(LocalDateTime.parse(timestampStr))
                    .tableName(tableName)
                    .data(data)
                    .build();
        } catch (Exception e) {
            System.err.println("Failed to parse log entry: " + line + " - " + e.getMessage());
            return null;
        }
    }
    
    private void processLogEntry(TransactionLogEntry entry) {
        switch (entry.getOperationType()) {
            case BEGIN:
                activeTransactions.put(entry.getData(), entry.getTransactionName());
                break;
            case COMMIT:
            case ROLLBACK:
                activeTransactions.remove(entry.getData());
                break;
            default:
                break;
        }
    }
    
    public void close() throws IOException {
        if (logWriter != null) {
            logWriter.close();
        }
    }
} 