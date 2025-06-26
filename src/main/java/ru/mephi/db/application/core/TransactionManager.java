package ru.mephi.db.application.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.adapter.db.TransactionLogger;
import ru.mephi.db.exception.LogUnableWriteTransactionException;

public class TransactionManager {
    private boolean inTransaction = false;
    private String currentTransactionId = null;
    private final ConnectionConfig connectionconfig;
    private final DataRepository dataRepository;
    private final TransactionLogger transactionLogger;
    

    public TransactionManager(ConnectionConfig connectionconfig, DataRepository dataRepository, TransactionLogger transactionLogger) {
        this.connectionconfig = connectionconfig;
        this.dataRepository = dataRepository;
        this.transactionLogger = transactionLogger;
    }

    public boolean isInTransaction() {
        return inTransaction;
    }

    public String getCurrentTransactionId() {
        return currentTransactionId;
    }

    public void begin() throws LogUnableWriteTransactionException {
        if (inTransaction) {
            throw new IllegalStateException("Transaction already in progress");
        }
        
        currentTransactionId = UUID.randomUUID().toString();
        inTransaction = true;
        
        // Логируем начало транзакции
        transactionLogger.logBeginTransaction(currentTransactionId, null);
    }

    public void commit() throws IOException {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction to commit");
        }
        
        try {
            processTemporaryTables(true); // true = commit mode
            
            // Логируем коммит транзакции
            transactionLogger.logCommitTransaction(currentTransactionId);
            
            inTransaction = false;
            currentTransactionId = null;
        } catch (LogUnableWriteTransactionException e) {
            // Если не удалось записать в лог, откатываем транзакцию
            rollback();
            throw new IOException("Failed to commit transaction: " + e.getMessage(), e);
        }
    }

    public void rollback() throws IOException {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction to rollback");
        }
        
        try {
            processTemporaryTables(false); // false = rollback mode
            
            // Логируем откат транзакции
            transactionLogger.logRollbackTransaction(currentTransactionId);
            
            inTransaction = false;
            currentTransactionId = null;
        } catch (LogUnableWriteTransactionException e) {
            // Даже если не удалось записать в лог, все равно откатываем транзакцию
            inTransaction = false;
            currentTransactionId = null;
            throw new IOException("Failed to rollback transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Создаёт временную копию таблицы (например, users.txt -> users_tmp.txt)
     */
    public void createTempTableCopy(String tableName) throws IOException {
        Path originalPath = connectionconfig.getTablePath(tableName);
        Path tempPath = getTempTablePath(tableName);
        Files.copy(originalPath, tempPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Возвращает путь к временной таблице, если транзакция активна и копия существует, иначе к основной
     */
    public Path getActualTablePath(String tableName) {
        if (isInTransaction()) {
            Path tempPath = getTempTablePath(tableName);
            if (Files.exists(tempPath)) {
                return tempPath;
            }
        }
        return connectionconfig.getTablePath(tableName);
    }

    /**
     * Возвращает путь к временной таблице (например, users.txt -> users_tmp.txt)
     */
    public Path getTempTablePath(String tableName) {
        String fileName = tableName;
        if (!fileName.endsWith(".txt")) {
            fileName += ".txt";
        }
        int dotIndex = fileName.lastIndexOf('.');
        String tempFileName = (dotIndex > 0)
                ? fileName.substring(0, dotIndex) + "_tmp" + fileName.substring(dotIndex)
                : fileName + "_tmp";
        return connectionconfig.getDbPath().resolve(tempFileName);
    }

    /**
     * Обрабатывает все временные таблицы в Master.txt
     * @param isCommit true для коммита, false для отката
     */
    private void processTemporaryTables(boolean isCommit) throws IOException {
        Path masterPath = connectionconfig.getMasterPath().toAbsolutePath();
        List<String> allTableNames = dataRepository.getAllTableNames(masterPath.toString());
        
        for (String tableName : allTableNames) {
            if (tableName.contains("_tmp")) {
                Path tempPath = connectionconfig.getTablePath(tableName).toAbsolutePath();
                
                if (isCommit) {
                    processTemporaryTableForCommit(tableName, tempPath, masterPath);
                } else {
                    processTemporaryTableForRollback(tempPath, masterPath);
                }
            }
        }
    }

    /**
     * Обрабатывает временную таблицу для коммита
     */
    private void processTemporaryTableForCommit(String tableName, Path tempPath, Path masterPath) throws IOException {
        String mainTableName = tableName.replace("_tmp", "");
        Path mainTablePath = connectionconfig.getTablePath(mainTableName).toAbsolutePath();

        boolean mainExistsInMaster = dataRepository.isTableExists(masterPath.toString(), mainTablePath.toString());
    
        if (Files.exists(tempPath)) {
            Files.copy(tempPath, mainTablePath, StandardCopyOption.REPLACE_EXISTING);
            
            if(!mainExistsInMaster) {
                dataRepository.addTableReference(masterPath.toString(), mainTablePath.toString());
            }
            
            dataRepository.deleteTableFile(tempPath.toString());
        } else {
            if (mainExistsInMaster) {
                dataRepository.deleteTableFile(mainTablePath.toString());
            }
            dataRepository.removeTableReference(masterPath.toString(), tempPath.toString());
        }
    }

    /**
     * Обрабатывает временную таблицу для отката
     */
    private void processTemporaryTableForRollback(Path tempPath, Path masterPath) throws IOException {
        if (Files.exists(tempPath)) {
            dataRepository.deleteTableFile(tempPath.toString());
        } else {
            dataRepository.removeTableReference(masterPath.toString(), tempPath.toString());
        }
    }

    /**
     * Логирует создание таблицы
     */
    public void logCreateTable(String tableName, List<String> schema) throws LogUnableWriteTransactionException {
        if (inTransaction && currentTransactionId != null) {
            transactionLogger.logCreateTable(currentTransactionId, tableName, schema);
        }
    }

    /**
     * Логирует удаление таблицы
     */
    public void logDropTable(String tableName) throws LogUnableWriteTransactionException {
        if (inTransaction && currentTransactionId != null) {
            transactionLogger.logDropTable(currentTransactionId, tableName);
        }
    }

    /**
     * Логирует вставку записи
     */
    public void logInsertRecord(String tableName, List<Object> values) throws LogUnableWriteTransactionException {
        if (inTransaction && currentTransactionId != null) {
            transactionLogger.logInsertRecord(currentTransactionId, tableName, values);
        }
    }

    /**
     * Логирует удаление записи
     */
    public void logDeleteRecord(String tableName, int recordIndex) throws LogUnableWriteTransactionException {
        if (inTransaction && currentTransactionId != null) {
            transactionLogger.logDeleteRecord(currentTransactionId, tableName, recordIndex);
        }
    }

    /**
     * Восстанавливает состояние базы данных из WAL
     * Применяет все операции из завершенных транзакций
     * Откатывает незавершенные транзакции
     */
    public void recoverFromWAL() throws IOException {
        System.out.println("Starting database recovery from WAL...");
        
        try {
            // Получаем список незавершенных транзакций
            List<String> unfinishedTransactions = transactionLogger.getUnfinishedTransactions();
            
            if (unfinishedTransactions.isEmpty()) {
                System.out.println("No unfinished transactions found. Recovery not needed.");
                return;
            }
            
            System.out.println("Found " + unfinishedTransactions.size() + " unfinished transactions: " + unfinishedTransactions);
            
            // Откатываем все незавершенные транзакции
            for (String transactionId : unfinishedTransactions) {
                System.out.println("Rolling back transaction: " + transactionId);
                rollbackUnfinishedTransaction(transactionId);
            }
            
            System.out.println("Recovery completed successfully.");
            
        } catch (Exception e) {
            e.printStackTrace(); // Для диагностики
            System.err.println("Recovery failed: " + e.getMessage());
            throw new IOException("Failed to recover from WAL: " + e.getMessage(), e);
        }
    }
    
    /**
     * Откатывает конкретную незавершенную транзакцию
     */
    private void rollbackUnfinishedTransaction(String transactionId) throws IOException {
        // Удаляем все временные файлы, связанные с этой транзакцией
        Path masterPath = connectionconfig.getMasterPath().toAbsolutePath();
        List<String> allTableNames = dataRepository.getAllTableNames(masterPath.toString());
        
        for (String tableName : allTableNames) {
            if (tableName.contains("_tmp")) {
                Path tempPath = connectionconfig.getTablePath(tableName).toAbsolutePath();
                if (Files.exists(tempPath)) {
                    System.out.println("Removing temporary table: " + tableName);
                    dataRepository.deleteTableFile(tempPath.toString());
                }
            }
        }
        
        // Удаляем ссылки на временные таблицы из Master.txt
        removeAllTempTableReferences(masterPath.toString());
    }
    
    /**
     * Удаляет все ссылки на временные таблицы из Master.txt
     */
    private void removeAllTempTableReferences(String masterPath) throws IOException {
        List<String> allTableNames = dataRepository.getAllTableNames(masterPath);
        List<String> tempTableNames = allTableNames.stream()
                .filter(name -> name.contains("_tmp"))
                .toList();
        
        for (String tempTableName : tempTableNames) {
            Path tempPath = connectionconfig.getTablePath(tempTableName);
            dataRepository.removeTableReference(masterPath, tempPath.toString());
        }
    }
} 