package ru.mephi.db.infrastructure.di;

import dagger.Module;
import dagger.Provides;
import ru.mephi.db.adapter.repository.DatabaseRepository;
import ru.mephi.db.adapter.repository.FileDatabaseRepository;
import ru.mephi.db.core.usecase.CreateTableUseCase;

@Module
public class DatabaseModule {

    @Provides
    public DatabaseRepository provideDatabaseRepository() {
        return new FileDatabaseRepository();
    }

    @Provides
    public CreateTableUseCase provideCreateTableUseCase(DatabaseRepository repository) {
        return new CreateTableUseCase(repository);
    }

}
