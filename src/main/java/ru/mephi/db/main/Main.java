package ru.mephi.db.main;

import ru.mephi.db.adapter.cli.CliInputHandler;
import ru.mephi.db.infrastructure.di.AppComponent;
import ru.mephi.db.infrastructure.di.DaggerAppComponent;

import javax.inject.Inject;

public class Main {
    public static void main(String[] args) {

        AppComponent component = DaggerAppComponent.create();

        CliInputHandler cliInputHandler = component.cliInputHandler();

        cliInputHandler.start();
    }
}
