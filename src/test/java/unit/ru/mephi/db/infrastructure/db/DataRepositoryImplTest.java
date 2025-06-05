package unit.ru.mephi.db.infrastructure.db;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataRepositoryImplTest {

    private DataRepositoryImpl dataRepository;
    private Path testDir;
    private String dbFilePath;
    private String tableFilePath;
    private String tableFilePath2;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        dataRepository = new DataRepositoryImpl();
        testDir = tempDir;
        dbFilePath = testDir.resolve("test_db.txt").toString();
        tableFilePath = testDir.resolve("test_table.txt").toString();
        tableFilePath2 = testDir.resolve("table_part2.txt").toString();

        // Создаем тестовую БД и таблицу
        dataRepository.createDatabaseFile(dbFilePath, "test_db");
        dataRepository.createTableFile(tableFilePath, "test_table",
                Arrays.asList("int", "str_20"));
        dataRepository.addTableReference(dbFilePath, tableFilePath);

        dataRepository.createTableFile(tableFilePath2, "test_table_part2",
                Arrays.asList("int", "str_50"));
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

    @Test
    void createDatabaseFile_ShouldCreateParentDirectories() throws IOException {
        Path nestedPath = testDir.resolve("nested/dir/test_db.txt");
        dataRepository.createDatabaseFile(nestedPath.toString(), "TestDB");

        assertTrue(Files.exists(nestedPath));
        assertTrue(Files.isDirectory(nestedPath.getParent()));
    }

    @Test
    void createDatabaseFile_ShouldCreateValidFile() throws IOException {
        dataRepository.createDatabaseFile(dbFilePath, "TestDB");

        assertTrue(Files.exists(Path.of(dbFilePath)));
        assertEquals(154, Files.size(Path.of(dbFilePath))); // 50 (имя) + 4 (количество) + 100 (указатель)
    }

    @Test
    void createDatabaseFile_ShouldThrowForLongName() {
        String longName = "A".repeat(51);

        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.createDatabaseFile(dbFilePath, longName));
    }

    @Test
    void createTableFile_ShouldAccept50CharName() {
        String exactLengthName = "a".repeat(50);
        assertDoesNotThrow(
                () -> dataRepository.createTableFile(testDir.resolve("test.txt").toString(), exactLengthName, List.of("int"))
        );
    }

    @Test
    void createTableFile_ShouldCreateParentDirectories() throws IOException {
        Path nestedPath = testDir.resolve("nested/dir/test_table.txt");
        dataRepository.createTableFile(nestedPath.toString(), "TestTable", List.of("int"));

        assertTrue(Files.exists(nestedPath));
        assertTrue(Files.isDirectory(nestedPath.getParent()));
    }

    @Test
    void createDatabaseFile_ShouldThrowForInvalidExtension() {
        String invalidPath = dbFilePath.replace(".txt", ".dat");

        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.createDatabaseFile(invalidPath, "TestDB"));
    }


    @Test
    void createTableFile_ShouldCreateValidFile() throws IOException {
        List<String> schema = Arrays.asList("int", "str_20", "int");

        dataRepository.createTableFile(tableFilePath, "TestTable", schema);

        assertTrue(Files.exists(Path.of(tableFilePath)));
        assertTrue(Files.size(Path.of(tableFilePath)) > 0);
    }

    @Test
    void createTableFile_ShouldThrowForInvalidSchema() {
        List<String> invalidSchema = List.of("invalid_type");

        assertThrows(IllegalArgumentException.class,
                () -> dataRepository.createTableFile(tableFilePath, "TestTable", invalidSchema));
    }

    @Test
    void isTableExists_ShouldReturnFalseForEmptyDB() throws IOException {
        dataRepository.createDatabaseFile(dbFilePath, "TestDB");
        assertFalse(dataRepository.isTableExists(dbFilePath, tableFilePath));
    }

    @Test
    void isTableExists_ShouldReturnFalseForNonExistingTable() throws IOException {
        dataRepository.createDatabaseFile(dbFilePath, "TestDB");
        dataRepository.createTableFile(tableFilePath, "TestTable", List.of("int"));
        dataRepository.addTableReference(dbFilePath, tableFilePath);

        String nonExistingPath = tableFilePath.replace("table", "non existing");

        assertFalse(dataRepository.isTableExists(dbFilePath, nonExistingPath));
    }

    @Test
    void validateSchema_ShouldThrowForNullSchema() throws Exception {
        assertPrivateValidateSchemaThrows(
                null,
                "Schema must contain 1-20 fields"
        );
    }

    @Test
    void validateSchema_ShouldThrowForInvalidIntFormat1() throws Exception {
        assertPrivateValidateSchemaThrows(
                List.of("int1"),
                "Integer field must be 'int'"
        );
    }

    @Test
    void validateSchema_ShouldThrowForInvalidIntFormat2() throws Exception {
        assertPrivateValidateSchemaThrows(
                List.of("int_"),
                "Integer field must be 'int'"
        );
    }

    @Test
    void validateSchema_ShouldThrowForInvalidStringLength1() throws Exception {
        assertPrivateValidateSchemaThrows(
                List.of("str_0"),
                "String length must be 1-1000"
        );
    }

    @Test
    void validateSchema_ShouldThrowForInvalidStringLength2() throws Exception {
        assertPrivateValidateSchemaThrows(
                List.of("str_1001"),
                "String length must be 1-1000"
        );
    }

    @Test
    void validateSchema_ShouldAcceptValidSchema() throws Exception {
        callPrivateValidateSchema(Arrays.asList("int", "str_20", "str_1000"));
        // Если не будет исключения - тест пройден
    }

    private void callPrivateValidateSchema(List<String> schema) throws Exception {
        Method method = DataRepositoryImpl.class.getDeclaredMethod("validateSchema", List.class);
        method.setAccessible(true);
        method.invoke(dataRepository, schema);
    }

    private void assertPrivateValidateSchemaThrows(List<String> schema, String expectedMessage) throws Exception {
        Method method = DataRepositoryImpl.class.getDeclaredMethod("validateSchema", List.class);
        method.setAccessible(true);

        Exception exception = assertThrows(
                Exception.class,
                () -> method.invoke(dataRepository, schema)
        );

        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
        assertEquals(expectedMessage, exception.getCause().getMessage());
    }

    @Test
    void decodeSchema_ShouldCorrectlySplitSchemaString() throws Exception {
        byte[] input = "int;str_20;date".getBytes();

        Method method = DataRepositoryImpl.class.getDeclaredMethod("decodeSchema", byte[].class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(dataRepository, (Object) input);

        assertEquals(Arrays.asList("int", "str_20", "date"), result);
    }

    @Test
    void createTableFile_ShouldThrowWhenTableNameTooLong() {
        String longTableName = "a".repeat(51);
        String filePath = testDir.resolve("test.txt").toString();

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> dataRepository.createTableFile(filePath, longTableName, List.of("int"))
        );

        assertEquals("Table name must be 50 characters or less", exception.getMessage());
    }

    @Test
    void createTableFile_ShouldAcceptMaxLengthName() {
        String maxLengthName = "a".repeat(50);
        String filePath = testDir.resolve("test.txt").toString();

        assertDoesNotThrow(
                () -> dataRepository.createTableFile(filePath, maxLengthName, List.of("int"))
        );

        assertTrue(Files.exists(Path.of(filePath)));
    }


    /* ------------------------------- Удаление БД -----------------------------*/

    @Test
    @DisplayName("Удаление БД с таблицами")
    void testDeleteDatabaseWithTables() throws IOException {
        Assertions.assertTrue(Files.exists(Path.of(dbFilePath)));
        Assertions.assertTrue(Files.exists(Path.of(tableFilePath)));

        dataRepository.deleteDatabaseFile(dbFilePath);

        Assertions.assertFalse(Files.exists(Path.of(dbFilePath)));
        Assertions.assertFalse(Files.exists(Path.of(tableFilePath)));
    }

    @Test
    @DisplayName("Удаление таблицы с связанными частями")
    void testDeleteTableWithParts() throws IOException {
        // Связываем таблицы
        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "rw")) {
            file.seek(50 + 4 + DataRepositoryImpl.TABLE_SCHEMA_SIZE);
            file.write(tableFilePath2.getBytes());
        }

        dataRepository.deleteTableFile(tableFilePath);

        Assertions.assertFalse(Files.exists(Path.of(tableFilePath)));
        Assertions.assertFalse(Files.exists(Path.of(tableFilePath2)));
    }

    @Test
    @DisplayName("Попытка удаления несуществующей БД")
    void testDeleteNonExistentDatabase() {
        String nonExistPath = testDir.resolve("non_exist.txt").toString();
        Assertions.assertThrows(IOException.class, () ->
                dataRepository.deleteDatabaseFile(nonExistPath));
    }

    @Test
    @DisplayName("Попытка удаления несуществующей таблицы")
    void testDeleteNonExistentTable() {
        String nonExistPath = testDir.resolve("non_exist.txt").toString();
        Assertions.assertThrows(IOException.class, () ->
                dataRepository.deleteTableFile(nonExistPath));
    }

    @Test
    @DisplayName("Удаление ссылки на таблицу - проверка фильтрации таблиц")
    void removeTableReference_ShouldFilterCorrectly() throws IOException {
        String table2Path = testDir.resolve("table2.txt").toString();
        dataRepository.createTableFile(table2Path, "table2", List.of("int"));
        dataRepository.addTableReference(dbFilePath, table2Path);

        // Удаляем ссылку на первую таблицу
        dataRepository.removeTableReference(dbFilePath, tableFilePath);

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "r")) {
            file.seek(50);
            int tableCount = file.readInt();
            assertEquals(1, tableCount);

            byte[] pointerBytes = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
            file.seek(DataRepositoryImpl.DB_HEADER_SIZE);
            file.readFully(pointerBytes);
            String remainingPath = new String(pointerBytes, StandardCharsets.UTF_8).trim();

            assertEquals(table2Path, remainingPath);
        }
    }

    @Test
    @DisplayName("Удаление ссылки - проверка записи оставшихся таблиц")
    void removeTableReference_ShouldWriteRemainingTablesCorrectly() throws IOException {
        Path tempDbPath = testDir.resolve("temp_db.txt");
        dataRepository.createDatabaseFile(tempDbPath.toString(), "temp_db");

        List<String> tables = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String path = testDir.resolve("table" + i + ".txt").toString();
            dataRepository.createTableFile(path, "table" + i, List.of("int"));
            dataRepository.addTableReference(tempDbPath.toString(), path);
            tables.add(path);
        }

        try (RandomAccessFile file = new RandomAccessFile(tempDbPath.toFile(), "r")) {
            file.seek(50);
            assertEquals(3, file.readInt());
        }

        // Удаляем среднюю таблицу
        dataRepository.removeTableReference(tempDbPath.toString(), tables.get(1));

        try (RandomAccessFile file = new RandomAccessFile(tempDbPath.toFile(), "r")) {
            file.seek(50);
            int tableCount = file.readInt();
            assertEquals(2, tableCount);

            file.seek(DataRepositoryImpl.DB_HEADER_SIZE);
            byte[] pointer1 = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
            file.readFully(pointer1);
            assertEquals(tables.get(0), new String(pointer1, StandardCharsets.UTF_8).trim());

            file.seek(DataRepositoryImpl.DB_HEADER_SIZE + DataRepositoryImpl.DB_POINTER_SIZE);
            byte[] pointer2 = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
            file.readFully(pointer2);
            assertEquals(tables.get(2), new String(pointer2, StandardCharsets.UTF_8).trim());
        }

        Files.deleteIfExists(tempDbPath);
        tables.forEach(path -> {
            try {
                Files.deleteIfExists(Path.of(path));
            } catch (IOException e) {
                System.err.println("Failed to delete " + path + ": " + e.getMessage());
            }
        });
    }

}