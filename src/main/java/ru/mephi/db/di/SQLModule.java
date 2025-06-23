package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
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
    public static QueryExecutor provideQueryExecutor(ConnectionConfig connectionConfig, DataRepository dataRepository) {
        return new QueryExecutorImpl(List.of(
                new CreateTableHandler(dataRepository, connectionConfig),
                new SelectQueryHandler(),
                new InsertQueryHandler(dataRepository, connectionConfig),
                new DeleteQueryHandler(),
                new BeginTransactionHandler(),
                new CommitHandler(),
                new RollbackHandler(),
                new ShowFilesHandler(),
                new DropTableHandler(dataRepository, connectionConfig),
                new ShowTablesHandler()
        ));
    }

}
