package ru.mephi.db.application.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.List;

import ru.mephi.db.application.adapter.db.DataRepository;

public class TransactionManager {
    private boolean inTransaction = false;
    private final ConnectionConfig connectionconfig;
    private final DataRepository dataRepository;
    

    public TransactionManager(ConnectionConfig connectionconfig, DataRepository dataRepository) {
        this.connectionconfig = connectionconfig;
        this.dataRepository = dataRepository;
    }

    public boolean isInTransaction() {
        return inTransaction;
    }

    public void begin() {
        inTransaction = true;
    }

    public void commit() throws IOException {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction to commit");
        }
        
        processTemporaryTables(true); // true = commit mode
        inTransaction = false;
    }

    public void rollback() throws IOException {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction to rollback");
        }
        
        processTemporaryTables(false); // false = rollback mode
        inTransaction = false;
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
} 