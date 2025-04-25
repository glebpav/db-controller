package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import ru.mephi.db.application.adapter.io.InputBoundary;
import ru.mephi.db.application.adapter.io.OutputBoundary;
import ru.mephi.db.infrastructure.cli.CliInputBoundaryImpl;
import ru.mephi.db.infrastructure.cli.CliOutputBoundaryImpl;

@Module
public abstract class BoundaryModule {

    @Binds
    public abstract InputBoundary bindInputBoundary(
            CliInputBoundaryImpl implementation
    );

    @Binds
    public abstract OutputBoundary bindOutputBoundary(
            CliOutputBoundaryImpl implementation
    );

}
