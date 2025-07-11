package ru.mephi.db.di;

import dagger.Module;
import dagger.Provides;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.infrastructure.Constants;

import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

@Module
public class AppModule {
    private final Path dbPath;

    public AppModule(Path dbPath) {
        this.dbPath = dbPath;
    }

    @Provides
    @Singleton
    public Scanner provideScanner() {
        return new Scanner(System.in);
    }

    @Provides
    @Singleton
    public ConnectionConfig provideConnectionConfig() {
        return new ConnectionConfig(this.dbPath);
    }

    public PrintStream providePrintStream() {
        try {
            return new PrintStream(Constants.TEST_PRINT_STREAM_FILE);
        } catch (FileNotFoundException e) {
            // TODO: handle error
            throw new RuntimeException(e);
        }
    }
}
