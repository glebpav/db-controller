package ru.mephi.db.application.adapter.db;

import ru.mephi.db.exception.DatabaseException;

public interface LogRepository {
    /**
     * Начинает логгирование новой транзакции с переданным идентификатором.
     * Можно записать начальную метку в файл лога.
     */
    void startTransaction(String transactionId) throws DatabaseException;

    /**
     * Записывает в лог факт изменения или другую операцию над страницей.
     *
     * @param transactionId идентификатор текущей транзакции
     * @param pageId        условный идентификатор страницы (8 KB)
     * @param beforeData    данные до изменения (для возможного отката)
     * @param afterData     данные после изменения
     */
    void writeChange(String transactionId, String pageId, String beforeData, String afterData) throws Exception;

    /**
     * Фиксирует транзакцию в логе (commit).
     * То есть записывает «commit record» в файл лога.
     */
    void commitTransaction(String transactionId) throws Exception;

//    транзакция 1
//    TID=..., PAGE=..., BEFORE=..., AFTER=...
//    КОММИТ
//    транзакция 1
//    TID=..., PAGE=..., BEFORE=..., AFTER=...
//    КОММИТ
//    транзакция 1
//    TID=..., PAGE=..., BEFORE=..., AFTER=...
//    КОММИТ

    /**
     * Записывает в лог метку об откате транзакции (rollback).
     */
    void rollbackTransaction(String transactionId) throws Exception;
}