package ru.mephi.db.infrastructure.cli;

import lombok.Setter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import ru.mephi.db.application.adapter.io.OutputBoundary;

import javax.inject.Inject;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CliOutputBoundaryImpl implements OutputBoundary {

    private boolean showTimestamp = false;
    private boolean ansiEnabled;
    @Setter
    LogLevel minimumLogLevel = LogLevel.INFO;

    private final PrintStream out;
    private final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Inject
    public CliOutputBoundaryImpl() {
        this.out = System.out;
        initAnsi();
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

    protected void send(String message, LogLevel level) {
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
            out.print(ansi);
        } else {
            out.print(timestamp + level.paddedName() + " " + message);
        }
    }

    @Override
    public void success(String message) {
        send(message, LogLevel.SUCCESS);
    }

    @Override
    public void error(String message) {
        send(message, LogLevel.ERROR);
    }

    @Override
    public void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace(out);
    }

    @Override
    public void warning(String message) {
        send(message, LogLevel.WARN);
    }

    @Override
    public void info(String message) {
        send(message, LogLevel.INFO);
    }

    @Override
    public void debug(String message) {
        send(message, LogLevel.DEBUG);
    }

    @Override
    public void verbose(String message) {
        send(message, LogLevel.VERBOSE);
    }
}
