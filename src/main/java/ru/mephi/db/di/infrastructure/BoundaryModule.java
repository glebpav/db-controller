package ru.mephi.db.di.infrastructure;

import dagger.Binds;
import dagger.Module;
import ru.mephi.db.application.adapter.io.InputBoundary;
import ru.mephi.db.application.adapter.io.OutputBoundary;
import ru.mephi.db.infrastructure.cli.CliInputBoundaryImpl;
import ru.mephi.db.infrastructure.cli.CliOutputBoundaryImpl;

import javax.inject.Singleton;

@Module
public abstract class BoundaryModule {

    @Binds
    @Singleton
    public abstract InputBoundary bindInputBoundary(
            CliInputBoundaryImpl implementation
    );

    @Binds
    @Singleton
    public abstract OutputBoundary bindOutputBoundary(
            CliOutputBoundaryImpl implementation
    );

}
