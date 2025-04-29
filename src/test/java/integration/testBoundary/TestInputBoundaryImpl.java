package integration.testBoundary;

import lombok.Getter;
import ru.mephi.db.application.adapter.io.InputBoundary;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TestInputBoundaryImpl implements InputBoundary {

    @Getter
    private static final TestInputBoundaryImpl instance = new TestInputBoundaryImpl();

    private final Deque<String> inputList;

    private TestInputBoundaryImpl() {
        inputList = new LinkedList<>();
    }

    @Override
    public String next() {
        String nextInput = inputList.getLast();
        inputList.removeLast();
        return nextInput;
    }

    public void addToInputList(String input) {
        inputList.addFirst(input);
    }

}
