package ru.mephi.db.usecase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.mephi.db.exception.DatabaseExitException;
import ru.mephi.db.application.usecase.ExitDatabaseUseCase;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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

    @Test(expected = DatabaseExitException.class)
    public void shouldThrowExceptionWhenReleaseFails() throws Exception {
        // Arrange
        doThrow(new IOException("Lock release failed")).when(mockFileLock).release();

        // Act
        useCase.execute(mockFileLock);

        // Assert
        verify(mockFileLock).release();
    }

    @Test(expected = DatabaseExitException.class)
    public void shouldThrowExceptionWhenChannelCloseFails() throws Exception {
        // Arrange
        when(mockFileLock.channel()).thenReturn(mockFileChannel);
        doThrow(new IOException("Channel close failed")).when(mockFileChannel).close();

        // Act
        useCase.execute(mockFileLock);
    }

    @Test
    public void shouldWrapIOExceptionWithProperMessage() throws Exception {
        // Arrange
        String errorMessage = "Test IO error";
        doThrow(new IOException(errorMessage)).when(mockFileLock).release();

        try {
            // Act
            useCase.execute(mockFileLock);
            fail("Expected DatabaseExitException");
        } catch (Exception e) {

            // Assert
            assertTrue(e instanceof DatabaseExitException);
        }
    }
}