package ru.mephi.db.util;

import java.util.Scanner;

public class ScannerUtils {

    public static boolean promptYesNo(Scanner scanner, String prompt, boolean defaultNo) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.isEmpty()) {
            return !defaultNo;
        }
        return input.equals("y") || input.equals("yes");
    }
}
