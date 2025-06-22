package ru.mephi.db.di;

import dagger.Module;
import dagger.Provides;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.infrastructure.Constants;
import ru.mephi.db.application.core.sql.QueryExecutor;
import ru.mephi.db.application.core.sql.Impl.QueryExecutorImpl;
import ru.mephi.db.application.core.sql.Impl.handler.DeleteQueryHandler;
import ru.mephi.db.application.core.sql.Impl.handler.InsertQueryHandler;
import ru.mephi.db.application.core.sql.Impl.handler.SelectQueryHandler;
import ru.mephi.db.application.core.sql.SQLParser;
import ru.mephi.db.application.core.sql.Impl.SQLParserImpl;

import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

@Module
public class AppModule {

    @Provides
    @Singleton
    public Scanner provideScanner() {
        return new Scanner(System.in);
    }

    @Provides
    @Singleton
    public ConnectionConfig provideConnectionConfig() {
        return new ConnectionConfig();
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
