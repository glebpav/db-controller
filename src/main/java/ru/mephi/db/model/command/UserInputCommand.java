package ru.mephi.db.model.command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInputCommand {
    private final String command;
    private final UserInputCommandType userInputType;
}
