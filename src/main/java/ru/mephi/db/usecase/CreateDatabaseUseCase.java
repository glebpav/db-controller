package ru.mephi.db.usecase;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.DatabaseCreateException;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.util.Constants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Properties;

@AllArgsConstructor
public class CreateDatabaseUseCase {

    public void execute(Path dbPath) throws DatabaseException {
        try {
            Files.createDirectories(dbPath);

            Path dbInfoFile = dbPath.resolve(Constants.DB_INFO_FILE);
            Path dbLogFile = dbPath.resolve(Constants.DB_LOG_FILE);
            Properties props = new Properties();
            props.setProperty("version", "1.0.0"); // TODO: Get from build
            props.setProperty("createdAt", String.valueOf(System.currentTimeMillis()));

            try (BufferedWriter writer = Files.newBufferedWriter(dbInfoFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                writer.write(Constants.MAGIC_HEADER);
                writer.newLine();

                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
            }

            try (BufferedWriter logWriter = Files.newBufferedWriter(dbLogFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                logWriter.write("Database log initialized at: " + props.getProperty("createdAt"));
                logWriter.newLine();
            }

            System.out.println("Database created successfully at: " + dbPath);

        } catch (IOException e) {
            throw new DatabaseCreateException(e);
        }
    }
}
