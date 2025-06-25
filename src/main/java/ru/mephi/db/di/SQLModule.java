package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.TransactionManager;
import ru.mephi.db.application.core.sql.Impl.QueryExecutorImpl;
import ru.mephi.db.application.core.sql.Impl.SQLParserImpl;
import ru.mephi.db.application.core.sql.Impl.handler.*;
import ru.mephi.db.application.core.sql.QueryExecutor;
import ru.mephi.db.application.core.sql.SQLParser;


import javax.inject.Singleton;
import java.util.List;

@Module
public abstract class SQLModule {

    @Binds
    public abstract SQLParser bindSQLParser(SQLParserImpl implementation);

    @Provides
    @Singleton
    public static QueryExecutor provideQueryExecutor(ConnectionConfig connectionConfig, DataRepository dataRepository, TransactionManager transactionManager) {
        return new QueryExecutorImpl(List.of(
                new CreateTableHandler(dataRepository, connectionConfig, transactionManager),
                new SelectQueryHandler(dataRepository, connectionConfig, transactionManager),
                new InsertQueryHandler(dataRepository, connectionConfig, transactionManager),
                new DeleteQueryHandler(dataRepository, connectionConfig, transactionManager),
                new BeginTransactionHandler(transactionManager, dataRepository, connectionConfig),
                new CommitHandler(transactionManager, dataRepository, connectionConfig),
                new RollbackHandler(transactionManager, dataRepository, connectionConfig),
                new ShowFilesHandler(),
                new DropTableHandler(dataRepository, connectionConfig, transactionManager),
                new ShowTablesHandler(dataRepository, connectionConfig)
        ));
    }

    @Provides
    @Singleton
    public static TransactionManager provideTransactionManager(ConnectionConfig connectionConfig) {
        return new TransactionManager(connectionConfig);
    }

}
