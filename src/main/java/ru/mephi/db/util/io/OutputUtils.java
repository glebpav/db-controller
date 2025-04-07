package ru.mephi.db.util.io;

import lombok.Getter;
import lombok.Setter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class OutputUtils {

    @Getter
    public enum LogLevel {
        SUCCESS(Ansi.Color.GREEN, "SUCCESS"),
        ERROR(Ansi.Color.RED, "ERROR"),
        WARN(Ansi.Color.YELLOW, "WARN"),
        INFO(Ansi.Color.CYAN, "INFO"),
        DEBUG(Ansi.Color.MAGENTA, "DEBUG"),
        VERBOSE(Ansi.Color.BLUE, "VERBOSE");


        private final Ansi.Color color;
        private final String tag;
        private static final int maxLength;

        static {
            int max = 0;
            for (LogLevel level : values()) {
                max = Math.max(max, level.name().length());
            }
            maxLength = max;
        }

        LogLevel(Ansi.Color color, String tag) {
            this.color = color;
            this.tag = tag;
        }

        public String paddedName() {
            return String.format("%-" + maxLength + "s", tag);
        }
    }

    private static boolean showTimestamp = false;
    private static boolean ansiEnabled;
    @Setter
    static LogLevel minimumLogLevel = LogLevel.INFO;

    @Setter
    private static PrintStream out = System.out;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        try {
            AnsiConsole.systemInstall();
            AnsiConsole.out().print(Ansi.ansi());
            ansiEnabled = true;
        } catch (Exception e) {
            ansiEnabled = false;
            out.println("ANSI colors not supported, falling back to plain text");
        }
    }

    public static void showTimestamps(boolean show) {
        showTimestamp = show;
    }

    public static void enableAnsiColors(boolean enable) {
        ansiEnabled = enable;
    }

    private static void print(String message, LogLevel level) {
        if (level.ordinal() > minimumLogLevel.ordinal()) {
            return;
        }

        String timestamp = showTimestamp ? "[" + LocalDateTime.now().format(TIME_FORMATTER) + "] " : "";

        if (ansiEnabled) {
            Ansi ansi = Ansi.ansi()
                    .fg(level.getColor())
                    .a(timestamp)
                    .a(level.paddedName())
                    .a(" ")
                    .a(message)
                    .reset();
            out.println(ansi);
        } else {
            out.println(timestamp + level.paddedName() + " " + message);
        }
    }

    public static void success(String message) {
        print(message, LogLevel.SUCCESS);
    }

    public static void error(String message) {
        print(message, LogLevel.ERROR);
    }

    public static void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace(out);
    }

    public static void warning(String message) {
        print(message, LogLevel.WARN);
    }

    public static void info(String message) {
        print(message, LogLevel.INFO);
    }

    public static void debug(String message) {
        print(message, LogLevel.DEBUG);
    }

    public static void verbose(String message) {
        print(message, LogLevel.VERBOSE);
    }

    public static void success(String format, Object... args) {
        success(String.format(format, args));
    }

    public static void error(String format, Object... args) {
        if (args.length > 0 && args[args.length - 1] instanceof Throwable throwable) {
            Object[] messageArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, messageArgs, 0, args.length - 1);
            error(String.format(format, messageArgs), throwable);
        } else {
            error(String.format(format, args));
        }
    }

    public static void warning(String format, Object... args) {
        warning(String.format(format, args));
    }

    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public static void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    public static void verbose(String format, Object... args) {
        verbose(String.format(format, args));
    }

    public static void main(String[] args) {
        enableAnsiColors(true);
        info("ANSI colors enabled test");
        enableAnsiColors(false);
        info("ANSI colors disabled test");
        enableAnsiColors(true);

        showTimestamps(true);
        setMinimumLogLevel(LogLevel.VERBOSE);

        info("Application started");
        warning("Low memory: %dMB remaining", 512);
        error("File not found: %s", "data.txt", new FileNotFoundException());
        success("Operation completed successfully");
        debug("Debug message");
        verbose("Verbose diagnostic information");

        setMinimumLogLevel(LogLevel.WARN);
        verbose("This verbose message won't appear");
        warning("This warning will appear");
    }
}
