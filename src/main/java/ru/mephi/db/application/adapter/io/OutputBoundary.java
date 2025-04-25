package ru.mephi.db.application.adapter.io;

import lombok.Getter;
import org.fusesource.jansi.Ansi;

public interface OutputBoundary {
    @Getter
    enum LogLevel {
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

    // void send(String message, LogLevel level);

    void success(String message);

    void error(String message);

    void error(String message, Throwable throwable);

    void warning(String message);

    void info(String message);

    void debug(String message);

    void verbose(String message);
}
