package ru.mephi.db.infrastructure.db;

import ru.mephi.db.application.adapter.db.DataRepository;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
     * Проверяет существование таблицы в базе данных.
     *
     * @param tableName имя таблицы для проверки (не может быть null или пустым)
     * @throws UnsupportedOperationException метод пока не реализован
     * @throws IllegalArgumentException если tableName равен null или пустой
     */
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
        DataRepositoryImpl dataRepository = new DataRepositoryImpl();
        String dbFilePath = "C:\\BDTest\\database_description.txt";
        String tableToDelete = "C:\\BDTest\\table2.txt";

        try {
            dataRepository.createDatabaseFile(dbFilePath, "MyTestDatabase");
            System.out.println("Database file created: " + dbFilePath);

            List<String> tableFiles = List.of(
                    "C:\\BDTest\\table1.txt",
                    tableToDelete,
                    "C:\\BDTest\\table3.txt"
            );


            for (String tableFile : tableFiles) {
                String tableName = Paths.get(tableFile).getFileName().toString().replace(".txt", "");
                dataRepository.createTableFile(tableFile, tableName);
                dataRepository.addTableReference(dbFilePath, tableFile);
                System.out.println("Table created and added to DB: " + tableFile);
            }

            System.out.println("\nPress Enter to delete entire database...");
            new Scanner(System.in).nextLine();

            System.out.println("\nAttempting to delete single table: " + tableToDelete);
            try {
                dataRepository.removeTableReference(dbFilePath, tableToDelete);
                dataRepository.deleteTableFile(tableToDelete);
                System.out.println("Table successfully deleted: " + tableToDelete);

            } catch (IOException e) {
                System.err.println("Error deleting table: " + e.getMessage());
            }

            System.out.println("\nPress Enter to delete entire database...");
            new Scanner(System.in).nextLine();

            dataRepository.deleteDatabaseFile(dbFilePath);
            System.out.println("Database and all remaining tables deleted successfully");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
        }
    }
}
