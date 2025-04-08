package ru.mephi.db.util.io;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class OutputUtils implements AutoCloseable {

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

    private boolean showTimestamp = false;
    private boolean ansiEnabled;
    @Setter
    LogLevel minimumLogLevel = LogLevel.INFO;

    private final PrintStream out;
    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public OutputUtils(@NonNull PrintStream out) {
        this.out = out;
        initAnsi();
    }

    public OutputUtils() {
        this(System.out);
    }

    private void initAnsi() {
        try {
            AnsiConsole.systemInstall();
            AnsiConsole.out().print(Ansi.ansi());
            ansiEnabled = true;
        } catch (Exception e) {
            ansiEnabled = false;
            out.println("ANSI colors not supported, falling back to plain text");
        }
    }

    public void showTimestamps(boolean show) {
        showTimestamp = show;
    }

    public void enableAnsiColors(boolean enable) {
        ansiEnabled = enable;
    }

    private void print(String message, LogLevel level) {
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

    public void success(String message) {
        print(message, LogLevel.SUCCESS);
    }

    public void error(String message) {
        print(message, LogLevel.ERROR);
    }

    public void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace(out);
    }

    public void warning(String message) {
        print(message, LogLevel.WARN);
    }

    public void info(String message) {
        print(message, LogLevel.INFO);
    }

    public void debug(String message) {
        print(message, LogLevel.DEBUG);
    }

    public void verbose(String message) {
        print(message, LogLevel.VERBOSE);
    }

    public void success(String format, Object... args) {
        success(String.format(format, args));
    }

    public void error(String format, Object... args) {
        if (args.length > 0 && args[args.length - 1] instanceof Throwable throwable) {
            Object[] messageArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, messageArgs, 0, args.length - 1);
            error(String.format(format, messageArgs), throwable);
        } else {
            error(String.format(format, args));
        }
    }

    public void warning(String format, Object... args) {
        warning(String.format(format, args));
    }

    public void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    public void verbose(String format, Object... args) {
        verbose(String.format(format, args));
    }

    @Override
    public void close() throws Exception {
        out.close();
    }

    public static void main(String[] args) {

        OutputUtils out = new OutputUtils(System.out);

        out.enableAnsiColors(true);
        out.info("ANSI colors enabled test");
        out.enableAnsiColors(false);
        out.info("ANSI colors disabled test");
        out.enableAnsiColors(true);

        out.showTimestamps(true);
        out.setMinimumLogLevel(LogLevel.VERBOSE);

        out.info("Application started");
        out.warning("Low memory: %dMB remaining", 512);
        out.error("File not found: %s", "data.txt", new FileNotFoundException());
        out.success("Operation completed successfully");
        out.debug("Debug message");
        out.verbose("Verbose diagnostic information");

        out.setMinimumLogLevel(LogLevel.WARN);
        out.verbose("This verbose message won't appear");
        out.warning("This warning will appear");
    }
}
