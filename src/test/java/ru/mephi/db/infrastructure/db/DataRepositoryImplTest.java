package ru.mephi.db.infrastructure.db;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

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
        dataRepository.createTableFile(tableFilePath, "test_table",
               Arrays.asList("int", "str_20"));
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
    void tableCreation_ShouldValidateDatabaseNameLength() {
        String exactLengthName = "A".repeat(50);
        String tooLongName = "B".repeat(51);
        String shortName = "ShortName";

        Path exactLengthPath = testDir.resolve("exact_length_table.txt");
        Path tooLongPath = testDir.resolve("too_long_table.txt");
        Path shortNamePath = testDir.resolve("short_name_table.txt");

        assertDoesNotThrow(
                () -> dataRepository.createTableFile(exactLengthPath.toString(), exactLengthName,
                        Arrays.asList("int", "str_20")),
                "Имя из 50 символов должно быть допустимо"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dataRepository.createTableFile(tooLongPath.toString(), tooLongName,
                        Arrays.asList("int", "str_20")),
                "Должно выбрасываться исключение для имени длиннее 50 символов"
        );

        assertTrue(
                exception.getMessage().contains("must be 50 characters or less"),
                "Сообщение об ошибке должно содержать информацию о длине имени"
        );

        assertDoesNotThrow(
                () -> dataRepository.createTableFile(shortNamePath.toString(), shortName,
                        Arrays.asList("int", "str_20")),
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

        List<String> invalidSchema = List.of("invalid_type");
        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.createTableFile(tableFilePath, "tbl", invalidSchema));

        String rootTable = testDir.resolve("root.txt").toString();
        assertDoesNotThrow(() -> dataRepository.createTableFile(rootTable, "tbl", List.of("int")));
        assertTrue(Files.exists(Paths.get(rootTable)));
    }

    @Test
    void tableCreation_ShouldRejectNullTableName() {
        Path path = testDir.resolve("null_name_table.txt");

        assertThrows(
                NullPointerException.class,
                () -> dataRepository.createTableFile(path.toString(), null,
                        Arrays.asList("int", "str_20")),
                "Должно выбрасываться исключение при передаче null в качестве имени таблицы"
        );
    }

    @Test
    void tableCreation_ShouldHandleAllReferenceCases() throws IOException {
        Path nestedDir = testDir.resolve("n/d");
        Files.createDirectories(nestedDir);
        String nestedTablePath = nestedDir.resolve("t.txt").toString();

        assertDoesNotThrow(() ->
                dataRepository.createTableFile(nestedTablePath, "tbl", List.of("int")));
        assertThrows(IllegalArgumentException.class, () ->
                dataRepository.createTableFile(nestedTablePath, "tbl", List.of("int")));

        String missingTablePath = testDir.resolve("missing.txt").toString();
        assertThrows(IOException.class, () ->
                dataRepository.addRecord(missingTablePath, List.of("1")));

        String invalidExtPath = testDir.resolve("invalid.dat").toString();
        Files.createFile(Paths.get(invalidExtPath));
        assertThrows(IOException.class, () ->
                dataRepository.addRecord(invalidExtPath, List.of("1")));

        String longNamePath = testDir.resolve("long.txt").toString();
        assertThrows(IllegalArgumentException.class, () ->
                dataRepository.createTableFile(longNamePath, "a".repeat(51), List.of("int")));

        String masterDbPath = testDir.resolve("Master.txt").toString();
        try (RandomAccessFile dbFile = new RandomAccessFile(masterDbPath, "rw")) {
            dbFile.setLength(65536 - 100);
        }

        String lastTablePath = testDir.resolve("last.txt").toString();
        dataRepository.createTableFile(lastTablePath, "last", List.of("int"));

        String corruptedPath = testDir.resolve("corrupt.txt").toString();
        Files.write(Paths.get(corruptedPath), new byte[10]);
        assertThrows(IOException.class, () ->
                dataRepository.addRecord(corruptedPath, List.of("1")));
    }

    /* ------------------------------- Проверка существования таблиц -----------------------------*/
    @Test
    void isTableExists_ShouldValidateAllCases() throws IOException {
        String existingTablePath = testDir.resolve("existing_table.txt").toString();
        dataRepository.createTableFile(existingTablePath, "ExistingTable", List.of("int"));

        Path masterDbPath = Paths.get(existingTablePath).getParent().resolve("Master.txt");
        assertTrue(Files.exists(masterDbPath), "Master DB должна быть создана автоматически");

        String nonExistingTablePath = testDir.resolve("non_existing_table.txt").toString();
        String corruptedDbPath = testDir.resolve("corrupted_db.txt").toString();
        String invalidSizeDbPath = testDir.resolve("invalid_size_db.txt").toString();

        try (RandomAccessFile file = new RandomAccessFile(corruptedDbPath, "rw")) {
            file.write(new byte[30]); // Слишком маленький файл
        }

        try (RandomAccessFile file = new RandomAccessFile(invalidSizeDbPath, "rw")) {
            byte[] nameBytes = "InvalidSizeDB".getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, nameBytes.length);
            file.write(paddedName);
            file.writeInt(2);
        }
        assertTrue(dataRepository.isTableExists(masterDbPath.toString(), existingTablePath),
                "Должен найти существующую таблицу");

        assertFalse(dataRepository.isTableExists(masterDbPath.toString(), nonExistingTablePath),
                "Не должен находить несуществующую таблицу");

        String emptyDbPath = testDir.resolve("empty_db.txt").toString();
        try (RandomAccessFile file = new RandomAccessFile(emptyDbPath, "rw")) {
            byte[] nameBytes = "EmptyDB".getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, nameBytes.length);
            file.write(paddedName);
            file.writeInt(0);
        }
        assertFalse(dataRepository.isTableExists(emptyDbPath, existingTablePath),
                "Должен вернуть false для пустой БД");

        assertThrows(IOException.class,
                () -> dataRepository.isTableExists(corruptedDbPath, existingTablePath),
                "Должен бросить исключение для поврежденной БД");

        assertThrows(IOException.class,
                () -> dataRepository.isTableExists(invalidSizeDbPath, existingTablePath),
                "Должен бросить исключение при несоответствии размера");

        String missingDbPath = testDir.resolve("missing_db.txt").toString();
        assertThrows(FileNotFoundException.class,
                () -> dataRepository.isTableExists(missingDbPath, existingTablePath),
                "Должен бросить исключение для отсутствующей БД");

        Path nonNormalizedPath = Paths.get(testDir.toString(), "..",
                testDir.getFileName().toString(), "existing_table.txt");
        assertTrue(dataRepository.isTableExists(masterDbPath.toString(), nonNormalizedPath.toString()),
                "Должен корректно обрабатывать ненормализованные пути");

        Path relativePath = Paths.get("existing_table.txt");
        assertTrue(dataRepository.isTableExists(masterDbPath.toString(), relativePath.toString()),
                "Должен корректно обрабатывать относительные пути");
    }

    /* ------------------------------- Проверка удаления -----------------------------*/
    @Test
    void deleteDatabaseFile_allScenarios() throws Exception {
        Path masterDbPath = Paths.get(tableFilePath).getParent().resolve("Master.txt");
        assertTrue(Files.exists(masterDbPath), "Master DB должен существовать после создания таблицы");
        assertTrue(Files.exists(Paths.get(tableFilePath)), "Таблица должна существовать");

        dataRepository.deleteDatabaseFile(masterDbPath.toString());
        assertFalse(Files.exists(masterDbPath), "Master DB должен быть удален");
        assertFalse(Files.exists(Paths.get(tableFilePath)), "Таблица должна быть удалена вместе с БД");

        String newTablePath = testDir.resolve("new_table.txt").toString();
        dataRepository.createTableFile(newTablePath, "new_table", List.of("int", "str_20"));
        Path newMasterDbPath = Paths.get(newTablePath).getParent().resolve("Master.txt");
        assertTrue(Files.exists(newMasterDbPath), "Новый Master DB должен быть создан");
        assertTrue(Files.exists(Paths.get(newTablePath)), "Новая таблица должна существовать");

        String nonExistentDbPath = testDir.resolve("nonexistent_db.txt").toString();
        assertThrows(FileNotFoundException.class,
                () -> dataRepository.deleteDatabaseFile(nonExistentDbPath),
                "Должно выбрасываться исключение для несуществующей БД");

        Path smallDbPath = testDir.resolve("small_db.txt");
        Files.write(smallDbPath, new byte[54 - 1]);
        assertThrows(IOException.class,
                () -> dataRepository.deleteDatabaseFile(smallDbPath.toString()),
                "Должно выбрасываться исключение для поврежденной БД (малый размер)");

        Path corruptedDbPath = testDir.resolve("corrupted_db.txt");
        try (RandomAccessFile dbFile = new RandomAccessFile(corruptedDbPath.toFile(), "rw")) {
            byte[] nameBytes = "corrupted_db".getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, nameBytes.length);
            dbFile.write(paddedName);
            dbFile.writeInt(1000);

            dbFile.write(new byte[100 * 10]);
        }
        assertThrows(IOException.class,
                () -> dataRepository.deleteDatabaseFile(corruptedDbPath.toString()),
                "Должно выбрасываться исключение для поврежденной БД (некорректное кол-во таблиц)");
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            Path testTablePath = testDir.resolve("test_table.txt");
            dataRepository.createTableFile(testTablePath.toString(),
                    "test_table",
                    List.of("int", "str_20"));

            Path testMasterPath = testTablePath.getParent().resolve("Master.txt");
            assertTrue(Files.exists(testMasterPath), "Master DB должен быть создан автоматически");
            assertTrue(testTablePath.toFile().setWritable(false));

            try {
                assertThrows(IOException.class,
                        () -> dataRepository.deleteDatabaseFile(testMasterPath.toString()),
                        "Должно выбрасываться исключение при защищенной таблице");
            } finally {
                if (Files.exists(testTablePath)) {
                    testTablePath.toFile().setWritable(true);
                }
                try {
                    Files.deleteIfExists(testTablePath);
                    Files.deleteIfExists(testMasterPath);
                } catch (IOException e) {
                    System.err.println("Ошибка при очистке тестовых файлов: " + e.getMessage());
                }
            }
        }
        Path emptyDbPath = testDir.resolve("empty_db.txt");
        try (RandomAccessFile file = new RandomAccessFile(emptyDbPath.toFile(), "rw")) {
            byte[] nameBytes = "empty_db".getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, nameBytes.length);
            file.write(paddedName);
            file.writeInt(0);
        }
        assertDoesNotThrow(() -> dataRepository.deleteDatabaseFile(emptyDbPath.toString()),
                "Удаление пустой БД не должно вызывать исключений");
        assertFalse(Files.exists(emptyDbPath), "Пустая БД должна быть удалена");
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
        Files.write(corruptedTable, new byte[258 - 100 - 1]);
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
    void removeTableReference_missingTableInDb_doesNotFail() throws Exception {
        String masterDbPath = testDir.resolve("Master_empty.txt").toString();
        String missingTablePath = testDir.resolve("missing_table.txt").toString();

        try (RandomAccessFile file = new RandomAccessFile(masterDbPath, "rw")) {
            byte[] nameBytes = "EmptyDB".getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 50));
            file.write(paddedName);
            file.writeInt(0); // recordCountInThisPage
            file.writeInt(0); // tableCount
        }

        Method method = DataRepositoryImpl.class.getDeclaredMethod(
                "removeTableReference", String.class, String.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> method.invoke(dataRepository, masterDbPath, missingTablePath),
                "Удаление несуществующей ссылки не должно вызывать исключений");

        try (RandomAccessFile file = new RandomAccessFile(masterDbPath, "r")) {
            file.seek(54);
            int tableCount = file.readInt();
            assertEquals(0, tableCount);
        }
    }

    @Test
    void removeTableReference_smallDbFile_throwsIOException() throws Exception {
        Path smallDbPath = testDir.resolve("small_db.txt");
        Files.write(smallDbPath, new byte[54 - 1]); // меньше минимального размера
        String tableFilePath = testDir.resolve("fake_table.txt").toString();

        Method method = DataRepositoryImpl.class.getDeclaredMethod(
                "removeTableReference", String.class, String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(dataRepository, smallDbPath.toString(), tableFilePath);
        });

        assertEquals(IOException.class, exception.getCause().getClass());
    }

    @Test
    void removeTableReference_nonExistentDb_throwsException() throws Exception {
        String nonExistentDbPath = testDir.resolve("nonexistent_db.txt").toString();
        String tableFilePath = testDir.resolve("fake_table.txt").toString();

        Method method = DataRepositoryImpl.class.getDeclaredMethod(
                "removeTableReference", String.class, String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(dataRepository, nonExistentDbPath, tableFilePath);
        });

        assertEquals(FileNotFoundException.class, exception.getCause().getClass());
    }

    @Test
    void removeTableReference_multipleReferences_removesOnlyOne() throws Exception {
        String secondTablePath = testDir.resolve("second_table.txt").toString();
        dataRepository.createTableFile(secondTablePath, "second_table", List.of("int"));
        String masterDbPath = testDir.resolve("Master.txt").toString();

        Method removeTableRefMethod = DataRepositoryImpl.class.getDeclaredMethod(
                "removeTableReference", String.class, String.class);
        removeTableRefMethod.setAccessible(true);

        removeTableRefMethod.invoke(dataRepository, masterDbPath, tableFilePath);

        try (RandomAccessFile file = new RandomAccessFile(masterDbPath, "r")) {
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
        String masterDbPath = testDir.resolve("Master.txt").toString();

        Method removeTableRefMethod = DataRepositoryImpl.class.getDeclaredMethod(
                "removeTableReference", String.class, String.class);
        removeTableRefMethod.setAccessible(true);

        String fakePath = testDir.resolve("fake_table.txt").toString();
        removeTableRefMethod.invoke(dataRepository, masterDbPath, fakePath);

        try (RandomAccessFile file = new RandomAccessFile(masterDbPath, "r")) {
            file.seek(50);
            int tableCount = file.readInt();
            assertEquals(2, tableCount);
        }
    }

    @Test
    void removeTableReference_cantWriteToReadOnlyFile_throwsException() throws Exception {
        String masterDbPath = testDir.resolve("Master.txt").toString();
        tableFilePath = testDir.resolve("test_table.txt").toString();
        try (RandomAccessFile dbFile = new RandomAccessFile(masterDbPath, "rw")) {
            dbFile.writeBytes("test_db");
        }

        File masterFile = new File(masterDbPath);
        assertTrue(masterFile.exists(), "Master.txt должен существовать");
        assertTrue(masterFile.setWritable(false), "Не удалось сделать файл только для чтения");

        try {
            Method removeTableRefMethod = DataRepositoryImpl.class.getDeclaredMethod(
                    "removeTableReference", String.class, String.class);
            removeTableRefMethod.setAccessible(true);

            Exception ex = (Exception) assertThrows(InvocationTargetException.class, () ->
                    removeTableRefMethod.invoke(dataRepository, masterDbPath, tableFilePath))
                    .getCause();

            assertInstanceOf(IOException.class, ex);
            assertNotNull(ex.getMessage());
        } finally {
            assertTrue(masterFile.setWritable(true), "Не удалось восстановить права на запись");
        }
    }

    /* ------------------------------- Запись данных в файле -----------------------------*/
    @Test
    void testAddRecord_Successful() throws Exception {
        String tableFilePath = testDir.resolve("test_table_add.txt").toString();
        dataRepository.createTableFile(tableFilePath, "test_table", Arrays.asList("int", "str_20"));

        dataRepository.addRecord(tableFilePath, Arrays.asList(1, "JohnDoe"));
        List<Object> record = dataRepository.readRecord(tableFilePath, 0, 0);
        assertNotNull(record);
        assertEquals(1, record.get(0));
        assertEquals("JohnDoe", record.get(1));
    }

    @Test
    void testAddRecord_Combined() throws IOException {
        List<Object> testData = Arrays.asList(1, "Test String");
        dataRepository.addRecord(tableFilePath, testData);

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "r")) {
            file.seek(50);
            assertEquals(1, file.readInt(), "Record count should be 1");
        }

        assertThrows(IllegalArgumentException.class, () ->
                dataRepository.addRecord(tableFilePath, Arrays.asList("not_an_int", "JohnDoe")));

        assertThrows(IllegalArgumentException.class, () ->
                dataRepository.addRecord(tableFilePath, Arrays.asList(1, "ThisStringIsWayTooLongForStr20")));

        assertThrows(IllegalArgumentException.class, () ->
                dataRepository.addRecord(tableFilePath, Arrays.asList(1, "AnotherVeryLongStringThatExceedsLimit")));

        String invalidPath = tableFilePath.replace(".txt", ".dat");
        assertThrows(IOException.class,
                () -> dataRepository.addRecord(invalidPath, Arrays.asList(1, "Test String")),
                "Should throw IOException for invalid extension");
    }

    @Test
    void testAddRecord_AllScenarios() throws IOException {
        List<Object> testData = Arrays.asList(1, "Test String");
        dataRepository.addRecord(tableFilePath, testData);

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "r")) {
            file.seek(50);
            assertEquals(1, file.readInt(), "Record count should be 1 after first addition");
        }
        int recordCount = 100;
        for (int i = 1; i < recordCount; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "r")) {
            file.seek(50);
            assertEquals(recordCount, file.readInt(), "Record count mismatch after multiple additions");
        }

        assertAll("Invalid cases",
                () -> {
                    String invalidPath = tableFilePath.replace(".txt", ".dat");
                    assertThrows(IOException.class,
                            () -> dataRepository.addRecord(invalidPath, Arrays.asList(1, "Test")));
                },
                () -> {
                    assertThrows(IllegalArgumentException.class,
                            () -> dataRepository.addRecord(tableFilePath,
                                    Arrays.asList("String instead of int", "Test")));
                },
                () -> {
                    assertThrows(IllegalArgumentException.class,
                            () -> dataRepository.addRecord(tableFilePath,
                                    Arrays.asList(1, 123)));
                },
                () -> {
                    assertThrows(IllegalArgumentException.class,
                            () -> dataRepository.addRecord(tableFilePath,
                                    List.of(1)));
                },
                () -> {
                    assertThrows(IllegalArgumentException.class,
                            () -> dataRepository.addRecord(tableFilePath,
                                    Arrays.asList(1, "A".repeat(21))));
                }
        );
    }

    @Test
    void testAddRecord_WithPageCreation() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "r")) {
            file.seek(50 + 4 + 4 + 100 + 100 - 100); // TABLE_HEADER_SIZE - TABLE_POINTER_SIZE
            byte[] pointer = new byte[100]; // TABLE_POINTER_SIZE
            file.readFully(pointer);
            String pointerContent = new String(pointer).trim();
            assertTrue(pointerContent.isEmpty(),
                    "Next page pointer should be empty initially. Actual: '" + pointerContent + "'");
        }

        int recordsPerPage = 0;
        try {
            while (true) {
                dataRepository.addRecord(tableFilePath, Arrays.asList(recordsPerPage, "User" + recordsPerPage));
                recordsPerPage++;
            }
        } catch (Exception e) {
            // Ловим переполнение страницы
        }

        String nextPagePath;
        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "r")) {
            file.seek(50 + 4 + 4 + 100 + 100 - 100);
            byte[] pointer = new byte[100];
            file.readFully(pointer);
            nextPagePath = new String(pointer).trim();
            assertFalse(nextPagePath.isEmpty(),
                    "Next page pointer should be set after overflow");
        }

        assertTrue(Files.exists(Paths.get(nextPagePath)),
                "Next page file should exist at: " + nextPagePath);

        try (RandomAccessFile newPageFile = new RandomAccessFile(nextPagePath, "r")) {
            byte[] nameBytes = new byte[50];
            newPageFile.readFully(nameBytes);
            assertEquals("test_table", new String(nameBytes).trim());

            newPageFile.seek(50);

            byte[] schemaBytes = new byte[100];
            newPageFile.readFully(schemaBytes);
            newPageFile.seek(50 + 4 + 4 + 100);
            byte[] nextPointer = new byte[100];
            newPageFile.readFully(nextPointer);
        }
    }

    @Test
    void generateNextTablePartPath_shouldGenerateCorrectSequence() throws Exception {

        Method method = DataRepositoryImpl.class.getDeclaredMethod(
                "generateNextTablePartPath", String.class);
        method.setAccessible(true);

        String firstPart = (String) method.invoke(dataRepository, tableFilePath);
        assertEquals(testDir.resolve("test_table_part1.txt").toString(), firstPart);

        Files.createFile(Paths.get(firstPart));
        String secondPart = (String) method.invoke(dataRepository, tableFilePath);
        assertEquals(testDir.resolve("test_table_part2.txt").toString(), secondPart);

        String relativePath = "relative_table.txt";
        String relativeResult = (String) method.invoke(dataRepository, relativePath);
        assertTrue(relativeResult.endsWith("relative_table_part1.txt"));
    }

    /* ------------------------------- Чтение данных -----------------------------*/
    @Test
    public void testReadRecord_ValidFirstRecord_Success() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(100, "John"));

        List<Object> result = dataRepository.readRecord(tableFilePath, 0, 0);

        assertEquals(100, result.get(0));
        assertEquals("John", result.get(1));
    }

    @Test
    public void testReadRecord_ValidMultipleRecords_Success() throws Exception {
        for (int i = 0; i < 3; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }

        for (int i = 0; i < 3; i++) {
            List<Object> result = dataRepository.readRecord(tableFilePath, i, 0);
            assertEquals(i, result.get(0));
            assertEquals("User" + i, result.get(1));
        }
    }

    @Test
    public void testReadRecord_IndexTooLow_Exception() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(100, "John"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dataRepository.readRecord(tableFilePath, -1, 0));

        assertTrue(exception.getMessage().contains("Invalid record index"));
    }

    @Test
    public void testReadRecord_IndexTooHigh_Exception() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(100, "John"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dataRepository.readRecord(tableFilePath, 1, 0));

        assertTrue(exception.getMessage().contains("Invalid record index"));
    }

    @Test
    public void testReadRecord_ReadFromSecondPage_Success() throws Exception {
        for (int i = 0; i < 40; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }
        dataRepository.addRecord(tableFilePath, Arrays.asList(40, "User40"));
        List<Object> result = dataRepository.readRecord(tableFilePath, 40, 0);

        assertEquals(40, result.get(0));
        assertEquals("User40", result.get(1));
    }

    @Test
    public void testReadRecord_StringLengthValidation_Success() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(999, "short"));

        List<Object> result = dataRepository.readRecord(tableFilePath, 0, 0);

        assertEquals(999, result.get(0));
        assertEquals("short", result.get(1));
    }

    @Test
    public void testReadRecord_StringExceedsMaxLength_Failure() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(1, "short"));

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "rw")) {
            file.seek(50 + 4 + 4 + 100 + 100);
            file.writeInt(1);

            String invalidString = "thisstringiswaytoolong"; // >10 символов
            int maxLength = 10;
            file.writeInt(invalidString.length());
            byte[] bytes = invalidString.getBytes(StandardCharsets.UTF_8);
            file.write(bytes);
            if (maxLength > bytes.length) {
                file.write(new byte[maxLength - bytes.length]);
            }
        }
        IOException exception = assertThrows(IOException.class, () ->
                dataRepository.readRecord(tableFilePath, 0, 0));

        assertTrue(exception.getMessage().contains("String length exceeds max allowed size"));
    }

    @Test
    public void testReadRecord_CorruptedIndexOffset_Failure() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(100, "John"));

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "rw")) {
            long offsetPosition = file.length() - 8L;
            file.seek(offsetPosition);
            file.writeLong(10);
        }

        IOException exception = assertThrows(IOException.class, () ->
                dataRepository.readRecord(tableFilePath, 0, 0));

        assertTrue(exception.getMessage().contains("Invalid data offset in index"));
    }

    @Test
    void readRecord_shouldThrowWhenIndexPositionInvalid() throws IOException {
        Path tablePath = testDir.resolve("invalid_index_test.txt");
        List<String> schema = Arrays.asList("int", "str_10");
        dataRepository.createTableFile(tablePath.toString(), "test_table", schema);
        List<Object> testData = Arrays.asList(123, "test");
        dataRepository.addRecord(tablePath.toString(), testData);
        long originalSize = Files.size(tablePath);
        try (RandomAccessFile file = new RandomAccessFile(tablePath.toFile(), "rw")) {
            file.setLength(258 - 1); // Гарантированно меньше нужного
        }
        Exception exception = assertThrows(IOException.class,
                () -> dataRepository.readRecord(tablePath.toString(), 0, 0));

        assertTrue(exception.getMessage().contains("Invalid index position"),
                "Ожидалось сообщение о недопустимой позиции индекса. Получено: " + exception.getMessage());

        System.out.println("Original file size: " + originalSize);
        System.out.println("Truncated file size: " + Files.size(tablePath));
        System.out.println("TABLE_HEADER_SIZE: " + 258);
    }

    /* ------------------------------- Удаление записей -----------------------------*/
    @Test
    public void testDeleteRecord_ValidIndex_Success() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(100, "John"));
        dataRepository.addRecord(tableFilePath, Arrays.asList(200, "Alice"));

        assertDoesNotThrow(() -> dataRepository.deleteRecord(tableFilePath, 0));

        List<Object> result = dataRepository.readRecord(tableFilePath, 0, 0);
        assertEquals(200, result.get(0));
        assertEquals("Alice", result.get(1));
    }

    @Test
    public void testDeleteRecord_MiddleRecord_Success() throws Exception {
        for (int i = 0; i < 5; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }

        dataRepository.deleteRecord(tableFilePath, 2);

        List<Object> result1 = dataRepository.readRecord(tableFilePath, 0, 0);
        assertEquals(0, result1.get(0));

        List<Object> result2 = dataRepository.readRecord(tableFilePath, 1, 0);
        assertEquals(1, result2.get(0));

        List<Object> result3 = dataRepository.readRecord(tableFilePath, 2, 0);
        assertEquals(3, result3.get(0));

        List<Object> result4 = dataRepository.readRecord(tableFilePath, 3, 0);
        assertEquals(4, result4.get(0));
    }

    @Test
    public void testDeleteRecord_LastRecord_Success() throws Exception {
        for (int i = 0; i < 3; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }

        dataRepository.deleteRecord(tableFilePath, 2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dataRepository.readRecord(tableFilePath, 2, 0));

        assertTrue(exception.getMessage().contains("Invalid record index"));
    }

    @Test
    public void testDeleteRecord_FromSecondPage_Success() throws Exception {
        for (int i = 0; i < 40; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }

        dataRepository.addRecord(tableFilePath, Arrays.asList(40, "User40"));
        assertDoesNotThrow(() -> dataRepository.deleteRecord(tableFilePath, 40));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dataRepository.readRecord(tableFilePath, 40, 0));

        assertTrue(exception.getMessage().contains("Invalid record index"));
    }

    @Test
    public void testDeleteRecord_IndexTooLow_Exception() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(100, "John"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dataRepository.deleteRecord(tableFilePath, -1));

        assertTrue(exception.getMessage().contains("Invalid record index"));
    }

    @Test
    public void testDeleteRecord_OnSecondPage_Success() throws Exception {
        for (int i = 0; i < 40; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }
        dataRepository.addRecord(tableFilePath, Arrays.asList(40, "User40"));

        List<Object> result = dataRepository.readRecord(tableFilePath, 40, 0);
        assertEquals(40, result.get(0));
        assertEquals("User40", result.get(1));

        assertDoesNotThrow(() -> dataRepository.deleteRecord(tableFilePath, 40));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dataRepository.readRecord(tableFilePath, 40, 0));

        assertTrue(exception.getMessage().contains("Invalid record index"));
    }

    @Test
    public void testDeleteRecord_UpdatesTotalRecordCountOnSecondPage() throws Exception {
        for (int i = 0; i < 40; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }
        dataRepository.addRecord(tableFilePath, Arrays.asList(40, "User40")); // Вторая страница
        dataRepository.deleteRecord(tableFilePath, 40);

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "r")) {
            file.seek(50);
            int recordCountInThisPage = file.readInt();
            int totalRecords = file.readInt();

            assertEquals(40, recordCountInThisPage);
            assertEquals(40, totalRecords);
        }
    }

    @Test
    public void testDeleteRecord_FileStructureIntegrityAfterDeletion() throws Exception {
        dataRepository.addRecord(tableFilePath, Arrays.asList(100, "John"));
        dataRepository.addRecord(tableFilePath, Arrays.asList(200, "Alice"));

        dataRepository.deleteRecord(tableFilePath, 1);

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "r")) {
            file.seek(50);
            int recordCountInThisPage = file.readInt();
            int totalRecords = file.readInt();

            assertEquals(1, recordCountInThisPage);
            assertEquals(1, totalRecords);
        }
    }

    @Test
    public void testGenerateNextTablePartPath_FirstPart() throws Exception {
        DataRepositoryImpl repository = new DataRepositoryImpl();
        Method method = DataRepositoryImpl.class.getDeclaredMethod("generateNextTablePartPath", String.class);
        method.setAccessible(true);

        String currentPath = "test_table.txt";
        String nextPath = (String) method.invoke(repository, currentPath);
        assertEquals("test_table_part1.txt", new File(nextPath).getName());
    }

    @Test
    void testDeleteWithInvalidPageChain() throws IOException {
        try (RandomAccessFile mainFile = new RandomAccessFile(tableFilePath, "rw")) {
            mainFile.setLength(258);
            mainFile.seek(50);
            mainFile.writeInt(1);

            mainFile.seek(258 - 8);
            mainFile.write("bad_path".getBytes(StandardCharsets.UTF_8));
        }

        assertThrows(IOException.class, () ->
                dataRepository.deleteRecord(tableFilePath, 1));
    }

    @Test
    void testCalculateRecordSize_IntAndStr10() throws Exception {

        List<String> schema = Arrays.asList("int", "str_10");
        List<Object> data = Arrays.asList(123, "John");

        Method method = DataRepositoryImpl.class.getDeclaredMethod("calculateRecordSize", List.class, List.class);
        method.setAccessible(true);

        int size = (int) method.invoke(dataRepository, schema, data);

        assertEquals(4 + 4 + 10, size); // 4 байта на int, 4+10 на str_10
    }

    @Test
    void testDeleteRecord_OnSecondPage_ShouldUpdateCounters() throws Exception {
        List<String> schema = Arrays.asList("int", "str_10");


        int recordSize = 4 + 10;
        int maxRecordsOnPage = (65536 - 258) / (recordSize + 8);

        for (int i = 0; i <= maxRecordsOnPage; i++) {
            dataRepository.addRecord(tableFilePath, Arrays.asList(i, "User" + i));
        }

        dataRepository.addRecord(tableFilePath, Arrays.asList(maxRecordsOnPage + 1, "FinalUser"));

        Path secondPagePath = Paths.get(tableFilePath.replace(".txt", "_part1.txt"));
        assertTrue(Files.exists(secondPagePath), "Файл второй части должен быть создан");

        assertDoesNotThrow(() -> dataRepository.deleteRecord(tableFilePath, maxRecordsOnPage),
                "Запись должна успешно удалиться");

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "r")) {
            file.seek(50);
            int updatedRecordsInThisPage = file.readInt();
            file.seek(54);
            int updatedTotalRecords = file.readInt();

            assertEquals(maxRecordsOnPage, updatedRecordsInThisPage,
                    "Количество записей на первой странице не должно измениться");
            assertEquals(maxRecordsOnPage , updatedTotalRecords - 1,
                    "Общее количество записей должно уменьшиться на 1");
        }

        try (RandomAccessFile secondPageFile = new RandomAccessFile(secondPagePath.toString(), "r")) {
            secondPageFile.seek(50);
            int recordsInSecondPage = secondPageFile.readInt();
            assertEquals(1154, recordsInSecondPage - 1,
                    "После удаления единственной записи вторая страница должна быть пуста");
        }
    }

    /* ------------------------------- Проверка наших дополнительных методов -----------------------------*/
    @Nested
    class DataRepositoryConditionTest {

        private DataRepositoryImpl dataRepository;
        private Path testDir;
        private String tablePath;

        private void initThreeColumnTable() throws IOException {

            tablePath = testDir.resolve("condition_test_table.txt").toString();
            dataRepository.createTableFile(tablePath, "condition_test_table",
                    Arrays.asList("int", "str_20", "int"));

            dataRepository.addRecord(tablePath, Arrays.asList(1, "Alice", 25));
            dataRepository.addRecord(tablePath, Arrays.asList(2, "Bob", 30));
            dataRepository.addRecord(tablePath, Arrays.asList(3, "Charlie", 25));
            dataRepository.addRecord(tablePath, Arrays.asList(4, "David", 35));
            dataRepository.addRecord(tablePath, Arrays.asList(5, "Eve", 30));
        }

        private boolean invokeCheckConditionWithConstant(DataRepositoryImpl repository, Object columnValue,
                                                         String operator,
                                                         Object constant) throws Exception {

            Method method = DataRepositoryImpl.class.getDeclaredMethod(
                    "checkConditionWithConstant", Object.class, String.class, Object.class);
            method.setAccessible(true);
            return (boolean) method.invoke(repository, columnValue, operator, constant);
        }

        @BeforeEach
        void setUp(@TempDir Path tempDir) throws IOException {
            dataRepository = new DataRepositoryImpl();
            testDir = tempDir;

            tableFilePath = testDir.resolve("test_table.txt").toString();
            dataRepository.createTableFile(tableFilePath, "test_table",
                    Arrays.asList("int", "str_20"));

            dbFilePath = testDir.resolve("Master.txt").toString();
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

        @Test
        void testFindRecordsByCondition_CrossColumn() throws IOException {
            initThreeColumnTable();

            dataRepository.addRecord(tablePath, Arrays.asList(25, "Test", 25));

            List<Integer> result = dataRepository.findRecordsByCondition(tablePath, 2, "==", 0);
            assertEquals(List.of(5), result);
        }

        @Test
        void testFindRecordsByCondition_InvalidColumn() throws IOException {
            initThreeColumnTable();

            assertThrows(IllegalArgumentException.class, () -> {
                dataRepository.findRecordsByCondition(tablePath, 5, "==", 0);
            });
        }

        @Test
        void testFindRecordsByConstant_IntComparison() throws IOException {
            initThreeColumnTable();


            List<Integer> result = dataRepository.findRecordsByConstant(tablePath, 2, ">", String.valueOf(30));
            assertEquals(List.of(3), result); // David(35)
        }

        @Test
        void testFindRecordsByConstant_StringComparison() throws IOException {
            initThreeColumnTable();

            List<Integer> result = dataRepository.findRecordsByConstant(tablePath, 1, "==", "Alice");
            assertEquals(List.of(0), result);
        }

        @Test
        void testFindRecordsByCondition_InvalidOperator() throws IOException {
            initThreeColumnTable();

            assertThrows(IllegalArgumentException.class, () -> {
                dataRepository.findRecordsByCondition(tablePath, 0, "like", 1);
            });
        }

        @Test
        void testFindRecordsByCondition_EmptyTable() throws IOException {
            tablePath = testDir.resolve("empty_table.txt").toString();
            dataRepository.createTableFile(tablePath, "empty_table",
                    Collections.singletonList("int"));

            List<Integer> result = dataRepository.findRecordsByCondition(
                    tablePath, 0, ">", 0);
            assertTrue(result.isEmpty());
        }

        @Nested
        class StringComparisonTests {
            @BeforeEach
            void initStringTable() throws IOException {
                tablePath = testDir.resolve("str_comparison_table.txt").toString();
                dataRepository.createTableFile(tablePath, "str_comparison",
                        Arrays.asList("str_20", "str_20"));

                dataRepository.addRecord(tablePath, Arrays.asList("apple", "banana"));
                dataRepository.addRecord(tablePath, Arrays.asList("banana", "apple"));
                dataRepository.addRecord(tablePath, Arrays.asList("cherry", "cherry"));
                dataRepository.addRecord(tablePath, Arrays.asList("date", "date"));
                dataRepository.addRecord(tablePath, Arrays.asList("apple", "date"));
            }

            @Test
            void testStringGreaterThan() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, ">", 1);
                assertEquals(List.of(1), result);
            }

            @Test
            void testStringLessThan() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, "<", 1);
                assertEquals(List.of(0, 4), result);
            }

            @Test
            void testStringGreaterOrEqual() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, ">=", 1);
                assertEquals(List.of(1, 2, 3), result);
            }

            @Test
            void testStringLessOrEqual() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, "<=", 1);
                assertEquals(List.of(0, 2, 3, 4), result);
            }

            @Test
            void testStringEqual() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, "==", 1);
                assertEquals(List.of(2, 3), result);
            }

            @Test
            void testStringNotEqual() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, "!=", 1);
                assertEquals(List.of(0, 1, 4), result);
            }

            @Test
            void shouldThrowExceptionForUnsupportedOperator() throws IOException {
                tablePath = testDir.resolve("operator_test.txt").toString();
                dataRepository.createTableFile(tablePath, "operator_test",
                        Arrays.asList("str_20", "str_20"));
                dataRepository.addRecord(tablePath, Arrays.asList("test", "test"));

                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    dataRepository.findRecordsByCondition(tablePath, 0, "invalid_op", 1);
                });

                assertTrue(exception.getMessage().contains("Unsupported operator"));
            }
        }

        @Nested
        class IntegerComparisonTests {
            @BeforeEach
            void initIntTable() throws IOException {
                tablePath = testDir.resolve("int_comparison_table.txt").toString();
                dataRepository.createTableFile(tablePath, "int_comparison",
                        Arrays.asList("int", "int"));

                // Колонка 0: [5, 3, 4, 4, 2]
                // Колонка 1: [3, 3, 2, 5, 2]
                dataRepository.addRecord(tablePath, Arrays.asList(5, 3));
                dataRepository.addRecord(tablePath, Arrays.asList(3, 3));
                dataRepository.addRecord(tablePath, Arrays.asList(4, 2));
                dataRepository.addRecord(tablePath, Arrays.asList(4, 5));
                dataRepository.addRecord(tablePath, Arrays.asList(2, 2));
            }

            @Test
            void testGreaterThan() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, ">", 1);
                assertEquals(List.of(0, 2), result); // 5>3 и 4>2
            }

            @Test
            void testLessThan() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, "<", 1);
                assertEquals(List.of(3), result); // 4<5
            }

            @Test
            void testGreaterOrEqual() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, ">=", 1);
                assertEquals(List.of(0, 1, 2, 4), result); // 5≥3, 3≥3, 4≥2, 2≥2
            }

            @Test
            void testLessOrEqual() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, "<=", 1);
                assertEquals(List.of(1, 3, 4), result); // 3≤3, 4≤5, 2≤2
            }

            @Test
            void testEqual() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, "==", 1);
                assertEquals(List.of(1, 4), result); // 3==3 и 2==2
            }

            @Test
            void testNotEqual() throws IOException {
                List<Integer> result = dataRepository.findRecordsByCondition(
                        tablePath, 0, "!=", 1);
                assertEquals(List.of(0, 2, 3), result); // 5≠3, 4≠2, 4≠5
            }
        }

        @Test
        void testCompareDifferentTypes_throwsException() throws IOException {
            initThreeColumnTable();

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                dataRepository.findRecordsByCondition(tablePath, 0, "==", 1);
            });

            String message = exception.getMessage();
            assertNotNull(message, "Сообщение об ошибке не должно быть null");
            assertTrue(message.contains("Cannot compare different types"),
                    "Сообщение должно содержать 'Cannot compare different types'. Получено: " + message);
            assertTrue(message.contains("Integer") && message.contains("String"),
                    "Сообщение должно содержать типы Integer и String. Получено: " + message);
        }

        @Test
        void testFindRecordsByConstant_IntGreaterThan() throws IOException {
            tablePath = testDir.resolve("int_constant_table.txt").toString();
            dataRepository.createTableFile(tablePath, "int_constant_table", List.of("int"));

            dataRepository.addRecord(tablePath, List.of(10));
            dataRepository.addRecord(tablePath, List.of(20));
            dataRepository.addRecord(tablePath, List.of(30));

            List<Integer> result = dataRepository.findRecordsByConstant(tablePath, 0, ">", String.valueOf(15));
            assertEquals(List.of(1, 2), result); // 20 и 30 > 15
        }

        @Test
        void testFindRecordsByConstant_StringEqual() throws IOException {
            tablePath = testDir.resolve("str_constant_table.txt").toString();
            dataRepository.createTableFile(tablePath, "str_constant_table", List.of("str_20"));

            dataRepository.addRecord(tablePath, List.of("apple"));
            dataRepository.addRecord(tablePath, List.of("banana"));
            dataRepository.addRecord(tablePath, List.of("apple"));

            List<Integer> result = dataRepository.findRecordsByConstant(tablePath, 0, "==", "apple");

            assertEquals(List.of(0, 2), result);
        }

        @Test
        void testFindRecordsByConstant_InvalidColumnIndex() throws IOException {
            tablePath = testDir.resolve("invalid_column_table.txt").toString();
            dataRepository.createTableFile(tablePath, "invalid_column_table", List.of("int"));

            assertThrows(IllegalArgumentException.class, () -> {
                dataRepository.findRecordsByConstant(tablePath, 5, "==", String.valueOf(10));
            });
        }

        @Test
        void testFindRecordsByConstant_UnsupportedOperator() throws IOException {
            tablePath = testDir.resolve("unsupported_op_table.txt").toString();
            dataRepository.createTableFile(tablePath, "unsupported_op_table", List.of("int"));

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                dataRepository.findRecordsByConstant(tablePath, 0, "like", String.valueOf(10));
            });

            assertTrue(exception.getMessage().contains("Unsupported operator"));
        }

        @Nested
        class IntegerComparisonTest {
            private DataRepositoryImpl repository;

            @BeforeEach
            void setUp() {
                repository = new DataRepositoryImpl();
            }

            @Test
            void testGreaterThan() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, 10, ">", 5));
                assertFalse(invokeCheckConditionWithConstant(repository, 5, ">", 10));
            }

            @Test
            void testLessThan() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, 5, "<", 10));
                assertFalse(invokeCheckConditionWithConstant(repository, 10, "<", 5));
            }

            @Test
            void testGreaterOrEqual() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, 10, ">=", 5));
                assertTrue(invokeCheckConditionWithConstant(repository, 10, ">=", 10));
                assertFalse(invokeCheckConditionWithConstant(repository, 5, ">=", 10));
            }

            @Test
            void testLessOrEqual() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, 5, "<=", 10));
                assertTrue(invokeCheckConditionWithConstant(repository, 10, "<=", 10));
                assertFalse(invokeCheckConditionWithConstant(repository, 10, "<=", 5));
            }

            @Test
            void testEqual() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, 10, "==", 10));
                assertFalse(invokeCheckConditionWithConstant(repository, 10, "==", 5));
            }

            @Test
            void testNotEqual() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, 10, "!=", 5));
                assertFalse(invokeCheckConditionWithConstant(repository, 10, "!=", 10));
            }

            @Test
            void testUnsupportedOperator() {
                Exception exception = assertThrows(InvocationTargetException.class, () ->
                        invokeCheckConditionWithConstant(repository, 10, "like", 5));

                Throwable cause = exception.getCause();
                assertNotNull(cause);
                assertEquals(AssertionError.class, cause.getClass());
                assertTrue(cause.getMessage().contains("Unsupported operator: like"));
            }

        }

        @Nested
        class StringComparisonTest {
            private DataRepositoryImpl repository;

            @BeforeEach
            void setUp() {
                repository = new DataRepositoryImpl();
            }

            @Test
            void testGreaterThan() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, "banana", ">", "apple"));
                assertFalse(invokeCheckConditionWithConstant(repository, "apple", ">", "banana"));
            }

            @Test
            void testLessThan() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, "apple", "<", "banana"));
                assertFalse(invokeCheckConditionWithConstant(repository, "banana", "<", "apple"));
            }

            @Test
            void testGreaterOrEqual() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, "banana", ">=", "apple"));
                assertTrue(invokeCheckConditionWithConstant(repository, "apple", ">=", "apple"));
                assertFalse(invokeCheckConditionWithConstant(repository, "apple", ">=", "banana"));
            }

            @Test
            void testLessOrEqual() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, "apple", "<=", "banana"));
                assertTrue(invokeCheckConditionWithConstant(repository, "apple", "<=", "apple"));
                assertFalse(invokeCheckConditionWithConstant(repository, "banana", "<=", "apple"));
            }

            @Test
            void testEqual() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, "apple", "==", "apple"));
                assertFalse(invokeCheckConditionWithConstant(repository, "apple", "==", "banana"));
            }

            @Test
            void testNotEqual() throws Exception {
                assertTrue(invokeCheckConditionWithConstant(repository, "apple", "!=", "banana"));
                assertFalse(invokeCheckConditionWithConstant(repository, "apple", "!=", "apple"));
            }

            @Test
            void testUnsupportedOperator() throws Exception {
                InvocationTargetException exception = assertThrows(
                        InvocationTargetException.class,
                        () -> invokeCheckConditionWithConstant(repository, "apple", "like", "a")
                );

                Throwable cause = exception.getCause();
                assertNotNull(cause);
                assertEquals(AssertionError.class, cause.getClass());
                assertTrue(cause.getMessage().contains("Unsupported operator: like"));
            }
        }

        @Nested
        class MixedTypeComparisonTests {
            private DataRepositoryImpl repository;

            @BeforeEach
            void setUp() {
                repository = new DataRepositoryImpl();
            }

            @Test
            void testIntVsString() {
                InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                        invokeCheckConditionWithConstant(repository, 10, "==", "10"));

                Throwable cause = exception.getCause();
                assertNotNull(cause);
                assertEquals(IllegalArgumentException.class, cause.getClass());
                assertTrue(cause.getMessage().contains("Cannot compare Integer with String"));
            }

            @Test
            void testStringVsInt() {
                InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                        invokeCheckConditionWithConstant(repository, "10", "==", 10));

                Throwable cause = exception.getCause();
                assertNotNull(cause);
                assertEquals(IllegalArgumentException.class, cause.getClass());
                assertTrue(cause.getMessage().contains("Cannot compare String with Integer"));
            }

            @Test
            void testDoubleVsString() {
                InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                        invokeCheckConditionWithConstant(repository, 10.5, "==", "10.5"));

                Throwable cause = exception.getCause();
                assertNotNull(cause);
                assertEquals(IllegalArgumentException.class, cause.getClass());
                assertTrue(cause.getMessage().contains("Cannot compare Double with String"));
            }
        }

        @Nested
        class FindRecordsByPatternTests {
            private DataRepositoryImpl dataRepository;
            private Path testDir;
            private String tablePath;

            @BeforeEach
            void setUp(@TempDir Path tempDir) throws IOException {
                dataRepository = new DataRepositoryImpl();
                testDir = tempDir;
                tablePath = testDir.resolve("pattern_table.txt").toString();
                dataRepository.createTableFile(tablePath, "pattern_table",
                        Arrays.asList("str_20", "int"));

                dataRepository.addRecord(tablePath, Arrays.asList("apple", 1));
                dataRepository.addRecord(tablePath, Arrays.asList("banana", 2));
                dataRepository.addRecord(tablePath, Arrays.asList("applesauce", 3));
                dataRepository.addRecord(tablePath, Arrays.asList("grape", 4));
                dataRepository.addRecord(tablePath, Arrays.asList("Apple", 5));
            }

            @Test
            void testFindExactMatch() throws IOException {
                List<Integer> result = dataRepository.findRecordsByPattern(tablePath, 0, "apple", true);
                assertEquals(List.of(0), result);
            }

            @Test
            void testWildcardPercent() throws IOException {
                List<Integer> result = dataRepository.findRecordsByPattern(tablePath, 0, "apple%", true);
                assertEquals(List.of(0, 2), result); // "apple" и "applesauce"
            }

            @Test
            void testWildcardUnderscore() throws IOException {
                List<Integer> result = dataRepository.findRecordsByPattern(tablePath, 0, "gr_pe", true);
                assertEquals(List.of(3), result); // "grape"
            }

            @Test
            void testCaseInsensitiveMatch() throws IOException {
                List<Integer> result = dataRepository.findRecordsByPattern(tablePath, 0, "apple", false);
                assertEquals(List.of(0, 4), result); // "apple" и "Apple"
            }

            @Test
            void testNoMatch() throws IOException {
                List<Integer> result = dataRepository.findRecordsByPattern(tablePath, 0, "orange", true);
                assertTrue(result.isEmpty());
            }

            @Test
            void testInvalidColumnType() {
                assertThrows(IllegalArgumentException.class, () -> {
                    dataRepository.findRecordsByPattern(tablePath, 1, "123", true);
                });
            }

            @Test
            void testInvalidColumnIndex() {
                assertThrows(IllegalArgumentException.class, () -> {
                    dataRepository.findRecordsByPattern(tablePath, 2, "invalid", true);
                });
            }

            @Test
            void testEmptyTable() throws IOException {
                String emptyTablePath = testDir.resolve("empty_pattern_table.txt").toString();
                dataRepository.createTableFile(emptyTablePath, "empty_pattern_table",
                        Collections.singletonList("str_20"));
                List<Integer> result = dataRepository.findRecordsByPattern(emptyTablePath, 0, "%a%",
                        true);
                assertTrue(result.isEmpty());
            }
        }
        @Nested
        class GetAllRecordIndicesTest {
            private DataRepositoryImpl dataRepository;
            private String tablePath;

            @BeforeEach
            void setUp(@TempDir Path tempDir) {
                dataRepository = new DataRepositoryImpl();
                tablePath = tempDir.resolve("record_indices_table.txt").toString();
            }

            @Test
            void testGetAllRecordIndices_WithMultipleRecords() throws IOException {
                dataRepository.createTableFile(tablePath, "record_indices", Arrays.asList("int", "str_20"));
                dataRepository.addRecord(tablePath, Arrays.asList(10, "Alice"));
                dataRepository.addRecord(tablePath, Arrays.asList(20, "Bob"));
                dataRepository.addRecord(tablePath, Arrays.asList(30, "Charlie"));
                List<Integer> indices = dataRepository.getAllRecordIndices(tablePath);

                assertEquals(Arrays.asList(0, 1, 2), indices);
            }

            @Test
            void testGetAllRecordIndices_EmptyTable() throws IOException {
                dataRepository.createTableFile(tablePath, "empty_table", Collections.singletonList("int"));

                List<Integer> indices = dataRepository.getAllRecordIndices(tablePath);
                assertTrue(indices.isEmpty());
            }

            @Test
            void testGetAllRecordIndices_SingleRecord() throws IOException {
                dataRepository.createTableFile(tablePath, "single_record", Collections.singletonList("int"));
                dataRepository.addRecord(tablePath, Arrays.asList(1));

                List<Integer> indices = dataRepository.getAllRecordIndices(tablePath);

                assertEquals(Collections.singletonList(0), indices);
            }

            @Test
            void testGetAllRecordIndices_CorrectIndexRange() throws IOException {
                dataRepository.createTableFile(tablePath, "range_table", Collections.singletonList("int"));

                for (int i = 0; i < 10; i++) {
                    dataRepository.addRecord(tablePath, List.of(i));
                }
                List<Integer> indices = dataRepository.getAllRecordIndices(tablePath);

                List<Integer> expected = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    expected.add(i);
                }
                assertEquals(expected, indices);
            }
        }
    }

    @Test
    void getAllTableNames_ShouldReturnTablesFromMasterDB() throws IOException {
        Path tablePathObj = Paths.get(tableFilePath); // из setUp — это test_table.txt
        Path masterDbPath = tablePathObj.getParent().resolve("Master.txt");

        assertTrue(Files.exists(masterDbPath), "Master DB должна существовать");
        String secondTablePath = testDir.resolve("second_table.txt").toString();
        dataRepository.createTableFile(secondTablePath, "second_table", List.of("int"));
        List<String> tableNames = dataRepository.getAllTableNames(masterDbPath.toString());

        assertEquals(2, tableNames.size());
        assertTrue(tableNames.contains("test_table"));
        assertTrue(tableNames.contains("second_table"));
    }

    @Test
    void getAllTableNames_ShouldHandleEmptyMasterDB() throws IOException {
        String emptyDbPath = testDir.resolve("EmptyDB.txt").toString();
        try (RandomAccessFile dbFile = new RandomAccessFile(emptyDbPath, "rw")) {
            byte[] name = new byte[50];
            dbFile.write(name);
            dbFile.writeInt(0); // 0 таблиц
        }

        List<String> tableNames = dataRepository.getAllTableNames(emptyDbPath);
        assertTrue(tableNames.isEmpty(), "Для пустой БД должен вернуть пустой список");
    }

    @Test
    void getAllTableNames_ShouldThrowForCorruptedMasterDB() throws IOException {
        String corruptedDbPath = testDir.resolve("CorruptedDB.txt").toString();
        try (RandomAccessFile dbFile = new RandomAccessFile(corruptedDbPath, "rw")) {
            byte[] name = new byte[50];
            dbFile.write(name);
            dbFile.writeInt(100);
        }

        assertThrows(IOException.class, () ->
                        dataRepository.getAllTableNames(corruptedDbPath),
                "Должен бросить IOException для поврежденной БД"
        );
    }

    @Test
    void getAllTableNames_ShouldHandleDifferentPathFormats() throws IOException {
        String dbPath = testDir.resolve("PathFormatDB.txt").toString();

        try (RandomAccessFile dbFile = new RandomAccessFile(dbPath, "rw")) {
            byte[] name = new byte[50];
            dbFile.write(name);
            dbFile.writeInt(2); // 2 таблицы

            String relPath = "./rel_table.txt";
            byte[] relRecord = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
            System.arraycopy(relPath.getBytes(), 0, relRecord, 0, relPath.length());
            dbFile.write(relRecord);

            String absPath = testDir.resolve("abs_table.txt").toString();
            byte[] absRecord = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
            System.arraycopy(absPath.getBytes(), 0, absRecord, 0, absPath.length());
            dbFile.write(absRecord);
        }

        List<String> tableNames = dataRepository.getAllTableNames(dbPath);

        assertEquals(2, tableNames.size());
        assertTrue(tableNames.contains("rel_table"));
        assertTrue(tableNames.contains("abs_table"));
    }

    @Test
    void getAllTableNames_ShouldThrowForNonexistentDB() {
        String missingPath = testDir.resolve("nonexistent.txt").toString();
        assertThrows(FileNotFoundException.class, () ->
                        dataRepository.getAllTableNames(missingPath),
                "Должен бросить FileNotFoundException для несуществующего файла"
        );
    }

    @Test
    void getAllTableNames_ShouldHandleTablesWithoutTxtExtension() throws IOException {
        String dbPath = testDir.resolve("NoExtDB.txt").toString();

        try (RandomAccessFile dbFile = new RandomAccessFile(dbPath, "rw")) {
            byte[] name = new byte[50];
            dbFile.write(name);
            dbFile.writeInt(1); // 1 таблица

            String tablePath = testDir.resolve("table_without_ext").toString();
            byte[] tableRecord = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
            System.arraycopy(tablePath.getBytes(), 0, tableRecord, 0, tablePath.length());
            dbFile.write(tableRecord);
        }
        List<String> tableNames = dataRepository.getAllTableNames(dbPath);

        assertEquals(1, tableNames.size());
        assertEquals("table_without_ext", tableNames.get(0));
    }
}

