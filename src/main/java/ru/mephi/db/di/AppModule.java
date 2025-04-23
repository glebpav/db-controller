package ru.mephi.db.di;

import dagger.Module;
import dagger.Provides;
import ru.mephi.db.infrastructure.Constants;
import ru.mephi.db.application.core.sql.QueryExecutor;
import ru.mephi.db.application.core.sql.Impl.QueryExecutorImpl;
import ru.mephi.db.application.core.sql.Impl.handler.DeleteQueryHandler;
import ru.mephi.db.application.core.sql.Impl.handler.InsertQueryHandler;
import ru.mephi.db.application.core.sql.Impl.handler.SelectQueryHandler;
import ru.mephi.db.application.core.sql.SQLParser;
import ru.mephi.db.application.core.sql.Impl.SQLParserImpl;
import ru.mephi.db.di.qulifier.CustomOutput;
import ru.mephi.db.bin.util.io.OutputUtils;

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

    public PrintStream providePrintStream() {
        try {
            return new PrintStream(Constants.TEST_PRINT_STREAM_FILE);
        } catch (FileNotFoundException e) {
            // TODO: handle error
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    public OutputUtils provideDefaultOutputUtils() {
        return new OutputUtils();
    }

    @Provides
    @Singleton
    @CustomOutput
    public OutputUtils provideCustomOutputUtils(PrintStream printStream) {
        return new OutputUtils(printStream);
    }

    @Provides
    @Singleton
    public SQLParser provideSQLParser() {
        return new SQLParserImpl();
    }

    @Provides
    @Singleton
    public QueryExecutor provideQueryExecutor() {
        return new QueryExecutorImpl(List.of(
                new SelectQueryHandler(),
                new InsertQueryHandler(),
                new DeleteQueryHandler()
        ));
    }
}
