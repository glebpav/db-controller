package ru.mephi.db.infrastructure.cil;

import lombok.NonNull;
import lombok.Setter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import ru.mephi.db.application.adapter.cli.OutputBoundary;

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

    @Override
    public void send(String message, LogLevel level) {
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

}
