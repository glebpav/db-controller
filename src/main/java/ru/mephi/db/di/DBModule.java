package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;

@Module
public abstract class DBModule {

    @Binds
    public abstract DataRepository bindDataRepository(DataRepositoryImpl implementation);
} 