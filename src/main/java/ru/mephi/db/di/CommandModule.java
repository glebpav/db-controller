package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import ru.mephi.db.application.core.command.*;
import ru.mephi.db.application.core.command.impl.*;
import ru.mephi.db.application.core.command.impl.handler.EmptyCommandHandler;
import ru.mephi.db.application.core.command.impl.handler.ExitCommandHandler;
import ru.mephi.db.application.core.command.impl.handler.HelpCommandHandler;
import ru.mephi.db.application.core.command.impl.handler.SQLQueryCommandHandler;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Set;

@Module
public abstract class CommandModule {

    @Binds
    @IntoSet
    abstract CommandHandler bindEmptyCommand(EmptyCommandHandler cmd);

    @Binds
    @IntoSet
    abstract CommandHandler bindHelpCommand(HelpCommandHandler cmd);

    @Binds
    @IntoSet
    abstract CommandHandler bindExitCommand(ExitCommandHandler cmd);

    @Binds
    @IntoSet
    abstract CommandHandler bindSqlCommand(SQLQueryCommandHandler cmd);


    @Provides
    @Singleton
    static CommandDispatcher provideCommandDispatcher(Set<CommandHandler> commandHandlers) {
        return new CommandDispatcherImpl(new ArrayList<>(commandHandlers));
    }

}
