package ru.mephi.db.infrastructure.cil;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.adapter.cli.InputBoundary;

import javax.inject.Inject;
import java.util.Scanner;

@AllArgsConstructor(onConstructor_ = @Inject)
public class CliInputBoundaryImpl implements InputBoundary {

    Scanner scanner;

    @Override
    public String next() {
        return scanner.nextLine();
    }
}
