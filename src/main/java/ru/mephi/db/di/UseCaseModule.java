package ru.mephi.db.di;

import dagger.Module;
import dagger.Provides;
import ru.mephi.db.application.usecase.CreateDatabaseUseCase;
import ru.mephi.db.application.usecase.ExitDatabaseUseCase;
import ru.mephi.db.application.usecase.HandleUserInputUseCase;
import ru.mephi.db.application.usecase.InitializeDatabaseUseCase;
import ru.mephi.db.application.core.command.impl.CommandDispatcherImpl;

import javax.inject.Singleton;
import java.util.Scanner;

@Module
public class UseCaseModule {

    @Provides
    @Singleton
    public InitializeDatabaseUseCase providesInitializeDatabaseUseCase(
            Scanner scanner,
            CreateDatabaseUseCase createDatabaseUseCase
    ) {
        return new InitializeDatabaseUseCase(scanner, createDatabaseUseCase);
    }

    @Provides
    @Singleton
    public CreateDatabaseUseCase providesCreateDatabaseUseCase() {
        return new CreateDatabaseUseCase();
    }

    @Provides
    @Singleton
    public HandleUserInputUseCase providesHandleUserInputUseCase(
            Scanner scanner,
            CommandDispatcherImpl commandDispatcher
    ) {
        return new HandleUserInputUseCase(scanner, commandDispatcher);
    }

    @Provides
    @Singleton
    public ExitDatabaseUseCase providesExitDatabaseUseCase() {
        return new ExitDatabaseUseCase();
    }
}
