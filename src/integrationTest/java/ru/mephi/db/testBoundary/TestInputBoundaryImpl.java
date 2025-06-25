package ru.mephi.db.testBoundary;

import lombok.Getter;
import ru.mephi.db.application.adapter.io.InputBoundary;

import java.util.LinkedList;
import java.util.Queue;

public class TestInputBoundaryImpl implements InputBoundary {

    @Getter
    private static final TestInputBoundaryImpl instance = new TestInputBoundaryImpl();

    private final Queue<String> inputQueue;

    private TestInputBoundaryImpl() {
        inputQueue = new LinkedList<>();
    }

    @Override
    public String next() {
        return inputQueue.poll();
    }

    public void addToInputList(String input) {
        inputQueue.add(input);
    }

    public void clearInputs() {
        inputQueue.clear();
    }
}
