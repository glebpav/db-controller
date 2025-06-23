package ru.mephi.db.application.core.sql.Impl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ru.mephi.db.application.core.sql.SQLParser;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;
import ru.mephi.db.exception.SQLParseException;
import ru.mephi.db.application.core.sql.Impl.listener.*;
import ru.mephi.sql.parser.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor_ = @Inject)
public class SQLParserImpl implements SQLParser {
    private final DataRepositoryImpl dataRepository = new DataRepositoryImpl();

    @Override
    public Query parse(String sql) throws SQLParseException {
        try {
            CharStream input = CharStreams.fromString(sql);
            LCombine lexer = new LCombine(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            String upperSql = sql.trim().toUpperCase();

            if (upperSql.startsWith("SELECT")) {
                return parseSelect(tokens);
            } else if (upperSql.startsWith("INSERT")) {
                return parseInsert(tokens);
            } else if (upperSql.startsWith("DELETE")) {
                return parseDelete(tokens);
            } else if (upperSql.startsWith("BEGIN")) {
                return parseBeginTransaction(tokens);
            } else if (upperSql.startsWith("COMMIT")) {
                return parseCommit(tokens);
            } else if (upperSql.startsWith("ROLLBACK")) {
                return parseRollback(tokens);
            } else if (upperSql.startsWith("SHOW FILES")) {
                return parseShowFiles(tokens);
            }else if(upperSql.startsWith("CREATE TABLE")){
                return parseCreateTable(tokens);
            } else if (upperSql.startsWith("DROP TABLE")) {
            return parseDropTable(tokens);
            }
            else if (upperSql.startsWith("SHOW TABLES")) {
                return parseShowTables(tokens);
            } else {
                throw new SQLParseException("Unsupported SQL query type");
            }
        } catch (Exception e) {
            throw new SQLParseException("Error parsing SQL: " + e.getMessage());
        }
    }private Query parseCreateTable(CommonTokenStream tokens) throws SQLParseException {
        try {
            PCreateTable parser = new PCreateTable(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPos, String msg, RecognitionException e) {
                    throw new RuntimeException("Syntax error in CREATE TABLE at " + line + ":" + charPos + " - " + msg);
                }
            });

            PCreateTable.QueryContext ctx = parser.query();
            CreateTableListener listener = new CreateTableListener();
            ParseTreeWalker.DEFAULT.walk(listener, ctx);

            return Query.builder()
                    .type(QueryType.CREATE_TABLE)
                    .table(listener.getTableName())
                    .schema(listener.getShema())
                    .build();
        } catch (Exception e) {
            throw new SQLParseException("Failed to parse CREATE TABLE: " + e.getMessage());
        }
    }
    private Query parseDropTable(CommonTokenStream tokens) throws SQLParseException {
        try {
            PDropTable parser = new PDropTable(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPos, String msg, RecognitionException e) {
                    throw new RuntimeException("Syntax error in DROP TABLE at " + line + ":" + charPos + " - " + msg);
                }
            });

            PDropTable.QueryContext ctx = parser.query();
            DropTableListener listener = new DropTableListener();
            ParseTreeWalker.DEFAULT.walk(listener, ctx);

            return Query.builder()
                    .type(QueryType.DROP_TABLE)
                    .table(listener.getTableName())
                    .build();
        } catch (Exception e) {
            throw new SQLParseException("Failed to parse DROP TABLE: " + e.getMessage());
        }
    }

    private Query parseSelect(CommonTokenStream tokens) throws SQLParseException {
        try {
            PSelect parser = new PSelect(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPos, String msg, RecognitionException e) {
                    throw new RuntimeException("Syntax error in SELECT at " + line + ":" + charPos + " - " + msg);
                }
            });

            PSelect.QueryContext queryContext = parser.query();
            SelectQueryListener listener = new SelectQueryListener();
            ParseTreeWalker.DEFAULT.walk(listener, queryContext);

            return Query.builder()
                    .type(QueryType.SELECT)
                    .table(listener.getTableName())
                    .columnIndices(listener.getColumnIndices())
                    .whereClause(listener.hasWhereClause() ? listener.getWhereClause() : null)
                    .build();
        } catch (Exception e) {
            throw new SQLParseException("Failed to parse SELECT query: " + e.getMessage());
        }
    }

    private Query parseInsert(CommonTokenStream tokens) throws SQLParseException {
        try {
            PInsert parser = new PInsert(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPos, String msg, RecognitionException e) {
                    throw new RuntimeException("Syntax error in INSERT at " + line + ":" + charPos + " - " + msg);
                }
            });

            PInsert.QueryContext queryContext = parser.query();
            InsertQueryListener listener = new InsertQueryListener();
            ParseTreeWalker.DEFAULT.walk(listener, queryContext);

            return Query.builder()
                    .type(QueryType.INSERT)
                    .table(listener.getTableName())
                    .values(listener.getValues())
                    .build();
        } catch (Exception e) {
            throw new SQLParseException("Failed to parse INSERT query: " + e.getMessage());
        }
    }
    private Query parseDelete(CommonTokenStream tokens) throws SQLParseException {
        try {
            PDelete parser = new PDelete(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @SneakyThrows
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPos, String msg, RecognitionException e) {
                    throw new SQLParseException("Syntax error in DELETE at " + line + ":" + charPos + " - " + msg);
                }
            });

            PDelete.QueryContext queryContext = parser.query();
            DeleteQueryListener listener = new DeleteQueryListener();
            ParseTreeWalker.DEFAULT.walk(listener, queryContext);

            if (listener.getTableName() == null || listener.getTableName().isEmpty()) {
                throw new SQLParseException("Table name is missing in DELETE statement");
            }

            Query.QueryBuilder builder = Query.builder()
                    .type(QueryType.DELETE)
                    .table(listener.getTableName());

            if (listener.hasWhereClause()) {
                String whereClause = listener.getWhereClause();
                // Проверяем специальный случай удаления по row_index
                if (whereClause.startsWith("row_index")) {
                    String[] parts = whereClause.split("=");
                    if (parts.length == 2) {
                        builder.rowIndex(Integer.parseInt(parts[1].trim()));
                    }
                } else {
                    builder.whereClause(whereClause);
                }
            }

            return builder.build();
        } catch (Exception e) {
            throw new SQLParseException("Failed to parse DELETE query: " + e.getMessage());
        }
    }

    private Query parseBeginTransaction(CommonTokenStream tokens) throws SQLParseException {
        try {
            PBeginTransaction parser = new PBeginTransaction(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPos, String msg, RecognitionException e) {
                    throw new RuntimeException("Syntax error at " + line + ":" + charPos + " - " + msg);
                }
            });

            PBeginTransaction.QueryContext ctx = parser.query();
            BeginTransactionListener listener = new BeginTransactionListener();
            ParseTreeWalker.DEFAULT.walk(listener, ctx);

            return Query.builder()
                    .type(QueryType.BEGIN_TRANSACTION)
                    .transactionName(listener.getTransactionName())
                    .build();
        } catch (Exception e) {
            throw new SQLParseException("Failed to parse BEGIN TRANSACTION: " + e.getMessage());
        }
    }

    private Query parseCommit(CommonTokenStream tokens) {
        return Query.builder()
                .type(QueryType.COMMIT)
                .build();
    }

    private Query parseRollback(CommonTokenStream tokens) {
        return Query.builder()
                .type(QueryType.ROLLBACK)
                .build();
    }
    private Query parseShowFiles(CommonTokenStream tokens) throws SQLParseException {
        try {
            PShowFiles parser = new PShowFiles(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPos, String msg, RecognitionException e) {
                    throw new RuntimeException("Syntax error at " + line + ":" + charPos + " - " + msg);
                }
            });

            PShowFiles.QueryContext ctx = parser.query();
            ShowFilesListener listener = new ShowFilesListener();
            ParseTreeWalker.DEFAULT.walk(listener, ctx);

            return Query.builder()
                    .type(QueryType.SHOW_FILES)
                    .databaseName(listener.getDatabaseName())
                    .build();
        } catch (Exception e) {
            throw new SQLParseException("Failed to parse SHOW FILES: " + e.getMessage());
        }
    }
    private Query parseShowTables(CommonTokenStream tokens) throws SQLParseException {
        try {
            PShowTables parser = new PShowTables(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPos, String msg, RecognitionException e) {
                    throw new RuntimeException("Syntax error in SHOW TABLES at " + line + ":" + charPos + " - " + msg);
                }
            });

            PShowTables.QueryContext ctx = parser.query();

            new ShowTablesListener();

            return Query.builder()
                    .type(QueryType.SHOW_TABLES)
                    .build();
        } catch (Exception e) {
            throw new SQLParseException("Failed to parse SHOW TABLES: " + e.getMessage());
        }
    }


}