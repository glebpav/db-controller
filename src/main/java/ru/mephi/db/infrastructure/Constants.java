package ru.mephi.db.infrastructure;

import java.util.Set;

public class Constants {
    public static final String MAGIC_HEADER = "# Database Configuration File";

    public static final String DB_INFO_FILE = "info";
    public static final String DB_LOCK_FILE = ".lock";
    public static final String DB_LOG_FILE = ".log";

    public static final String TEST_PRINT_STREAM_FILE = "testPrintStreamFile.txt";

    public static final Set<String> EXIT_COMMANDS = Set.of("exit", "quit", ":q");
    public static final Set<String> HELP_COMMANDS = Set.of("help", ":h");

    public static final String EXIT_MESSAGE = "Goodbye, dear!";
    public static final String HELP_MESSAGE = "There is no help =(";

    // ANSI Color Codes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001B[1m";
}
