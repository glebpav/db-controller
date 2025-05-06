package ru.mephi.db.infrastructure.db;

import ru.mephi.db.application.adapter.db.DataRepository;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataRepositoryImpl implements DataRepository {

    /** Размер заголовка файла базы данных (50 байт для имени + 4 байта для количества таблиц) */
    private static final int DB_HEADER_SIZE = 50 + 4;
    /** Размер указателя на таблицу в файле базы данных */
    private static final int DB_POINTER_SIZE = 100;

    /** Размер заголовка файла таблицы (50 байт для имени + 4 байта для количества записей + 100 байт для указателя) */
    private static final int TABLE_HEADER_SIZE = 50 + 4 + 100;
    /** Размер указателя на следующую часть таблицы */
    private static final int TABLE_POINTER_SIZE = 100;

    /** Размер блока схемы таблицы в заголовке */
    private static final int TABLE_SCHEMA_SIZE = 100;
    /** Максимальное количество полей в схеме */
    private static final int MAX_SCHEMA_FIELDS = 20;

    /**
     * Создает новый файл базы данных в формате TXT с указанным именем.
     *
     * @param dbFilePath абсолютный путь к создаваемому файлу базы данных (с расширением .txt)
     * @param dbName название базы данных (максимум 50 символов)
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws IllegalArgumentException если имя базы данных превышает 50 символов
     */
    @Override
    public void createDatabaseFile(String dbFilePath, String dbName) throws IOException {
        validateTxtExtension(dbFilePath);

        Path path = Paths.get(dbFilePath).getParent();
        if (path != null && !Files.exists(path)) {
            Files.createDirectories(path);
        }

        if (dbName.length() > 50) {
            throw new IllegalArgumentException("Database name must be 50 characters or less");
        }

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "rw")) {
            // Записываем заголовок
            byte[] nameBytes = dbName.getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 50));
            file.write(paddedName);

            // Количество таблиц (пока 0)
            file.writeInt(0);
        }
    }

    /**
     * Добавляет ссылку на таблицу в файл базы данных.
     *
     * @param dbFilePath путь к файлу базы данных (с расширением .txt)
     * @param tableFilePath путь к файлу таблицы (с расширением .txt), который нужно добавить
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws IllegalArgumentException если таблица уже существует в БД
     */
    @Override
    public void addTableReference(String dbFilePath, String tableFilePath) throws IOException {
        validateTxtExtension(dbFilePath);
        validateTxtExtension(tableFilePath);

        // Проверяем, что таблица еще не добавлена в БД
        if (isTableExists(dbFilePath, tableFilePath)) {
            throw new IllegalArgumentException("Table reference already exists in database: " + tableFilePath);
        }

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "rw")) {
            // Читаем текущее количество таблиц
            file.seek(50); // Пропускаем название БД
            int tableCount = file.readInt();

            // Увеличиваем счетчик таблиц
            file.seek(50);
            file.writeInt(tableCount + 1);

            // Записываем новую ссылку в конец
            file.seek(DB_HEADER_SIZE + (long) tableCount * DB_POINTER_SIZE);

            byte[] pointerBytes = tableFilePath.getBytes(StandardCharsets.UTF_8);
            byte[] paddedPointer = new byte[DB_POINTER_SIZE];
            System.arraycopy(pointerBytes, 0, paddedPointer, 0, Math.min(pointerBytes.length, DB_POINTER_SIZE));
            file.write(paddedPointer);
        }
    }

    /**
     * Проверяет существование таблицы в базе данных
     *
     * @param dbFilePath путь к файлу базы данных
     * @param tableFilePath путь к файлу таблицы для проверки
     * @return true если таблица уже существует в БД
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    public boolean isTableExists(String dbFilePath, String tableFilePath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "r")) {
            file.seek(50);
            int tableCount = file.readInt();

            byte[] searchBytes = tableFilePath.getBytes(StandardCharsets.UTF_8);
            byte[] currentPointer = new byte[DB_POINTER_SIZE];

            for (int i = 0; i < tableCount; i++) {
                file.seek(DB_HEADER_SIZE + (long) i * DB_POINTER_SIZE);
                file.readFully(currentPointer);

                if (startsWith(currentPointer, searchBytes)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Проверяет, начинается ли массив байтов с указанной последовательности
     */
    private boolean startsWith(byte[] array, byte[] prefix) {
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Создает файл таблицы с указанной схемой
     *
     * @param tableFilePath путь к файлу таблицы
     * @param tableName название таблицы
     * @param schema схема таблицы (список пар: тип+"_"+длина для строк)
     * @throws IOException
     */
    @Override
    public void createTableFile(String tableFilePath, String tableName, List<String> schema) throws IOException {
        validateTxtExtension(tableFilePath);
        validateSchema(schema);

        Path path = Paths.get(tableFilePath).getParent();
        if (path != null && !Files.exists(path)) {
            Files.createDirectories(path);
        }

        if (tableName.length() > 50) {
            throw new IllegalArgumentException("Table name must be 50 characters or less");
        }

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "rw")) {
            // Название таблицы
            byte[] nameBytes = tableName.getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 50));
            file.write(paddedName);

            // Количество записей (0 при создании)
            file.writeInt(0);

            // Записываем схему
            byte[] schemaBytes = encodeSchema(schema);
            file.write(schemaBytes);

            // Указатель на следующую часть (пустой)
            byte[] emptyPointer = new byte[TABLE_POINTER_SIZE];
            file.write(emptyPointer);
        }
    }

    /**
     * Удаляет файл базы данных и все связанные с ним таблицы.
     *
     * @param dbFilePath абсолютный путь к файлу базы данных (с расширением .txt)
     * @throws IOException если произошла ошибка ввода-вывода при удалении файлов
     * @throws IllegalArgumentException если файл не имеет расширения .txt
     */
    @Override
    public void deleteDatabaseFile(String dbFilePath) throws IOException {
        validateTxtExtension(dbFilePath);

        try (RandomAccessFile dbFile = new RandomAccessFile(dbFilePath, "r")) {
            dbFile.seek(50);
            int tableCount = dbFile.readInt();

            for (int i = 0; i < tableCount; i++) {
                dbFile.seek(DB_HEADER_SIZE + (long) i * DB_POINTER_SIZE);
                byte[] pointerBytes = new byte[DB_POINTER_SIZE];
                dbFile.readFully(pointerBytes);
                String tablePath = new String(pointerBytes, StandardCharsets.UTF_8).trim();

                if (!tablePath.isEmpty()) {
                    deleteTableFile(tablePath);
                }
            }
        }
        Files.deleteIfExists(Paths.get(dbFilePath));
    }

    /**
     * Удаляет файл таблицы и все её связанные части (если таблица разделена на несколько файлов).
     *
     * @param tableFilePath абсолютный путь к файлу таблицы (с расширением .txt)
     * @throws IOException если произошла ошибка ввода-вывода при удалении
     * @throws IllegalArgumentException если файл не имеет расширения .txt
     */
    @Override
    public void deleteTableFile(String tableFilePath) throws IOException {
        validateTxtExtension(tableFilePath);

        try (RandomAccessFile tableFile = new RandomAccessFile(tableFilePath, "r")) {
            tableFile.seek(50 + 4);
            byte[] nextPartPointer = new byte[TABLE_POINTER_SIZE];
            tableFile.readFully(nextPartPointer);
            String nextPartPath = new String(nextPartPointer, StandardCharsets.UTF_8).trim();

            if (!nextPartPath.isEmpty()) {
                deleteTableFile(nextPartPath);
            }
        }

        Files.deleteIfExists(Paths.get(tableFilePath));
    }

    /**
     * Удаляет ссылку на таблицу из файла базы данных.
     *
     * @param dbFilePath путь к файлу базы данных (с расширением .txt)
     * @param tableFilePath путь к файлу таблицы (с расширением .txt), который нужно удалить
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws IllegalArgumentException если файлы имеют неверное расширение
     */
    @Override
    public void removeTableReference(String dbFilePath, String tableFilePath) throws IOException {
        validateTxtExtension(dbFilePath);
        validateTxtExtension(tableFilePath);

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "rw")) {
            file.seek(50);
            int tableCount = file.readInt();

            List<String> remainingTables = new ArrayList<>();

            for (int i = 0; i < tableCount; i++) {
                file.seek(DB_HEADER_SIZE + (long) i * DB_POINTER_SIZE);
                byte[] pointerBytes = new byte[DB_POINTER_SIZE];
                file.readFully(pointerBytes);
                String currentPath = new String(pointerBytes, StandardCharsets.UTF_8).trim();

                if (!currentPath.equals(tableFilePath)) {
                    remainingTables.add(currentPath);
                }
            }

            file.seek(50);
            file.writeInt(remainingTables.size());

            for (int i = 0; i < remainingTables.size(); i++) {
                file.seek(DB_HEADER_SIZE + (long) i * DB_POINTER_SIZE);
                byte[] pathBytes = remainingTables.get(i).getBytes(StandardCharsets.UTF_8);
                byte[] paddedPath = new byte[DB_POINTER_SIZE];
                System.arraycopy(pathBytes, 0, paddedPath, 0, Math.min(pathBytes.length, DB_POINTER_SIZE));
                file.write(paddedPath);
            }

            byte[] empty = new byte[DB_POINTER_SIZE];
            for (int i = remainingTables.size(); i < tableCount; i++) {
                file.seek(DB_HEADER_SIZE + (long) i * DB_POINTER_SIZE);
                file.write(empty);
            }
        }
    }

    /**
     * Проверяет, что файл имеет расширение .txt
     *
     * @param filePath путь к файлу для проверки
     * @throws IllegalArgumentException если файл не имеет расширения .txt
     */
    private void validateTxtExtension(String filePath) {
        if (!filePath.toLowerCase().endsWith(".txt")) {
            throw new IllegalArgumentException("File must have .txt extension");
        }
    }

    /**
     * Кодирует схему таблицы в бинарный формат
     */
    private byte[] encodeSchema(List<String> schema) {
        StringBuilder sb = new StringBuilder();
        for (String field : schema) {
            sb.append(field).append(";"); // Разделитель полей
        }

        byte[] schemaBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] paddedSchema = new byte[TABLE_SCHEMA_SIZE];
        System.arraycopy(schemaBytes, 0, paddedSchema, 0, Math.min(schemaBytes.length, TABLE_SCHEMA_SIZE));
        return paddedSchema;
    }

    /**
     * Декодирует схему таблицы из бинарного формата
     */
    private List<String> decodeSchema(byte[] schemaBytes) {
        String schemaStr = new String(schemaBytes, StandardCharsets.UTF_8).trim();
        return Arrays.asList(schemaStr.split(";"));
    }

    /**
     * Проверяет корректность схемы таблицы
     */
    private void validateSchema(List<String> schema) {
        if (schema == null || schema.isEmpty() || schema.size() > MAX_SCHEMA_FIELDS) {
            throw new IllegalArgumentException("Schema must contain 1-" + MAX_SCHEMA_FIELDS + " fields");
        }

        for (String field : schema) {
            if (field.startsWith("int")) {
                if (!field.equals("int")) {
                    throw new IllegalArgumentException("Integer field must be 'int'");
                }
            }
            else if (field.startsWith("str_")) {
                try {
                    int length = Integer.parseInt(field.substring(4));
                    if (length <= 0 || length > 1000) {
                        throw new IllegalArgumentException("String length must be 1-1000");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid string length format");
                }
            }
            else {
                throw new IllegalArgumentException("Invalid field type: " + field);
            }
        }
    }

    /**
     * Основной метод для демонстрации функциональности класса.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        try {
            DataRepositoryImpl dataRepository = new DataRepositoryImpl();

            String dbFilePath = "C:\\BDTest\\database1.txt";
            dataRepository.createDatabaseFile(dbFilePath, "MyTestDatabase");
            System.out.println("Database file created: " + dbFilePath);

            List<String> tableFiles = new ArrayList<>();
            tableFiles.add("C:\\BDTest\\table1.txt");
            tableFiles.add("C:\\BDTest\\table2.txt");

            List<String> schema = Arrays.asList("int", "str_20", "int");

            for (String tableFile : tableFiles) {
                String tableName = Paths.get(tableFile).getFileName().toString().replace(".txt", "");

                if (!Files.exists(Paths.get(tableFile))) {
                    dataRepository.createTableFile(tableFile, tableName, schema);
                    System.out.println("Table created: " + tableFile);
                }

                if (!dataRepository.isTableExists(dbFilePath, tableFile)) {
                    dataRepository.addTableReference(dbFilePath, tableFile);
                    System.out.println("Table reference added to DB: " + tableFile);
                } else {
                    System.out.println("Table reference already exists in DB: " + tableFile);
                }
            }

            try {
                dataRepository.addTableReference(dbFilePath, tableFiles.getFirst());
            } catch (IllegalArgumentException e) {
                System.out.println("Expected error: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
        }
    }

}
