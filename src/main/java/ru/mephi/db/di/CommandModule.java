package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import ru.mephi.db.util.command.*;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Set;

@Module
public abstract class CommandModule {

    @Binds
    @IntoSet
    abstract Command bindSqlCommand(SQLQueryCommand cmd);

    @Binds @IntoSet
    abstract Command bindHelpCommand(HelpCommand cmd);

    @Binds
    @IntoSet
    abstract Command bindExitCommand(ExitCommand cmd);

    @Binds
    @IntoSet
    abstract Command bindEmptyCommand(EmptyCommand cmd);

    @Provides
    @Singleton
    static CommandDispatcher provideCommandDispatcher(Set<Command> commands) {
        return new CommandDispatcher(new ArrayList<>(commands));
    }

}
