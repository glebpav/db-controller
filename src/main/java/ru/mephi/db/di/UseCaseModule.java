package ru.mephi.db.di;

import dagger.Module;
import dagger.Provides;
import ru.mephi.db.usecase.CreateDatabaseUseCase;
import ru.mephi.db.usecase.ExitDatabaseUseCase;
import ru.mephi.db.usecase.HandleUserInputUseCase;
import ru.mephi.db.usecase.InitializeDatabaseUseCase;

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
    public HandleUserInputUseCase providesHandleUserInputUseCase(Scanner scanner) {
        return new HandleUserInputUseCase(scanner);
    }

    @Provides
    @Singleton
    public ExitDatabaseUseCase providesExitDatabaseUseCase() {
        return new ExitDatabaseUseCase();
    }
}
