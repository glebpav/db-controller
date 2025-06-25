package ru.mephi.db.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mephi.db.exception.DatabaseExitException;
import ru.mephi.db.application.usecase.ExitDatabaseUseCase;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExitDatabaseUseCaseTest {

    @Mock
    private FileLock mockFileLock;

    @Mock
    private FileChannel mockFileChannel;

    private final ExitDatabaseUseCase useCase = new ExitDatabaseUseCase();

    @Test
    public void shouldReleaseLockAndCloseChannelSuccessfully() throws Exception {
        // Arrange
        when(mockFileLock.channel()).thenReturn(mockFileChannel);

        // Act
        useCase.execute(mockFileLock);

        // Assert
        verify(mockFileLock).release();
        verify(mockFileChannel).close();
    }

    @Test
    public void shouldThrowExceptionWhenReleaseFails() throws IOException {
        // Arrange
        doThrow(new IOException("Lock release failed")).when(mockFileLock).release();

        // Act & Assert
        assertThrows(DatabaseExitException.class, () -> useCase.execute(mockFileLock));
        verify(mockFileLock).release();
    }

    @Test
    public void shouldThrowExceptionWhenChannelCloseFails() throws IOException {
        // Arrange
        when(mockFileLock.channel()).thenReturn(mockFileChannel);
        doThrow(new IOException("Channel close failed")).when(mockFileChannel).close();

        // Act & Assert
        assertThrows(DatabaseExitException.class, () -> useCase.execute(mockFileLock));
    }

    @Test
    public void shouldWrapIOExceptionWithProperMessage() throws IOException {
        // Arrange
        String errorMessage = "Test IO error";
        doThrow(new IOException(errorMessage)).when(mockFileLock).release();

        // Act & Assert
        DatabaseExitException e = assertThrows(DatabaseExitException.class,
                () -> useCase.execute(mockFileLock));
        assertNotNull(e);
    }
}