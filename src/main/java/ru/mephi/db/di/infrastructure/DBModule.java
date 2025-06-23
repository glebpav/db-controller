package ru.mephi.db.di.infrastructure;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.adapter.db.LogRepository;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;
import ru.mephi.db.infrastructure.db.LogRepositoryImpl;

@Module
public abstract class DBModule {

    @Provides
    public static DataRepositoryImpl provideDataRepositoryImpl(){
        return new DataRepositoryImpl();
    }

    @Binds
    public abstract DataRepository bindDataRepository(DataRepositoryImpl implementation);


    @Binds
    public abstract LogRepository logRepository(LogRepositoryImpl implementation);

}
