package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.adapter.db.TransactionLogger;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.TransactionManager;
import ru.mephi.db.application.core.sql.impl.QueryExecutorImpl;
import ru.mephi.db.application.core.sql.impl.SQLParserImpl;
import ru.mephi.db.application.core.sql.impl.handler.*;
import ru.mephi.db.application.core.sql.QueryExecutor;
import ru.mephi.db.application.core.sql.SQLParser;
import ru.mephi.db.infrastructure.db.TransactionLoggerImpl;

import javax.inject.Singleton;
import java.util.List;

@Module
public abstract class SQLModule {

    @Binds
    public abstract SQLParser bindSQLParser(SQLParserImpl implementation);

    @Provides
    @Singleton
    public static TransactionLogger provideTransactionLogger(ConnectionConfig connectionConfig) {
        try {
            return new TransactionLoggerImpl(connectionConfig.getDbPath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TransactionLogger: " + e.getMessage(), e);
        }
    }

    @Provides
    @Singleton
    public static QueryExecutor provideQueryExecutor(ConnectionConfig connectionConfig, DataRepository dataRepository, TransactionManager transactionManager) {
        return new QueryExecutorImpl(List.of(
                new CreateTableHandler(dataRepository, transactionManager),
                new SelectQueryHandler(dataRepository, transactionManager),
                new InsertQueryHandler(dataRepository, transactionManager),
                new DeleteQueryHandler(dataRepository, transactionManager),
                new BeginTransactionHandler(transactionManager, dataRepository, connectionConfig),
                new CommitHandler(transactionManager, dataRepository, connectionConfig),
                new RollbackHandler(transactionManager, dataRepository, connectionConfig),
                new ShowFilesHandler(connectionConfig),
                new DropTableHandler(dataRepository, transactionManager),
                new ShowTablesHandler(dataRepository, connectionConfig)
        ));
    }

    @Provides
    @Singleton
    public static TransactionManager provideTransactionManager(ConnectionConfig connectionConfig, DataRepository dataRepository, TransactionLogger transactionLogger) {
        return new TransactionManager(connectionConfig, dataRepository, transactionLogger);
    }

}
