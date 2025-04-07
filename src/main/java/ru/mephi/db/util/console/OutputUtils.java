package ru.mephi.db.util.console;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OutputUtils {

    private static boolean colorsEnabled = true;
    private static boolean showTimestamp = false;

    @Setter
    private static PrintStream outputStream = System.out;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void enableColors(boolean enable) {
        colorsEnabled = enable;
    }

    public static void showTimestamps(boolean show) {
        showTimestamp = show;
    }

    private static void printFormatted(String message, Color color, String prefix) {
        StringBuilder sb = new StringBuilder();

        if (showTimestamp) {
            sb.append(getTimestamp()).append(" ");
        }

        if (colorsEnabled) {
            sb.append(color.code);
        }

        sb.append(prefix).append(message);

        if (colorsEnabled) {
            sb.append(Color.RESET.code);
        }

        outputStream.println(sb.toString());
    }

    private static String getTimestamp() {
        return "[" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "]";
    }

    public static void printError(String message) {
        printFormatted(message, Color.BOLD_RED, "[ERROR] ");
    }

    public static void printError(String message, Throwable throwable) {
        printError(message);
        throwable.printStackTrace(outputStream);
    }

    public static void printWarning(String message) {
        printFormatted(message, Color.YELLOW, "[WARNING] ");
    }

    public static void printInfo(String message) {
        printFormatted(message, Color.CYAN, "[INFO] ");
    }

    public static void printDebug(String message) {
        printFormatted(message, Color.GRAY, "[DEBUG] ");
    }

    public static void printSuccess(String message) {
        printFormatted(message, Color.PURPLE, "[SUCCESS] ");
    }

    public static void printError(String format, Object... args) {
        printError(String.format(format, args));
    }

    public static void printWarning(String format, Object... args) {
        printWarning(String.format(format, args));
    }

    public static void printInfo(String format, Object... args) {
        printInfo(String.format(format, args));
    }

    public static void printDebug(String format, Object... args) {
        printDebug(String.format(format, args));
    }

    public static void main(String[] args) {
        // Пример использования
        showTimestamps(true);
        printInfo("Application started");
        printWarning("Low memory: %dMB remaining", 512);
        printError("File not found: %s", "data.txt", new FileNotFoundException());
        printSuccess("Operation completed successfully");
        enableColors(false);
        printDebug("Colors disabled message");
    }
}