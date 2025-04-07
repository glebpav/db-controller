package ru.mephi.db.util.console;

enum Color {

    RESET("\u001B[0m"),
    BOLD_RED("\u001B[1;31m"),
    YELLOW("\u001B[33m"),
    CYAN("\u001B[36m"),
    GRAY("\u001B[90m"),
    PURPLE("\u001B[35m");

    final String code;

    Color(String code) {
        this.code = code;
    }
}

