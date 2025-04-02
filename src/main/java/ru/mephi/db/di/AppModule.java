package ru.mephi.db.di;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.Scanner;

@Module
public class AppModule {

    @Provides
    @Singleton
    public Scanner providesScanner() {
        return new Scanner(System.in);
    }
}
