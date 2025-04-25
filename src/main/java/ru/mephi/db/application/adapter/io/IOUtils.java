package ru.mephi.db.application.adapter.io;

public class IOUtils {

    public static boolean promptYesNo(InputBoundary input, OutputBoundary out, String prompt, boolean defaultNo) {
        out.info(prompt);
        String inputString = input.next().trim().toLowerCase();

        if (inputString.isEmpty()) {
            return !defaultNo;
        }

        return inputString.equals("y") || inputString.equals("yes");
    }

}
