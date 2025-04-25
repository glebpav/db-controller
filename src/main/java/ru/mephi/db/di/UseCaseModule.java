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

}
