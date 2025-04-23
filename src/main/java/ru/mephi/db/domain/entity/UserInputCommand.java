package ru.mephi.db.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.mephi.db.domain.valueobject.UserInputCommandType;

@Data
@AllArgsConstructor
public class UserInputCommand {
    private final String command;
    private final UserInputCommandType userInputType;
}
