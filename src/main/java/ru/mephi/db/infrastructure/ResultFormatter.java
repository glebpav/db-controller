package ru.mephi.db.infrastructure;

import ru.mephi.db.domain.entity.QueryResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ResultFormatter {
    
    public static String format(QueryResult result) {
        StringBuilder sb = new StringBuilder();
        
        // Status line with color
        if (result.isSuccess()) {
            sb.append(Constants.ANSI_GREEN).append(Constants.ANSI_BOLD)
              .append("SUCCESS").append(Constants.ANSI_RESET);
        } else {
            sb.append(Constants.ANSI_RED).append(Constants.ANSI_BOLD)
              .append("FAILED").append(Constants.ANSI_RESET);
        }
        
        // Message
        sb.append(": ").append(result.getMessage()).append("\n");
        
        // Rows as table if present
        if (result.getRows() != null && !result.getRows().isEmpty()) {
            sb.append(formatTable(result.getRows()));
        }
        
        return sb.toString();
    }
    
    private static String formatTable(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Get all unique column names
        Set<String> allColumns = rows.stream()
                .flatMap(row -> row.keySet().stream())
                .collect(Collectors.toSet());
        
        List<String> columnNames = allColumns.stream().sorted().toList();
        
        // Calculate column widths
        Map<String, Integer> columnWidths = calculateColumnWidths(rows, columnNames);
        
        // Print header
        sb.append(Constants.ANSI_CYAN).append(Constants.ANSI_BOLD);
        sb.append("┌");
        for (String col : columnNames) {
            sb.append("─".repeat(columnWidths.get(col) + 2)).append("┬");
        }
        sb.setLength(sb.length() - 1); // Remove last ┬
        sb.append("┐\n");
        
        // Column names
        sb.append("│");
        for (String col : columnNames) {
            sb.append(" ").append(padRight(col, columnWidths.get(col))).append(" │");
        }
        sb.append("\n");
        
        // Separator
        sb.append("├");
        for (String col : columnNames) {
            sb.append("─".repeat(columnWidths.get(col) + 2)).append("┼");
        }
        sb.setLength(sb.length() - 1); // Remove last ┼
        sb.append("┤\n");
        
        sb.append(Constants.ANSI_RESET);
        
        // Data rows
        for (Map<String, Object> row : rows) {
            sb.append("│");
            for (String col : columnNames) {
                Object value = row.get(col);
                String strValue = value != null ? value.toString() : "null";
                sb.append(" ").append(padRight(strValue, columnWidths.get(col))).append(" │");
            }
            sb.append("\n");
        }
        
        // Footer
        sb.append(Constants.ANSI_CYAN);
        sb.append("└");
        for (String col : columnNames) {
            sb.append("─".repeat(columnWidths.get(col) + 2)).append("┴");
        }
        sb.setLength(sb.length() - 1); // Remove last ┴
        sb.append("┘\n");
        sb.append(Constants.ANSI_RESET);
        
        return sb.toString();
    }
    
    private static Map<String, Integer> calculateColumnWidths(List<Map<String, Object>> rows, List<String> columnNames) {
        Map<String, Integer> widths = columnNames.stream()
                .collect(Collectors.toMap(col -> col, col -> col.length()));
        
        for (Map<String, Object> row : rows) {
            for (String col : columnNames) {
                Object value = row.get(col);
                String strValue = value != null ? value.toString() : "null";
                widths.put(col, Math.max(widths.get(col), strValue.length()));
            }
        }
        
        return widths;
    }
    
    private static String padRight(String str, int length) {
        if (str.length() >= length) {
            return str;
        }
        return str + " ".repeat(length - str.length());
    }
} 