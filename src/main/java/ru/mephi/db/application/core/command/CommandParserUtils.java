package ru.mephi.db.application.core.command;

import java.util.Set;

public class CommandParserUtils {

    static boolean checkInSet(String input, Set<String> commandsSet) {
        return commandsSet.stream().anyMatch(command -> command.equalsIgnoreCase(input));
    }

}
