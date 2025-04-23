package ru.mephi.db.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
@Builder
public class Database {
    private String dbName;
    private Path path;
}
