package ru.mephi.db.di;

import dagger.Module;
import dagger.Provides;
import ru.mephi.db.testBoundary.TestInputBoundaryImpl;
import ru.mephi.db.testBoundary.TestOutputBoundaryImpl;
import ru.mephi.db.application.adapter.io.InputBoundary;
import ru.mephi.db.application.adapter.io.OutputBoundary;

import javax.inject.Inject;


@Module
public class TestModule {

    private final TestOutputBoundaryImpl outputBoundary;
    private final TestInputBoundaryImpl inputBoundary;

    @Inject
    public TestModule() {
        outputBoundary = TestOutputBoundaryImpl.getInstance();
        inputBoundary = TestInputBoundaryImpl.getInstance();
    }

    @Provides
    InputBoundary provideInputBoundary() {
        return inputBoundary;
    }

    @Provides
    OutputBoundary provideOutputBoundary() {
        return outputBoundary;
    }

    public String getOutputText() {
        return outputBoundary.getOutMessage();
    }

    public OutputBoundary.LogLevel getOutputLogLevel() {
        return outputBoundary.getLogLevel();
    }

    public void addToInputList(String input) {
        inputBoundary.addToInputList(input);
    }

}