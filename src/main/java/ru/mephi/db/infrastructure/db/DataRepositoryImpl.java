package ru.mephi.db.infrastructure.db;

import ru.mephi.db.application.adapter.db.DataRepository;

import java.io.FileNotFoundException;
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
    public static final int DB_HEADER_SIZE = 50 + 4;
    /** Размер указателя на таблицу в файле базы данных */
    public static final int DB_POINTER_SIZE = 100;

    /** Максимальный размер файла таблицы */
    private static final int TABLE_MAX_SIZE = 65536;
    /** Размер заголовка файла таблицы (50 байт для имени + 4 байта для количества записей + 100 байт для схемы таблицы + 100 байт для указателя) */
    private static final int TABLE_HEADER_SIZE = 50 + 4 + 100 + 100;
    /** Размер указателя на следующую часть таблицы */
    private static final int TABLE_POINTER_SIZE = 100;
    /** Размер блока схемы таблицы в заголовке */
    public static final int TABLE_SCHEMA_SIZE = 100;
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
            //Создаем файл макс размера
            byte[] buffer = new byte[TABLE_MAX_SIZE];
            file.write(buffer);
            file.seek(0);

            // Название таблицы
            byte[] nameBytes = tableName.getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 50));
            file.write(paddedName);

            // Количество записей (0 при создании)
            file.writeInt(0);

            // Записываем схему
            byte[] schemaBytes = encodeSchema(schema);
            byte[] paddedSchema = new byte[TABLE_SCHEMA_SIZE];
            System.arraycopy(schemaBytes, 0, paddedSchema, 0, Math.min(schemaBytes.length, TABLE_SCHEMA_SIZE));
            file.write(paddedSchema);

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

        if (!Files.exists(Paths.get(tableFilePath))) {
            throw new IOException("Table file not found: " + tableFilePath);
        }

        // Сначала собираем все части таблицы
        List<String> allTableParts = new ArrayList<>();
        String currentPart = tableFilePath;

        while (currentPart != null && !currentPart.isEmpty()) {
            allTableParts.add(currentPart);

            try (RandomAccessFile tableFile = new RandomAccessFile(currentPart, "r")) {
                tableFile.seek(50 + 4 + TABLE_SCHEMA_SIZE);
                byte[] nextPartPointer = new byte[TABLE_POINTER_SIZE];
                tableFile.readFully(nextPartPointer);
                currentPart = new String(nextPartPointer, StandardCharsets.UTF_8).trim();
            } catch (FileNotFoundException e) {
                currentPart = null; // Прерываем цикл, если файл не найден
            }
        }

        // Удаляем все части в обратном порядке (от последней к первой)
        for (int i = allTableParts.size() - 1; i >= 0; i--) {
            Files.deleteIfExists(Paths.get(allTableParts.get(i)));
        }
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

            // Собираем все ссылки кроме удаляемой
            List<String> remainingTables = new ArrayList<>(tableCount);
            for (int i = 0; i < tableCount; i++) {
                file.seek(DataRepositoryImpl.DB_HEADER_SIZE + (long) i * DataRepositoryImpl.DB_POINTER_SIZE);
                byte[] pointerBytes = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
                file.readFully(pointerBytes);
                String currentPath = new String(pointerBytes, StandardCharsets.UTF_8).trim();

                if (!currentPath.equals(tableFilePath)) {
                    remainingTables.add(currentPath);
                }
            }

            file.seek(50);
            file.writeInt(remainingTables.size());

            // Перезаписываем оставшиеся ссылки
            for (int i = 0; i < remainingTables.size(); i++) {
                file.seek(DataRepositoryImpl.DB_HEADER_SIZE + (long) i * DataRepositoryImpl.DB_POINTER_SIZE);
                byte[] pathBytes = remainingTables.get(i).getBytes(StandardCharsets.UTF_8);
                byte[] paddedPath = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
                System.arraycopy(pathBytes, 0, paddedPath, 0, Math.min(pathBytes.length, DataRepositoryImpl.DB_POINTER_SIZE));
                file.write(paddedPath);
            }

            byte[] empty = new byte[DataRepositoryImpl.DB_POINTER_SIZE];
            for (int i = remainingTables.size(); i < tableCount; i++) {
                file.seek(DataRepositoryImpl.DB_HEADER_SIZE + (long) i * DataRepositoryImpl.DB_POINTER_SIZE);
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
     * Читает схему таблицы из файла
     */
    private List<String> getTableSchema(RandomAccessFile file) throws IOException {
        file.seek(50 + 4); // Пропускаем название и кол-во записей
        byte[] schemaBytes = new byte[TABLE_SCHEMA_SIZE];
        file.readFully(schemaBytes);
        return decodeSchema(schemaBytes);
    }

    /**
     * Проверяет соответствие данных схеме таблицы
     */
    private void validateDataAgainstSchema(List<Object> data, List<String> schema) {
        if (data.size() != schema.size()) {
            throw new IllegalArgumentException("Data size doesn't match schema");
        }

        for (int i = 0; i < schema.size(); i++) {
            String fieldType = schema.get(i);
            Object value = data.get(i);

            if (fieldType.equals("int")) {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("Field " + i + " must be Integer");
                }
            } else if (fieldType.startsWith("str_")) {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("Field " + i + " must be String");
                }
                int maxLength = Integer.parseInt(fieldType.substring(4));
                if (((String)value).length() > maxLength) {
                    throw new IllegalArgumentException("String too long for field " + i +
                            ", max length: " + maxLength);
                }
            }
        }
    }

    /**
     * Добавляет запись в таблицу
     * @param tablePath путь к файлу таблицы
     * @param data данные для записи
     * @throws IOException при ошибках ввода-вывода
     * @throws IllegalArgumentException при несоответствии данных схеме
     */
    @Override
    public void addRecord(String tablePath, List<Object> data) throws IOException {
        validateTxtExtension(tablePath);

        try (RandomAccessFile file = new RandomAccessFile(tablePath, "rw")) {
            // Читаем схему и проверяем данные
            List<String> schema = getTableSchema(file);
            validateDataAgainstSchema(data, schema);

            // Получаем текущее количество записей
            file.seek(50);
            int recordCount = file.readInt();

            // Определяем позицию для записи данных (после заголовка и существующих данных)
            long dataPosition;
            if (recordCount == 0) {
                dataPosition = TABLE_HEADER_SIZE;
            } else {
                // Переходим к началу индекса смещений
                file.seek(file.length() - recordCount * 8L);

                // Читаем последнее смещение и вычисляем конец данных
                long lastOffset = file.readLong();

                file.seek(lastOffset);
                dataPosition = file.getFilePointer() + calculateRecordSize(schema, data);
            }

            //Если нужна новая страница
            if(dataPosition >= file.length() - (recordCount + 1) * 8L){

                //Нужно доделать обработку этого случая

                return;
            }
            //Если вмещается на эту страницу
            else {

                // Записываем данные
                file.seek(dataPosition);
                writeData(file, data, schema);

                // Добавляем смещение в конец файла
                long offsetPosition = file.length() - (recordCount + 1) * 8L;
                file.seek(offsetPosition);
                file.writeLong(dataPosition);

                // Обновляем счетчик записей
                file.seek(50);
                file.writeInt(recordCount + 1);

            }
        }
    }

    /**
     * Вычисляет размер записи в байтах
     */
    private int calculateRecordSize(List<String> schema, List<Object> data) {
        int size = 0;
        for (int i = 0; i < schema.size(); i++) {
            String type = schema.get(i);
            if (type.equals("int")) {
                size += 4;
            } else if (type.startsWith("str_")) {
                int maxLength = Integer.parseInt(type.substring(4));
                size += maxLength; // 4 байта на длину + данные
            }
        }
        return size;
    }

    /**
     * Записывает данные в файл согласно схеме
     */
    private void writeData(RandomAccessFile file, List<Object> data, List<String> schema) throws IOException {
        for (int i = 0; i < schema.size(); i++) {
            String type = schema.get(i);
            Object value = data.get(i);

            if (type.equals("int")) {
                file.writeInt((Integer) value);
            } else if (type.startsWith("str_")) {
                String str = (String) value;
                byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
                int maxLength = Integer.parseInt(type.substring(4));

                // Записываем длину строки
                file.writeInt(strBytes.length);
                // Записываем данные
                file.write(strBytes);
                // Дополняем до максимальной длины
                if (strBytes.length < maxLength) {
                    byte[] padding = new byte[maxLength - strBytes.length];
                    file.write(padding);
                }
            }
        }
    }

    /**
     * Читает запись из таблицы по индексу
     */
    @Override
    public List<Object> readRecord(String tablePath, int recordIndex) throws IOException {
        validateTxtExtension(tablePath);

        try (RandomAccessFile file = new RandomAccessFile(tablePath, "r")) {
            // Проверяем количество записей
            file.seek(50);
            int recordCount = file.readInt();
            if (recordIndex < 0 || recordIndex >= recordCount) {
                throw new IllegalArgumentException("Invalid record index");
            }

            // Переходим к нужному смещению в индексе
            long indexPosition = file.length() - (recordIndex + 1) * 8L;
            file.seek(indexPosition);
            long dataOffset = file.readLong();

            // Читаем данные
            return readData(file, getTableSchema(file), dataOffset);
        }
    }

    /**
     * Читает данные из файла согласно схеме
     */
    private List<Object> readData(RandomAccessFile file, List<String> schema, long dataOffset) throws IOException {
        List<Object> record = new ArrayList<>();
        file.seek(dataOffset);

        for (String type : schema) {
            if (type.equals("int")) {
                record.add(file.readInt());
            } else if (type.startsWith("str_")) {
                int length = file.readInt();
                byte[] bytes = new byte[length];
                file.readFully(bytes);
                record.add(new String(bytes, StandardCharsets.UTF_8));

                // Пропускаем padding
                int maxLength = Integer.parseInt(type.substring(4));
                if (length < maxLength) {
                    file.skipBytes(maxLength - length);
                }
            }
        }

        return record;
    }

    public static void main(String[] args) {
        try {
            DataRepositoryImpl repo = new DataRepositoryImpl();
            String dbFile = "C:\\BDTest\\db.txt";
            String tableFile = "C:\\BDTest\\users.txt";

            // Создаем БД и таблицу
            repo.createDatabaseFile(dbFile, "TestDB");
            List<String> schema = Arrays.asList("int", "str_20", "int"); // ID, Name(10 chars), Age
            repo.createTableFile(tableFile, "users", schema);
            repo.addTableReference(dbFile, tableFile);

            // Добавляем записи
            System.out.println("Adding records:");
            repo.addRecord(tableFile, Arrays.asList(1, "Alice", 25));
            repo.addRecord(tableFile, Arrays.asList(2, "Bob", 30));
            repo.addRecord(tableFile, Arrays.asList(3, "Charlie", 35));
            repo.addRecord(tableFile, Arrays.asList(4, "Dima", 100));
            repo.addRecord(tableFile, Arrays.asList(5, "Egor", 3));
            repo.addRecord(tableFile, Arrays.asList(6, "Gleb", 14));


            // Читаем записи
            System.out.println("\nReading records:");
            for (int i = 0; i < 6; i++) {
                List<Object> record = repo.readRecord(tableFile, i);
                System.out.printf("Record %d: %s%n", i, record);
            }

            // Проверяем счетчик записей
            try (RandomAccessFile file = new RandomAccessFile(tableFile, "r")) {
                file.seek(50);
                System.out.println("\nTotal records: " + file.readInt());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
