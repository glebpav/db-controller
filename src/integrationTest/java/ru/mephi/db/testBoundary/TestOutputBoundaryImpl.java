package ru.mephi.db.testBoundary;

import lombok.Getter;
import ru.mephi.db.infrastructure.cli.CliOutputBoundaryImpl;

@Getter
public class TestOutputBoundaryImpl extends CliOutputBoundaryImpl {

    @Getter
    private static final TestOutputBoundaryImpl instance = new TestOutputBoundaryImpl();

    private TestOutputBoundaryImpl() { }

    private String outMessage;
    private LogLevel logLevel;

    @Override
    protected void send(String message, LogLevel level) {
        this.outMessage += message;
        this.logLevel = level;
    }

    public void clearOutput() {
        this.outMessage = "";
        this.logLevel = null;
    }
}
