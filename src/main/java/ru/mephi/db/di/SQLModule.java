package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import ru.mephi.db.application.core.sql.Impl.QueryExecutorImpl;
import ru.mephi.db.application.core.sql.Impl.SQLParserImpl;
import ru.mephi.db.application.core.sql.Impl.handler.DeleteQueryHandler;
import ru.mephi.db.application.core.sql.Impl.handler.InsertQueryHandler;
import ru.mephi.db.application.core.sql.Impl.handler.SelectQueryHandler;
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
    public static QueryExecutor provideQueryExecutor() {
        return new QueryExecutorImpl(List.of(
                new SelectQueryHandler(),
                new InsertQueryHandler(),
                new DeleteQueryHandler()
        ));
    }

}
