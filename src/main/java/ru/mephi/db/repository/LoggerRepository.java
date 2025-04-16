package ru.mephi.db.repository;

import java.util.List;

public interface LoggerRepository {
    // Запись новой записи в журнал
    void writeLogEntry(String logEntry);

    // Чтение всех записей из журнала
    List<String> readAllLogs();

    // Очистка журнала
    void clearLogs();

    // Проверка целостности журнала
    boolean isLogFileValid();

    // Архивация старых записей
    void archiveLogs(String archivePath);

    // Получение размера журнала
    long getLogSize();
}
