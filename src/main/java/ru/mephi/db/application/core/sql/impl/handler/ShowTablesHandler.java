package ru.mephi.db.application.core.sql.impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ShowTablesHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final ConnectionConfig connectionconfig;

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.SHOW_TABLES;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            String masterDbFilePath = connectionconfig.getMasterPath().toString();
            List<String> tablesName = dataRepository.getAllTableNames(masterDbFilePath);
            return new QueryResult(
                    true,
                    List.of(Map.of("tables", tablesName)),
                    "Tables listed successfully");
        } catch (Exception e) {
            return new QueryResult(false, null, "Error showing tables: " + e.getMessage());
        }
    }
}