package ru.mephi.db.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import org.jetbrains.annotations.NotNull;
import ru.mephi.db.application.core.command.*;
import ru.mephi.db.application.core.command.impl.*;
import ru.mephi.db.application.core.command.impl.handler.EmptyCommandHandler;
import ru.mephi.db.application.core.command.impl.handler.ExitCommandHandler;
import ru.mephi.db.application.core.command.impl.handler.HelpCommandHandler;
import ru.mephi.db.application.core.command.impl.handler.SQLQueryCommandHandler;
import ru.mephi.db.di.qulifier.CommandPriority;
import ru.mephi.db.domain.valueobject.Priority;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    private static @NotNull Priority getPriority(CommandHandler handler) {
        CommandPriority annotation = handler.getClass().getAnnotation(CommandPriority.class);
        return annotation != null ? annotation.value() : Priority.LOWEST;
    }

    @Provides
    @Singleton
    static CommandDispatcher provideCommandDispatcher(Set<CommandHandler> commandHandlers) {
        List<CommandHandler> sortedHandlers = new ArrayList<>(commandHandlers);
        sortedHandlers.sort(Comparator.comparingInt(handler -> getPriority(handler).getValue()));
        return new CommandDispatcherImpl(sortedHandlers);
    }
}
