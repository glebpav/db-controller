package ru.mephi.db.util;

import java.util.Set;

public class Constants {
    public static final String MAGIC_HEADER = "# Database Configuration File";

    public static final String DB_INFO_FILE = "info";
    public static final String DB_LOCK_FILE = ".lock";

    public static final String TEST_PRINT_STREAM_FILE = "testPrintStreamFile.txt";

    public static final Set<String> EXIT_COMMANDS = Set.of("exit", "quit", ":q");
    public static final Set<String> HELP_COMMANDS = Set.of("help", ":h");

    public static final String EXIT_MESSAGE = "Goodbye, dear!";
    public static final String HELP_MESSAGE = "There is no help =(";
}
