package ru.mephi.db.di;

import dagger.Component;
import ru.mephi.db.usecase.ExitDatabaseUseCase;
import ru.mephi.db.usecase.HandleUserInputUseCase;
import ru.mephi.db.usecase.InitializeDatabaseUseCase;

import javax.inject.Singleton;

@Singleton
@Component(modules = { AppModule.class, UseCaseModule.class, CommandModule.class })
public interface MainComponent {
    InitializeDatabaseUseCase getInitializeDatabaseUseCase();

    HandleUserInputUseCase getHandleUserInputUseCase();

    ExitDatabaseUseCase getExitDatabaseUseCase();
}