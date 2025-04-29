package integration.di;
import dagger.Component;

import ru.mephi.db.application.usecase.ExitDatabaseUseCase;
import ru.mephi.db.application.usecase.HandleUserInputUseCase;
import ru.mephi.db.application.usecase.InitializeDatabaseUseCase;
import ru.mephi.db.di.*;
import ru.mephi.db.di.infrastructure.*;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        TestModule.class,
        AppModule.class,
        UseCaseModule.class,
        CommandModule.class,
        SQLModule.class,
        DBModule.class,
})
public interface TestComponent {
    InitializeDatabaseUseCase getInitializeDatabaseUseCase();
    HandleUserInputUseCase getHandleUserInputUseCase();
    ExitDatabaseUseCase getExitDatabaseUseCase();

    TestModule getTestModule();
}