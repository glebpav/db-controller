package ru.mephi.db.infrastructure.cli;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.adapter.io.InputBoundary;

import javax.inject.Inject;
import java.util.Scanner;

@AllArgsConstructor(onConstructor_ = @Inject)
public class CliInputBoundaryImpl implements InputBoundary {
    private final Scanner scanner;

    @Override
    public String next() {
        return scanner.nextLine();
    }
}
