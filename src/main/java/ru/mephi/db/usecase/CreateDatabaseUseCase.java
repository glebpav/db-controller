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
            Properties props = new Properties();
            props.setProperty("version", "1.0.0"); // TODO: Get from build
            props.setProperty("createdAt", String.valueOf(System.currentTimeMillis()));

            try (BufferedWriter writer = Files.newBufferedWriter(dbInfoFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                writer.write(Constants.MAGIC_HEADER);
                writer.newLine();

                // Use this instead of `store` method, to prevent calling `writeDateComment`
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
            }

            System.out.println("Database created successfully at: " + dbPath);

        } catch (IOException e) {
            throw new DatabaseCreateException(e);
        }
    }
}
