package ru.mephi.db.infrastructure.db;

import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.domain.entity.Table;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
     */
    @Override
    public void addTableReference(String dbFilePath, String tableFilePath) throws IOException {
        validateTxtExtension(dbFilePath);
        validateTxtExtension(tableFilePath);

        try (RandomAccessFile file = new RandomAccessFile(dbFilePath, "rw")) {
            file.seek(50);
            int tableCount = file.readInt();
            file.seek(50);
            file.writeInt(tableCount + 1);
            file.seek(DB_HEADER_SIZE + (long) tableCount * DB_POINTER_SIZE);

            byte[] pointerBytes = tableFilePath.getBytes(StandardCharsets.UTF_8);
            byte[] paddedPointer = new byte[DB_POINTER_SIZE];
            System.arraycopy(pointerBytes, 0, paddedPointer, 0, Math.min(pointerBytes.length, DB_POINTER_SIZE));
            file.write(paddedPointer);
        }
    }

    /**
     * Создает новый файл таблицы в формате TXT с указанным именем.
     *
     * @param tableFilePath абсолютный путь к создаваемому файлу таблицы (с расширением .txt)
     * @param tableName название таблицы (максимум 50 символов)
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws IllegalArgumentException если имя таблицы превышает 50 символов
     */
    @Override
    public void createTableFile(String tableFilePath, String tableName) throws IOException {
        validateTxtExtension(tableFilePath);

        Path path = Paths.get(tableFilePath).getParent();
        if (path != null && !Files.exists(path)) {
            Files.createDirectories(path);
        }

        if (tableName.length() > 50) {
            throw new IllegalArgumentException("Table name must be 50 characters or less");
        }

        try (RandomAccessFile file = new RandomAccessFile(tableFilePath, "rw")) {
            byte[] nameBytes = tableName.getBytes(StandardCharsets.UTF_8);
            byte[] paddedName = new byte[50];
            System.arraycopy(nameBytes, 0, paddedName, 0, Math.min(nameBytes.length, 50));
            file.write(paddedName);

            file.writeInt(0);

            byte[] emptyPointer = new byte[TABLE_POINTER_SIZE];
            file.write(emptyPointer);
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

    @Override
    public boolean tableExists(String tableName) {
        return false;
    }

    /**
     * Основной метод для демонстрации функциональности класса.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        try {
            DataRepositoryImpl dataRepository = new DataRepositoryImpl();
            String dbFilePath = "C:\\BDTest\\database_description.txt";
            dataRepository.createDatabaseFile(dbFilePath, "MyTestDatabase");
            System.out.println("Database file created: " + dbFilePath);

            List<String> tableFiles = new ArrayList<>();
            tableFiles.add("C:\\BDTest\\table1.txt");
            tableFiles.add("C:\\BDTest\\table2.txt");
            tableFiles.add("C:\\BDTest\\table2.txt");


            for (String tableFile : tableFiles) {
                String tableName = Paths.get(tableFile).getFileName().toString().replace(".txt", "");
                dataRepository.createTableFile(tableFile, tableName);
                dataRepository.addTableReference(dbFilePath, tableFile);
                System.out.println("Table created and added to DB: " + tableFile);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
        }
    }

}
