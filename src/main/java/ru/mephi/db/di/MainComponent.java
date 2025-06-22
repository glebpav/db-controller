package ru.mephi.db.di;

import dagger.Component;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.usecase.ExitDatabaseUseCase;
import ru.mephi.db.application.usecase.HandleUserInputUseCase;
import ru.mephi.db.application.usecase.InitializeDatabaseUseCase;
import ru.mephi.db.di.infrastructure.BoundaryModule;
import ru.mephi.db.di.infrastructure.DBModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        AppModule.class,
        UseCaseModule.class,
        CommandModule.class,
        BoundaryModule.class,
        SQLModule.class,
        DBModule.class,
})
public interface MainComponent {
    InitializeDatabaseUseCase getInitializeDatabaseUseCase();

    HandleUserInputUseCase getHandleUserInputUseCase();

    ExitDatabaseUseCase getExitDatabaseUseCase();

    ConnectionConfig getConnectionConfig();
}