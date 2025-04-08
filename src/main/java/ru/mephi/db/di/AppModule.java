package ru.mephi.db.di;

import dagger.Module;
import dagger.Provides;
import ru.mephi.db.di.qulifier.CustomOutput;
import ru.mephi.db.util.Constants;
import ru.mephi.db.util.io.OutputUtils;

import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

@Module
public class AppModule {

    @Provides
    @Singleton
    public Scanner provideScanner() {
        return new Scanner(System.in);
    }

    public PrintStream providePrintStream() {
        try {
            return new PrintStream(Constants.TEST_PRINT_STREAM_FILE);
        } catch (FileNotFoundException e) {
            // TODO: handle error
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    public OutputUtils provideDefaultOutputUtils() {
        return new OutputUtils();
    }

    @Provides
    @Singleton
    @CustomOutput
    public OutputUtils provideCustomOutputUtils(PrintStream printStream) {
        return new OutputUtils(printStream);
    }
}
