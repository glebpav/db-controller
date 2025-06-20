package unit.ru.mephi.db.infrastructure.db;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

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
            try (Stream<Path> walk = Files.walk(testDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            boolean deleted = file.delete();
                            if (!deleted) {
                                System.err.println("Failed to delete file: " + file.getAbsolutePath());
                            }
                        });
            }
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
    void deleteDatabaseFile_allScenarios() throws Exception {
        dataRepository.deleteDatabaseFile(dbFilePath);

        assertFalse(Files.exists(Paths.get(dbFilePath)));
        assertFalse(Files.exists(Paths.get(tableFilePath)));
        dataRepository.createDatabaseFile(dbFilePath, "test_db");
        dataRepository.createTableFile(tableFilePath, "test_table", List.of("int", "str_20"));
        dataRepository.addTableReference(dbFilePath, tableFilePath);

        String nonExistentDbPath = testDir.resolve("nonexistent_db.txt").toString();
        assertThrows(FileNotFoundException.class,
                () -> dataRepository.deleteDatabaseFile(nonExistentDbPath));

        Path smallDbPath = testDir.resolve("small_db.txt");
        Files.write(smallDbPath, new byte[54 - 1]);
        assertThrows(IOException.class,
                () -> dataRepository.deleteDatabaseFile(smallDbPath.toString()));

        Path corruptedDbPath = testDir.resolve("corrupted_db.txt");
        dataRepository.createDatabaseFile(corruptedDbPath.toString(), "corrupted_db");

        try (RandomAccessFile dbFile = new RandomAccessFile(corruptedDbPath.toFile(), "rw")) {
            dbFile.seek(50);
            dbFile.writeInt(1000);
        }
        assertThrows(IOException.class,
                () -> dataRepository.deleteDatabaseFile(corruptedDbPath.toString()),
                "Должно выбрасывать исключение при неверном количестве таблиц");

        File tableFile = Paths.get(tableFilePath).toFile();
        assertTrue(tableFile.setWritable(false), "Не удалось сделать файл read-only");

        try {
            IOException ex = assertThrows(IOException.class,
                    () -> dataRepository.deleteDatabaseFile(dbFilePath));
            assertNotNull(ex.getMessage());
        } finally {
            if (!tableFile.setWritable(true)) {
                fail("Не удалось восстановить права на запись для файла: " + tableFile.getAbsolutePath());
            }
            Files.deleteIfExists(Paths.get(tableFilePath));
        }

        Path emptyDbPath = testDir.resolve("empty_db.txt");
        dataRepository.createDatabaseFile(emptyDbPath.toString(), "empty_db");

        assertDoesNotThrow(() -> dataRepository.deleteDatabaseFile(emptyDbPath.toString()));
        assertFalse(Files.exists(emptyDbPath));
    }

    @Test
    void deleteTableFile_ShouldHandleAllErrorCases() throws IOException {
        Path nonExistentTable = testDir.resolve("nonexistent_table.txt");
        Files.deleteIfExists(nonExistentTable);
        assertThrows(IOException.class,
                () -> dataRepository.deleteTableFile(nonExistentTable.toString()));

        Path unreadableTable = testDir.resolve("unreadable_table.txt");
        dataRepository.createTableFile(unreadableTable.toString(), "unreadable", List.of("int"));
        boolean readPermissionChanged = unreadableTable.toFile().setReadable(false);
        assumeTrue(readPermissionChanged, "Не удалось изменить права на чтение");
        try {
            IOException ex = assertThrows(IOException.class,
                    () -> dataRepository.deleteTableFile(unreadableTable.toString()));
            assertTrue(ex.getMessage().contains("not readable"));
        } finally {
            boolean readPermissionRestored = unreadableTable.toFile().setReadable(true);
            assertTrue(readPermissionRestored, "Не удалось восстановить права на чтение");
        }

        Path corruptedTable = testDir.resolve("corrupted_table.txt");
        Files.write(corruptedTable, new byte[54 + 100 + 100 - 1]);
        assertDoesNotThrow(() -> dataRepository.deleteTableFile(corruptedTable.toString()));

        Path cyclicPart1 = testDir.resolve("cyclic1.txt");
        Path cyclicPart2 = testDir.resolve("cyclic2.txt");
        dataRepository.createTableFile(cyclicPart1.toString(), "cyclic", List.of("int"));
        dataRepository.createTableFile(cyclicPart2.toString(), "cyclic", List.of("int"));

        try (RandomAccessFile file1 = new RandomAccessFile(cyclicPart1.toFile(), "rw");
             RandomAccessFile file2 = new RandomAccessFile(cyclicPart2.toFile(), "rw")) {
            file1.seek(54 + 100);
            file1.write(cyclicPart2.toString().getBytes(StandardCharsets.UTF_8));
            file2.seek(54 + 100);
            file2.write(cyclicPart1.toString().getBytes(StandardCharsets.UTF_8));
        }

        assertThrows(IOException.class,
                () -> dataRepository.deleteTableFile(cyclicPart1.toString()));

        Path primaryPart = testDir.resolve("primary_part.txt");
        dataRepository.createTableFile(primaryPart.toString(), "primary", List.of("int"));
        boolean writePermissionChanged = primaryPart.toFile().setWritable(false);
        assumeTrue(writePermissionChanged, "Не удалось изменить права на запись");
        try {
            IOException ex2 = assertThrows(IOException.class,
                    () -> dataRepository.deleteTableFile(primaryPart.toString()));
            assertTrue(ex2.getMessage().contains("Failed to delete primary table part"));
        } finally {
            boolean writePermissionRestored = primaryPart.toFile().setWritable(true);
            assertTrue(writePermissionRestored, "Не удалось восстановить права на запись");
        }

        Path part1 = testDir.resolve("partial1.txt");
        Path part2 = testDir.resolve("partial2.txt");
        dataRepository.createTableFile(part1.toString(), "partial", List.of("int"));
        dataRepository.createTableFile(part2.toString(), "partial", List.of("int"));

        try (RandomAccessFile file = new RandomAccessFile(part1.toFile(), "rw")) {
            file.seek(54 + 100);
            file.write(part2.toString().getBytes(StandardCharsets.UTF_8));
        }

        boolean part2WritePermissionChanged = part2.toFile().setWritable(false);
        assumeTrue(part2WritePermissionChanged, "Не удалось изменить права на запись для part2");
        try {
            IOException ex3 = assertThrows(IOException.class,
                    () -> dataRepository.deleteTableFile(part1.toString()));
            assertTrue(ex3.getMessage().contains("Failed to delete some table parts"));
            assertFalse(Files.exists(part1));
            assertTrue(Files.exists(part2));
        } finally {
            boolean part2WritePermissionRestored = part2.toFile().setWritable(true);
            assertTrue(part2WritePermissionRestored, "Не удалось восстановить права на запись для part2");
            Files.deleteIfExists(part1);
            Files.deleteIfExists(part2);
        }
    }

    @Test
    void deleteTableFile_ShouldHandlePointerErrors() throws IOException {
        Path mainWithMissingPart = testDir.resolve("main_missing_part.txt");
        dataRepository.createTableFile(mainWithMissingPart.toString(), "test_table", List.of("int"));

        Path missingPart = testDir.resolve("missing_part.txt");
        try (RandomAccessFile file = new RandomAccessFile(mainWithMissingPart.toFile(), "rw")) {
            file.seek(54 + 100);
            file.write(missingPart.toString().getBytes(StandardCharsets.UTF_8));
        }

        IOException missingEx = assertThrows(IOException.class,
                () -> dataRepository.deleteTableFile(mainWithMissingPart.toString()));

        assertTrue(missingEx.getMessage().contains("Table part not found") &&
                        missingEx.getMessage().contains(missingPart.toString()),
                "Должно сообщать об отсутствующей части таблицы");
        assertTrue(Files.exists(mainWithMissingPart), "Основная часть не должна быть удалена");

        Path mainWithUnreadablePart = testDir.resolve("main_unreadable_part.txt");
        Path unreadablePart = testDir.resolve("unreadable_part.txt");
        dataRepository.createTableFile(mainWithUnreadablePart.toString(), "test_table", List.of("int"));
        dataRepository.createTableFile(unreadablePart.toString(), "test_table", List.of("int"));

        try (RandomAccessFile file = new RandomAccessFile(mainWithUnreadablePart.toFile(), "rw")) {
            file.seek(54 + 100);
            file.write(unreadablePart.toString().getBytes(StandardCharsets.UTF_8));
        }

        assumeTrue(unreadablePart.toFile().setReadable(false));
        try {
            IOException readEx = assertThrows(IOException.class,
                    () -> dataRepository.deleteTableFile(mainWithUnreadablePart.toString()));

            assertTrue(readEx.getMessage().contains("Failed to read next part pointer from"),
                    "Должно сообщать о проблеме чтения указателя");
            assertTrue(readEx.getMessage().contains(unreadablePart.toString()),
                    "Должно содержать путь к проблемной части");
            assertTrue(Files.exists(mainWithUnreadablePart), "Основная часть не должна быть удалена");
        } finally {
            assertTrue(unreadablePart.toFile().setReadable(true));
            Files.deleteIfExists(mainWithUnreadablePart);
            Files.deleteIfExists(unreadablePart);
        }
        Files.deleteIfExists(mainWithMissingPart);
    }

    /* ------------------------------- Удаление ссылок таблицы -----------------------------*/
    @Test
    void removeTableReference_allScenarios() throws Exception {
        dataRepository.removeTableReference(dbFilePath, tableFilePath);

        assertTrue(Files.exists(Paths.get(dbFilePath)));

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "r")) {
            file.seek(50);
            int tableCount = file.readInt();
            assertEquals(0, tableCount);
        }

        String nonExistentDbPath = testDir.resolve("nonexistent_db.txt").toString();
        assertThrows(FileNotFoundException.class,
                () -> dataRepository.removeTableReference(nonExistentDbPath, tableFilePath));

        Path smallDbPath = testDir.resolve("small_db.txt");
        Files.write(smallDbPath, new byte[54 - 1]);
        assertThrows(IOException.class,
                () -> dataRepository.removeTableReference(smallDbPath.toString(), tableFilePath));
    }

    @Test
    void removeTableReference_multipleReferences_removesOnlyOne() throws Exception {
        String secondTablePath = testDir.resolve("second_table.txt").toString();
        dataRepository.createTableFile(secondTablePath, "second_table", List.of("int"));
        dataRepository.addTableReference(dbFilePath, secondTablePath);
        dataRepository.removeTableReference(dbFilePath, tableFilePath);

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "r")) {
            file.seek(50);
            int tableCount = file.readInt();
            assertEquals(1, tableCount);
            byte[] buffer = new byte[100];
            file.seek(54);
            file.readFully(buffer);
            String path = new String(buffer, StandardCharsets.UTF_8).trim();

            assertEquals(secondTablePath, path);
        }
    }
    @Test
    void removeTableReference_nonMatchingReference_doesNotRemove() throws Exception {
        String secondTablePath = testDir.resolve("second_table.txt").toString();
        dataRepository.createTableFile(secondTablePath, "second_table", List.of("int"));
        dataRepository.addTableReference(dbFilePath, secondTablePath);

        String fakePath = testDir.resolve("fake_table.txt").toString();
        dataRepository.removeTableReference(dbFilePath, fakePath);

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "r")) {
            file.seek(50);
            int tableCount = file.readInt();
            assertEquals(2, tableCount);
        }
    }
    @Test
    void removeTableReference_cantWriteToReadOnlyFile_throwsException() {
        Path readOnlyDb = Paths.get(dbFilePath);
        File file = readOnlyDb.toFile();
        assertTrue(file.setWritable(false), "Не удалось сделать файл доступным только для чтения");

        try {
            IOException ex = assertThrows(IOException.class,
                    () -> dataRepository.removeTableReference(dbFilePath, tableFilePath));
            assertNotNull(ex.getMessage());
        } finally {
            assertTrue(file.setWritable(true), "Не удалось восстановить права на запись");
        }
    }

    /* ------------------------------- Запись данных в файле -----------------------------*/

    
}
