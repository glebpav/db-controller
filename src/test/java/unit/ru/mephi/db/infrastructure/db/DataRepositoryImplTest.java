package unit.ru.mephi.db.infrastructure.db;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DataRepositoryImplTest {

    private DataRepositoryImpl dataRepository;
    private Path testDir;
    private String dbFilePath;
    private String tableFilePath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        dataRepository = new DataRepositoryImpl();
        testDir = tempDir;
        dbFilePath = testDir.resolve("test_db.txt").toString();
        tableFilePath = testDir.resolve("test_table.txt").toString();

        dataRepository.createDatabaseFile(dbFilePath, "test_db");
        dataRepository.createTableFile(tableFilePath, "test_table",
                Arrays.asList("int", "str_20"));
        dataRepository.addTableReference(dbFilePath, tableFilePath);
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Path.of(dbFilePath));
            Files.deleteIfExists(Path.of(tableFilePath));
        } catch (IOException e) {
            System.err.println("Cleanup warning: " + e.getMessage());
        }
    }

    /* ------------------------------- Создание БД -----------------------------*/
    @Test
    void createDatabaseFile_ShouldValidateNameLength() {
        String exactLengthName = "A".repeat(50);
        String tooLongName = "B".repeat(51);
        String shortName = "ShortName";

        Path validPath = testDir.resolve("valid_db.txt");
        Path invalidPath = testDir.resolve("invalid_db.txt");
        Path shortNamePath = testDir.resolve("short_name_db.txt");

        assertDoesNotThrow(
                () -> dataRepository.createDatabaseFile(validPath.toString(), exactLengthName),
                "Имя из 50 символов должно быть допустимо"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dataRepository.createDatabaseFile(invalidPath.toString(), tooLongName),
                "Должно выбрасываться исключение для имени длиннее 50 символов"
        );
        assertEquals(
                "Database name must be 50 characters or less",
                exception.getMessage(),
                "Сообщение об ошибке должно соответствовать требованию"
        );

        assertDoesNotThrow(
                () -> dataRepository.createDatabaseFile(shortNamePath.toString(), shortName),
                "Короткое имя должно быть допустимо"
        );
    }

    @Test
    void createTableFile_ShouldValidateAllCases() throws IOException {
        Path nestedDir = testDir.resolve("a/b/c");
        String tableInNestedDir = nestedDir.resolve("table.txt").toString();

        assertFalse(Files.exists(nestedDir));
        assertDoesNotThrow(() -> dataRepository.createTableFile(tableInNestedDir, "tbl", List.of("int")));
        assertTrue(Files.exists(nestedDir));
        assertTrue(Files.exists(Paths.get(tableInNestedDir)));

        Path existingDir = testDir.resolve("existing");
        Files.createDirectories(existingDir);
        String tableInExistingDir = existingDir.resolve("existing.txt").toString();
        assertDoesNotThrow(() -> dataRepository.createTableFile(tableInExistingDir, "tbl", List.of("int")));
        assertTrue(Files.exists(Paths.get(tableInExistingDir)));

        String longNameTable = testDir.resolve("long_name.txt").toString();
        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.createTableFile(longNameTable, "a".repeat(51), List.of("int")));

        String invalidExt = testDir.resolve("invalid.dat").toString();
        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.createTableFile(invalidExt, "tbl", List.of("int")));

        List<String> invalidSchema = List.of("invalid_type");
        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.createTableFile(tableFilePath, "tbl", invalidSchema));

        String rootTable = testDir.resolve("root.txt").toString();
        assertDoesNotThrow(() -> dataRepository.createTableFile(rootTable, "tbl", List.of("int")));
        assertTrue(Files.exists(Paths.get(rootTable)));
    }

    @Test
    void createDatabaseFile_ShouldRejectNullName() {
        Path path = testDir.resolve("null_name_db.txt");

        assertThrows(
                NullPointerException.class,
                () -> dataRepository.createDatabaseFile(path.toString(), null),
                "Должно выбрасываться исключение для null-имени"
        );
    }

    /* ------------------------------- Добавление ссылки -----------------------------*/
    @Test
    void addTableReference_ShouldValidateAllCases() throws IOException {
        Path nestedDir = testDir.resolve("n/d");
        String dbInNestedDir = nestedDir.resolve("db.txt").toString();
        String tableInNestedDir = nestedDir.resolve("tbl.txt").toString();

        dataRepository.createDatabaseFile(dbInNestedDir, "db");
        dataRepository.createTableFile(tableInNestedDir, "tbl", List.of("int"));
        assertDoesNotThrow(() -> dataRepository.addTableReference(dbInNestedDir, tableInNestedDir));

        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.addTableReference(dbInNestedDir, tableInNestedDir));

        String missingDbPath = testDir.resolve("missing_db.txt").toString();
        String missingTablePath = testDir.resolve("missing_table.txt").toString();

        assertThrows(FileNotFoundException.class,
                () -> dataRepository.addTableReference(missingDbPath, tableFilePath));

        assertThrows(FileNotFoundException.class,
                () -> dataRepository.addTableReference(dbFilePath, missingTablePath));

        String invalidDbExt = testDir.resolve("invalid_db.dat").toString();
        Files.createFile(Paths.get(invalidDbExt));

        String invalidTableExt = testDir.resolve("invalid_table.dat").toString();
        Files.createFile(Paths.get(invalidTableExt));

        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.addTableReference(invalidDbExt, tableFilePath));

        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.addTableReference(dbFilePath, invalidTableExt));

        String longNameTablePath = testDir.resolve("long_tbl.txt").toString();
        Files.createFile(Paths.get(longNameTablePath));

        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.createTableFile(longNameTablePath, "a".repeat(51), List.of("int")));

        String longPathTable = testDir.resolve("a".repeat(90) + ".txt").toString();
        Files.createFile(Paths.get(longPathTable));
        dataRepository.createTableFile(longPathTable, "tbl", List.of("int"));

        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.addTableReference(dbFilePath, longPathTable));

        String fullDbPath = testDir.resolve("full_db.txt").toString();
        dataRepository.createDatabaseFile(fullDbPath, "full_db");

        int maxTables = (65536 - 54) / 100;
        for (int i = 0; i < maxTables; i++) {
            String tempTablePath = testDir.resolve("t" + i + ".txt").toString();
            dataRepository.createTableFile(tempTablePath, "t" + i, List.of("int"));
            dataRepository.addTableReference(fullDbPath, tempTablePath);
        }

        String extraTablePath = testDir.resolve("extra_tbl.txt").toString();
        dataRepository.createTableFile(extraTablePath, "extra", List.of("int"));
        assertThrows(IllegalStateException.class,
                () -> dataRepository.addTableReference(fullDbPath, extraTablePath));

        String corruptedDbPath = testDir.resolve("corrupted_db.txt").toString();
        Files.write(Paths.get(corruptedDbPath), new byte[10]);
        assertThrows(IOException.class,
                () -> dataRepository.addTableReference(corruptedDbPath, tableFilePath));
    }

    /* ------------------------------- Проверка существования таблиц -----------------------------*/
    @Test
    void isTableExists_ShouldValidateAllCases() throws IOException {
        String existingTablePath = testDir.resolve("existing_table.txt").toString();
        String nonExistingTablePath = testDir.resolve("non_existing_table.txt").toString();
        String corruptedDbPath = testDir.resolve("corrupted_db.txt").toString();
        String invalidSizeDbPath = testDir.resolve("invalid_size_db.txt").toString();

        dataRepository.createDatabaseFile(dbFilePath, "TestDB");
        dataRepository.createTableFile(existingTablePath, "ExistingTable", List.of("int"));
        dataRepository.addTableReference(dbFilePath, existingTablePath);

        dataRepository.createDatabaseFile(corruptedDbPath, "CorruptedDB");
        try (RandomAccessFile file = new RandomAccessFile(corruptedDbPath, "rw")) {
            file.setLength(30);
        }

        dataRepository.createDatabaseFile(invalidSizeDbPath, "InvalidSizeDB");
        try (RandomAccessFile file = new RandomAccessFile(invalidSizeDbPath, "rw")) {
            byte[] nameBytes = "InvalidSizeDB".getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 50));
            file.write(paddedName);
            file.writeInt(2);
        }

        assertTrue(dataRepository.isTableExists(dbFilePath, existingTablePath));
        assertFalse(dataRepository.isTableExists(dbFilePath, nonExistingTablePath));

        String emptyDbPath = testDir.resolve("empty_db.txt").toString();
        dataRepository.createDatabaseFile(emptyDbPath, "EmptyDB");
        assertFalse(dataRepository.isTableExists(emptyDbPath, existingTablePath));

        assertThrows(IOException.class, () -> dataRepository.isTableExists(corruptedDbPath, existingTablePath));
        assertThrows(IOException.class, () -> dataRepository.isTableExists(invalidSizeDbPath, existingTablePath));

        String missingDbPath = testDir.resolve("missing_db.txt").toString();
        assertThrows(FileNotFoundException.class, () -> dataRepository.isTableExists(missingDbPath, existingTablePath));

        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            String differentCasePath = existingTablePath.toUpperCase();
            if (!existingTablePath.equals(differentCasePath)) {
                assertFalse(dataRepository.isTableExists(dbFilePath, differentCasePath));
            }
        }

        Path nonNormalizedPath = Paths.get(testDir.toString(), "..", testDir.getFileName().toString(), ".", "existing_table.txt");
        assertNotEquals(nonNormalizedPath.toString(), nonNormalizedPath.normalize().toString());
        assertTrue(dataRepository.isTableExists(dbFilePath, nonNormalizedPath.toString()));

        Path relativePath = Paths.get("./existing_table.txt");
        assertTrue(dataRepository.isTableExists(dbFilePath, relativePath.toString()));
    }

    /* ------------------------------- Проверка удаления -----------------------------*/
    @Test
    void deleteOperations_ShouldHandleAllErrorCases() throws IOException {
        Path validDb = testDir.resolve("valid_db.txt");
        Path validTable = testDir.resolve("valid_table.txt");
        Path corruptedDb = testDir.resolve("corrupted_db.txt");
        Path invalidPointerDb = testDir.resolve("invalid_pointer_db.txt");
        Path nonExistentDb = testDir.resolve("non_existent_db.txt");
        Path multiPart1 = testDir.resolve("multi_part1.txt");
        Path multiPart2 = testDir.resolve("multi_part2.txt");
        Path corruptedTable = testDir.resolve("corrupted_table.txt");
        Path unreadableTable = testDir.resolve("unreadable_table.txt");
        Path nonExistentTable = testDir.resolve("non_existent_table_" + System.currentTimeMillis() + ".txt");

        Files.deleteIfExists(nonExistentTable);
        assertFalse(Files.exists(nonExistentTable), "Файл не должен существовать");
        dataRepository.createDatabaseFile(validDb.toString(), "valid_db");
        dataRepository.createTableFile(validTable.toString(), "valid_table", List.of("int"));
        dataRepository.addTableReference(validDb.toString(), validTable.toString());
        assertThrows(FileNotFoundException.class,
                () -> dataRepository.deleteDatabaseFile(nonExistentDb.toString()));

        Files.write(corruptedDb, new byte[54 - 1]);
        assertThrows(IOException.class,
                () -> dataRepository.deleteDatabaseFile(corruptedDb.toString()));
        try (RandomAccessFile file = new RandomAccessFile(invalidPointerDb.toFile(), "rw")) {
            file.setLength(54 + 100);
            file.seek(50);
            file.writeInt(1000);
        }
        assertThrows(IOException.class,
                () -> dataRepository.deleteDatabaseFile(invalidPointerDb.toString()));
        dataRepository.createTableFile(multiPart1.toString(), "multi_part", List.of("int"));
        dataRepository.createTableFile(multiPart2.toString(), "multi_part", List.of("int"));

        try (RandomAccessFile file = new RandomAccessFile(multiPart1.toFile(), "rw")) {
            file.seek(54 + 100);
            file.write(multiPart2.toString().getBytes(StandardCharsets.UTF_8));
        }

        dataRepository.createTableFile(corruptedTable.toString(), "corrupted", List.of("int"));
        Files.write(corruptedTable, new byte[54 + 100 - 1]);
        assertDoesNotThrow(() -> dataRepository.deleteTableFile(corruptedTable.toString()));
        assertFalse(Files.exists(corruptedTable));
        dataRepository.createTableFile(unreadableTable.toString(), "unreadable", List.of("int"));

        boolean setReadableResult = unreadableTable.toFile().setReadable(false);
        assumeTrue(setReadableResult, "Не удалось установить права на чтение");

        try {
            assertThrows(IOException.class,
                    () -> dataRepository.deleteTableFile(unreadableTable.toString()));
        } finally {
            boolean restoreResult = unreadableTable.toFile().setReadable(true);
            assertTrue(restoreResult, "Не удалось восстановить права на чтение");
        }

        assertThrows(IOException.class,
                () -> dataRepository.deleteTableFile(nonExistentTable.toString()));

        assertDoesNotThrow(() -> dataRepository.deleteTableFile(multiPart1.toString()));
        assertFalse(Files.exists(multiPart1));
        assertFalse(Files.exists(multiPart2));

        assertTrue(Files.exists(validDb));
        assertDoesNotThrow(() -> dataRepository.deleteDatabaseFile(validDb.toString()));
        assertFalse(Files.exists(validDb));
    }


}
