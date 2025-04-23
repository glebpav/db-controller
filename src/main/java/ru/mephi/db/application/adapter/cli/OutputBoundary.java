package ru.mephi.db.application.adapter.cli;

import lombok.Getter;
import org.fusesource.jansi.Ansi;
import ru.mephi.db.bin.util.io.OutputUtils;

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

    void send(String message, LogLevel level);
}
