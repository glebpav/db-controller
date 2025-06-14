package ru.mephi.db.infrastructure.db;

import ru.mephi.db.application.adapter.db.DataRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DataRepositoryImpl implements DataRepository {

    /** Размер заголовка файла базы данных (50 байт для имени + 4 байта для количества таблиц) */
    private static final int DB_HEADER_SIZE = 50 + 4;
    /** Размер указателя на таблицу в файле базы данных */
    public static final int DB_POINTER_SIZE = 100;
    /** Максимальный размер файла таблицы */
    private static final int TABLE_MAX_SIZE = 65536;
    /** Размер заголовка файла таблицы (50 байт для имени + 4 байта для количества записей +
     * 100 байт для схемы таблицы + 100 байт для указателя) */
    private static final int TABLE_HEADER_SIZE = 50 + 4 + 100 + 100;
    /** Размер указателя на следующую часть таблицы */
    private static final int TABLE_POINTER_SIZE = 100;
    /** Размер блока схемы таблицы в заголовке */
    private static final int TABLE_SCHEMA_SIZE = 100;
    /** Максимальное количество полей в схеме */
    private static final int MAX_SCHEMA_FIELDS = 20;
    /** Максимально допустимая длина строкового поля в таблице (в символах).*/
    private static final int MAX_STRING_LENGTH = 1000;

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
            byte[] nameBytes = dbName.getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 50));
            file.write(paddedName);
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

        Path dbPath = Paths.get(dbFilePath).toAbsolutePath().normalize();
        Path tablePath = Paths.get(tableFilePath).toAbsolutePath().normalize();

        if (!Files.exists(dbPath)) {
            throw new FileNotFoundException("Database file not found: " + dbPath);
        }
        if (!Files.exists(tablePath)) {
            throw new FileNotFoundException("Table file not found: " + tablePath);
        }

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "rw")) {
            if (file.length() < DB_HEADER_SIZE) {
                throw new IOException("Corrupted database file");
            }

            file.seek(50);
            int tableCount = file.readInt();

            long newPointerOffset = DB_HEADER_SIZE + (long) tableCount * DB_POINTER_SIZE;
            if (newPointerOffset + DB_POINTER_SIZE > TABLE_MAX_SIZE) {
                throw new IllegalStateException("Database cannot contain more tables");
            }

            String pathToStore = tablePath.toString();
            byte[] pathBytes = pathToStore.getBytes(StandardCharsets.UTF_8);

            if (pathBytes.length > DB_POINTER_SIZE) {
                throw new IllegalArgumentException("Table path exceeds maximum length");
            }
            if (isTableExists(dbFilePath, pathToStore)) {
                throw new IllegalArgumentException("Table reference already exists in database: " + pathToStore);
            }
            file.seek(50);
            file.writeInt(tableCount + 1);

            file.seek(newPointerOffset);
            byte[] pointer = new byte[DB_POINTER_SIZE];
            System.arraycopy(pathBytes, 0, pointer, 0, pathBytes.length);
            file.write(pointer);
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
        validateTxtExtension(dbFilePath);

        Path dbPath = Paths.get(dbFilePath).toAbsolutePath().normalize();
        if (!Files.exists(dbPath)) {
            throw new FileNotFoundException("Database file not found: " + dbPath);
        }

        Path searchPath = Paths.get(tableFilePath);
        if (!searchPath.isAbsolute()) {
            searchPath = dbPath.getParent().resolve(tableFilePath).normalize();
        } else {
            searchPath = searchPath.normalize();
        }

        String searchPathStr = searchPath.toString();
        try (RandomAccessFile file = new RandomAccessFile(dbPath.toFile(), "r")) {
            if (file.length() < DB_HEADER_SIZE) {
                throw new IOException("Corrupted database file");
            }

            file.seek(50);
            int tableCount = file.readInt();

            long requiredSize = DB_HEADER_SIZE + (long) tableCount * DB_POINTER_SIZE;
            if (file.length() < requiredSize) {
                throw new IOException("Corrupted database file: invalid table count");
            }

            file.seek(DB_HEADER_SIZE);
            byte[] pointerBuffer = new byte[DB_POINTER_SIZE];

            for (int i = 0; i < tableCount; i++) {
                file.readFully(pointerBuffer);
                String storedPath = new String(pointerBuffer, StandardCharsets.UTF_8).trim();

                if (searchPathStr.equals(storedPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Создает файл таблицы с указанной схемой.
     *
     * @param tableFilePath абсолютный путь к файлу таблицы (с расширением .txt)
     * @param tableName название таблицы (максимум 50 байт в UTF-8)
     * @param schema схема таблицы (список полей формата "int" или "str_<длина>")
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws IllegalArgumentException если параметры невалидны
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
            byte[] buffer = new byte[TABLE_MAX_SIZE];
            file.write(buffer);
            file.seek(0);

            byte[] nameBytes = tableName.getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 50));
            file.write(paddedName);

            file.writeInt(0);

            byte[] schemaBytes = encodeSchema(schema);
            byte[] paddedSchema = new byte[TABLE_SCHEMA_SIZE];
            System.arraycopy(schemaBytes, 0, paddedSchema, 0, Math.min(schemaBytes.length, TABLE_SCHEMA_SIZE));
            file.write(paddedSchema);

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
        Path dbPath = Paths.get(dbFilePath).toAbsolutePath().normalize();

        validateTxtExtension(dbPath.toString());

        if (!Files.exists(dbPath)) {
            throw new FileNotFoundException("Database file not found: " + dbPath);
        }

        try (RandomAccessFile dbFile = new RandomAccessFile(dbPath.toFile(), "r")) {
            if (dbFile.length() < DB_HEADER_SIZE) {
                throw new IOException("Corrupted database file: too small");
            }

            dbFile.seek(50);
            int tableCount = dbFile.readInt();

            long maxPointerOffset = DB_HEADER_SIZE + (long) tableCount * DB_POINTER_SIZE;
            if (maxPointerOffset > dbFile.length()) {
                throw new IOException("Corrupted database file: invalid table count");
            }

            for (int i = 0; i < tableCount; i++) {
                dbFile.seek(DB_HEADER_SIZE + (long) i * DB_POINTER_SIZE);
                byte[] pointerBytes = new byte[DB_POINTER_SIZE];
                dbFile.readFully(pointerBytes);
                String tablePath = new String(pointerBytes, StandardCharsets.UTF_8).trim();

                if (!tablePath.isEmpty()) {
                    Path tableFilePath = Paths.get(tablePath).normalize();
                    if (Files.exists(tableFilePath)) {
                        deleteTableFile(tableFilePath.toString());
                    }
                }
            }
        }

        Files.deleteIfExists(dbPath);
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
        Path tablePath = Paths.get(tableFilePath).normalize();
        validateTxtExtension(tableFilePath);

        if (!Files.exists(tablePath)) {
            throw new IOException("Table file not found: " + tableFilePath);
        }

        List<Path> allTableParts = new ArrayList<>();
        Path currentPart = tablePath;
        int partsLimit = 1000; //либо больше взять число(чтоб бесконечного цикла не было)
        while (currentPart != null && partsLimit --> 0) {
            allTableParts.add(currentPart);

            try (RandomAccessFile tableFile = new RandomAccessFile(currentPart.toFile(), "r")) {
                if (tableFile.length() < (DB_HEADER_SIZE + TABLE_SCHEMA_SIZE) + TABLE_POINTER_SIZE) {
                    break; // Файл слишком мал для хранения указателя
                }

                tableFile.seek(DB_HEADER_SIZE + TABLE_SCHEMA_SIZE);
                byte[] pointerBytes = new byte[TABLE_POINTER_SIZE];
                tableFile.readFully(pointerBytes);
                String nextPart = new String(pointerBytes, StandardCharsets.UTF_8).trim();
                currentPart = nextPart.isEmpty() ? null : Paths.get(nextPart).normalize();
            } catch (IOException e) {
                throw new IOException("Failed to read next part pointer from " + currentPart, e);
            }
        }

        List<Path> failedToDelete = new ArrayList<>();
        for (int i = allTableParts.size() - 1; i >= 0; i--) {
            try {
                Files.deleteIfExists(allTableParts.get(i));
            } catch (IOException e) {
                failedToDelete.add(allTableParts.get(i));
            }
        }
        if (!failedToDelete.isEmpty()) {
            throw new IOException("Failed to delete some table parts: " + failedToDelete);
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

        Path dbPath = Paths.get(dbFilePath);
        Path targetPath = Paths.get(tableFilePath).normalize();

        if (!Files.exists(dbPath)) {
            throw new FileNotFoundException("Database file not found");
        }

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "rw")) {
            if (file.length() < DB_HEADER_SIZE) {
                throw new IOException("Corrupted database file");
            }

            file.seek(50);
            int tableCount = file.readInt();

            List<String> remainingTables = new ArrayList<>(tableCount);
            byte[] pointerBuffer = new byte[DB_POINTER_SIZE];
            for (int i = 0; i < tableCount; i++) {
                file.seek(DB_HEADER_SIZE + (long) i * DB_POINTER_SIZE);
                file.readFully(pointerBuffer);

                Path currentPath = Paths.get(
                        new String(pointerBuffer, StandardCharsets.UTF_8).trim()
                ).normalize();

                if (!currentPath.equals(targetPath)) {
                    remainingTables.add(currentPath.toString());
                }
            }

            if (remainingTables.size() == tableCount) {
                return;
            }

            file.seek(50);
            file.writeInt(remainingTables.size());

            byte[] allPointers = new byte[remainingTables.size() * DB_POINTER_SIZE];
            for (int i = 0; i < remainingTables.size(); i++) {
                byte[] pathBytes = remainingTables.get(i).getBytes(StandardCharsets.UTF_8);
                System.arraycopy(
                        pathBytes, 0,
                        allPointers, i * DB_POINTER_SIZE,
                        Math.min(pathBytes.length, DB_POINTER_SIZE)
                );
            }

            file.seek(DB_HEADER_SIZE);
            file.write(allPointers);

            // Усечение файла
            file.setLength(DB_HEADER_SIZE + (long) remainingTables.size() * DB_POINTER_SIZE);
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
    private byte[] encodeSchema(List<String> schema) throws IOException {
        for (String field : schema) {
            if (field.contains(";")) {
                throw new IllegalArgumentException("Field cannot contain ';' character: " + field);
            }
        }

        String schemaStr = String.join(";", schema);
        byte[] schemaBytes = schemaStr.getBytes(StandardCharsets.UTF_8);
        if (schemaBytes.length > TABLE_SCHEMA_SIZE) {
            throw new IOException("Schema too large (" + schemaBytes.length +
                    " bytes), maximum is " + TABLE_SCHEMA_SIZE);
        }

        byte[] paddedSchema = new byte[TABLE_SCHEMA_SIZE];
        Arrays.fill(paddedSchema, (byte) 0);
        System.arraycopy(schemaBytes, 0, paddedSchema, 0, schemaBytes.length);
        return paddedSchema;
    }

    /**
     * Декодирует схему таблицы из бинарного формата
     */
    private List<String> decodeSchema(byte[] schemaBytes) {
        int length = 0;
        while (length < schemaBytes.length && schemaBytes[length] != 0) {
            length++;
        }

        String schemaStr = new String(schemaBytes, 0, length, StandardCharsets.UTF_8);
        return Arrays.stream(schemaStr.split(";"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Проверяет корректность схемы таблицы.
     * @param schema Список полей в формате:
     *               - "int" для целых чисел
     *               - "str_<длина>" для строк (длина от 1 до 1000)
     * @throws IllegalArgumentException если схема некорректна
     */
    private void validateSchema(List<String> schema) {
        if (schema == null || schema.isEmpty() || schema.size() > MAX_SCHEMA_FIELDS) {
            throw new IllegalArgumentException(
                    "Schema must contain 1-" + MAX_SCHEMA_FIELDS + " fields"
            );
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
                    if (length <= 0 || length > MAX_STRING_LENGTH) {
                        throw new IllegalArgumentException("String length must be 1-1000");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid string length format in field: " + field
                    );
                }
            }
            else {
                throw new IllegalArgumentException(
                        "Invalid field type: " + field + ". Expected 'int' or 'str_<length>'."
                );
            }
        }
    }

    /**
     * Читает схему таблицы из файла
     */
    private List<String> getTableSchema(RandomAccessFile file) throws IOException {
        if (file.length() < DB_HEADER_SIZE + TABLE_SCHEMA_SIZE) {
            throw new IOException("File too small to contain schema");
        }

        file.seek(DB_HEADER_SIZE);
        byte[] schemaBytes = new byte[TABLE_SCHEMA_SIZE];
        file.readFully(schemaBytes);
        return decodeSchema(schemaBytes);
    }

    /**
     * Проверяет соответствие данных схеме таблицы
     */
    private void validateDataAgainstSchema(List<Object> data, List<String> schema) {
        if (data == null || schema == null) {
            throw new IllegalArgumentException("Data and schema cannot be null");
        }

        if (data.size() != schema.size()) {
            throw new IllegalArgumentException(
                    String.format("Data size (%d) doesn't match schema size (%d)",
            data.size(), schema.size()));
        }

        for (int i = 0; i < schema.size(); i++) {
            String fieldType = schema.get(i);
            Object value = data.get(i);

            if (fieldType.equals("int")) {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException(
                            String.format("Field %d must be Integer, got %s",
                    i, value.getClass().getSimpleName()));
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
     * Вычисляет размер записи в байтах.
     * @param schema Схема таблицы (например, ["int", "str_10"])
     * @return Размер записи в байтах
     * @throws IllegalArgumentException если схема содержит неизвестный тип
     */
    private int calculateRecordSize(List<String> schema, List<Object> data) {
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null");
        }

        int size = 0;
        for (String type : schema) {
            if (type.equals("int")) {
                size += 4; // int = 4 байта
            }
            else if (type.startsWith("str_")) {
                try {
                    int maxLength = Integer.parseInt(type.substring(4));
                    size += 4 + maxLength; // 4 байта на длину + данные
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid string length in type: " + type);
                }
            }
            else {
                throw new IllegalArgumentException("Unknown field type: " + type);
            }
        }
        return size;
    }

    /**
     * Записывает данные в файл согласно схеме
     */
    private void writeData(RandomAccessFile file, List<Object> data, List<String> schema) throws IOException {
        byte[] paddingBuffer = new byte[1024];

        for (int i = 0; i < schema.size(); i++) {
            String type = schema.get(i);
            Object value = data.get(i);

            if (type.equals("int")) {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("Field " + i + " must be Integer");
                }

                file.writeInt((Integer) value);
            } else if (type.startsWith("str_")) {
                if (!(value instanceof String str)) {
                    throw new IllegalArgumentException("Field " + i + " must be String");
                }

                byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
                int maxLength = Integer.parseInt(type.substring(4));

                if (strBytes.length > maxLength) {
                    throw new IllegalArgumentException("String too long for field " + i);
                }

                file.writeInt(strBytes.length);
                file.write(strBytes);

                int paddingSize = maxLength - strBytes.length;
                while (paddingSize > 0) {
                    int chunk = Math.min(paddingSize, paddingBuffer.length);
                    file.write(paddingBuffer, 0, chunk);
                    paddingSize -= chunk;
                }
            }
            else {
                throw new IllegalArgumentException("Unknown field type: " + type);
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
            file.seek(50);
            int recordCount = file.readInt();

            if (recordIndex < 0 || recordIndex >= recordCount) {
                throw new IllegalArgumentException("Invalid record index: " + recordIndex +
                        ", available records: " + recordCount);
            }

            long indexPosition = file.length() - (recordIndex + 1) * 8L;
            if (indexPosition < TABLE_HEADER_SIZE) {
                throw new IOException("Invalid index position");
            }

            file.seek(indexPosition);
            long dataOffset = file.readLong();
            if (dataOffset < TABLE_HEADER_SIZE || dataOffset >= file.length()) {
                throw new IOException("Invalid data offset in index: " + dataOffset);
            }

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
                int maxLength = Integer.parseInt(type.substring(4));
                int length = file.readInt();
                if (length > maxLength) {
                    throw new IOException("String length exceeds max allowed size: " + length + " > " + maxLength);
                }

                byte[] bytes = new byte[length];
                file.readFully(bytes);
                record.add(new String(bytes, StandardCharsets.UTF_8));

                file.skipBytes(maxLength - length);
            } else {
                throw new IOException("Unknown field type: " + type);
            }
        }

        return record;
    }

    /**
     * Удаляет запись по указанному индексу.
     *
     * @param tablePath   путь к файлу таблицы
     * @param recordIndex индекс записи (0-based)
     * @throws IOException              при ошибках чтения/записи
     * @throws IllegalArgumentException если индекс некорректен
     */
    @Override
    public void deleteRecord(String tablePath, int recordIndex) throws IOException {
        validateTxtExtension(tablePath);
        Path backupPath = createBackup(tablePath);

        try (RandomAccessFile file = new RandomAccessFile(tablePath, "rw")) {
            // Проверка минимального размера файла
            if (file.length() < TABLE_HEADER_SIZE + 8) {
                throw new IOException("File is too small or corrupted");
            }

            file.seek(50);
            int recordCount = file.readInt();

            if (recordIndex < 0 || recordIndex >= recordCount) {
                throw new IllegalArgumentException("Invalid record index");
            }

            // Чтение всех смещений
            List<Long> offsets = readOffsets(file, recordCount);
            long deletedOffset = offsets.remove(recordIndex);

            file.seek(deletedOffset);
            file.writeByte(0xFF); // Маркер удаления
            truncateFileAndUpdateIndex(file, recordCount - 1, offsets);

            Files.deleteIfExists(backupPath);
        } catch (Exception e) {
            restoreFromBackup(tablePath, backupPath);
            throw new IOException("Failed to delete record", e);
        }
    }

    /** Чтение смещений */
    private List<Long> readOffsets(RandomAccessFile file, int recordCount) throws IOException {
        long indexStart = file.length() - (long) recordCount * 8;
        if (indexStart < TABLE_HEADER_SIZE) {
            throw new IOException("Invalid index start position");
        }

        List<Long> offsets = new ArrayList<>();
        file.seek(indexStart);

        for (int i = 0; i < recordCount; i++) {
            offsets.add(file.readLong());
        }

        return offsets;
    }

    /**
     * Усечение файла до нового размера и обновление индекса смещений.
     * Уменьшает размер файла, обновляет количество записей и перезаписывает смещения.
     */
    private void truncateFileAndUpdateIndex(RandomAccessFile file, int newRecordCount, List<Long> offsets)
            throws IOException {
        long oldLength = file.length();
        long newLength = oldLength - 8;
        if (newLength < TABLE_HEADER_SIZE) {
            throw new IOException("File size cannot be less than header size");
        }

        file.setLength(newLength);
        file.seek(50);
        file.writeInt(newRecordCount);

        long newIndexStart = newLength - (long) newRecordCount* 8;
        if (newIndexStart < TABLE_HEADER_SIZE) {
            throw new IOException("New index start is invalid: " + newIndexStart);
        }

        file.seek(newIndexStart);
        for (Long offset : offsets) {
            file.writeLong(offset);
        }
    }

    /**
     * Создание резервной копии файла таблицы.
     * Возвращает путь к созданному backup-файлу с расширением .bak.
     */
    private Path createBackup(String tablePath) throws IOException {
        Path path = Paths.get(tablePath);
        Path backup = path.resolveSibling(path.getFileName() + ".bak");
        Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
        return backup;
    }

    /**
     * Восстановление таблицы из резервной копии.
     * Заменяет текущий файл таблицы backup-файлом, если он существует.
     */
    private void restoreFromBackup(String tablePath, Path backup) throws IOException {
        Path target = Paths.get(tablePath);
        if (Files.exists(target)) {
            Files.delete(target);
        }
        Files.move(backup, target);
    }
}
