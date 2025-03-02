package ru.mephi.db.infrastructure.di;

import dagger.Component;
import ru.mephi.db.adapter.cli.CliInputHandler;
import ru.mephi.db.core.usecase.CreateTableUseCase;

@Component(modules = DatabaseModule.class)
public interface AppComponent {
    CreateTableUseCase createTableUseCase();
    CliInputHandler cliInputHandler();
}