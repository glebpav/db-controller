package ru.mephi.db.application.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionConfig {

    private String dbPath;

    // Isolation level
    // Permissions and available operations
    // etc

}
