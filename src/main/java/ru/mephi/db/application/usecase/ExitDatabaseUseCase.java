package ru.mephi.db.application.usecase;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.mephi.db.exception.DatabaseExitException;
import ru.mephi.db.exception.DatabaseException;

import java.io.IOException;
import java.nio.channels.FileLock;

@AllArgsConstructor
public class ExitDatabaseUseCase {
    public void execute(@NotNull FileLock lock) throws DatabaseException {
        try {
            lock.release();
            lock.channel().close();
        } catch (IOException e) {
            throw new DatabaseExitException(e);
        }
    }
}