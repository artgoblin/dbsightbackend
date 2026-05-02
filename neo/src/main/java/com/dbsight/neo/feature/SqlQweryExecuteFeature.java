package com.dbsight.neo.feature;

import org.springframework.dao.QueryTimeoutException;
import java.sql.SQLXML;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dbsight.neo.helper.UtilHelper;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.dbsight.neo.modal.Error;
import com.dbsight.neo.modal.QueryResult;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlQweryExecuteFeature {

    private final StringRedisTemplate redisTemplate;
    private final DataSourceFactoryFeature dataSourceFactoryFeature;

    // Default timeout in seconds (adjust as needed)
    private static final int DEFAULT_QUERY_TIMEOUT_SECONDS = 30;

    public QueryResult execute(String sql, String databaseName, Long userId, Integer offset, Integer limit)
            throws JsonProcessingException {

        try {
            DatabaseConnectionDetails databaseConnectionDetails = UtilHelper.getConnectionDetailsFromRedis(userId,
                    databaseName, redisTemplate);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(
                    dataSourceFactoryFeature.getOrCreateDataSource(databaseConnectionDetails, userId));

            jdbcTemplate.setQueryTimeout(DEFAULT_QUERY_TIMEOUT_SECONDS);

            String cleanSql = sql.trim().replaceAll(";+$", "");

            if (isSelectQuery(cleanSql)) {
                String sqlToExecute = cleanSql;
                if (limit != null && offset != null) {
                    if (isWrappable(cleanSql)) {
                        sqlToExecute = "SELECT * FROM (\n" + cleanSql + "\n) AS internal_query LIMIT " + (limit + 1) + " OFFSET " + offset;
                    } else if (!hasPagination(cleanSql)) {
                        sqlToExecute = cleanSql + " LIMIT " + (limit + 1) + " OFFSET " + offset;
                    }
                }

                long startTime = System.currentTimeMillis();
                List<Map<String, Object>> result = jdbcTemplate.query(
                        sqlToExecute,
                        new ColumnMapRowMapper() {
                            @Override
                            protected Object getColumnValue(java.sql.ResultSet rs, int index) throws SQLException {
                                Object value = super.getColumnValue(rs, index);
                                if (value instanceof SQLXML) {
                                    return ((SQLXML) value).getString();
                                }
                                return value;
                            }
                        });
                long endTime = System.currentTimeMillis();

                boolean hasMore = limit != null && result.size() > limit;

                if (hasMore) {
                    result.remove(result.size() - 1);
                }

                QueryResult queryResult = new QueryResult();
                queryResult.setResult(result);
                queryResult.setHasMore(hasMore);
                queryResult.setRowCount(result.size());
                queryResult.setExecutionTime(endTime - startTime);

                return queryResult;
            } else {
                // Handle DDL/DML
                long startTime = System.currentTimeMillis();
                int rowsAffected = jdbcTemplate.update(cleanSql);
                long endTime = System.currentTimeMillis();

                QueryResult queryResult = new QueryResult();
                queryResult.setRowCount(rowsAffected);
                queryResult.setResult(
                        List.of(Map.of("message", "Statement executed successfully. Rows affected: " + rowsAffected)));
                queryResult.setHasMore(false);
                queryResult.setExecutionTime(endTime - startTime);
                return queryResult;
            }

        } catch (QueryTimeoutException e) {
            QueryResult queryResult = new QueryResult();
            queryResult.setError(new Error(
                    "Query execution timed out after " + DEFAULT_QUERY_TIMEOUT_SECONDS + " seconds",
                    "timeout"));
            return queryResult;

        } catch (Exception e) {
            QueryResult queryResult = new QueryResult();
            String msg = "Error executing query: " + e.getMessage();
            if (e.getCause() != null) {
                msg += " | Cause: " + e.getCause().getMessage();
            }
            queryResult.setError(new Error(msg, "error"));
            return queryResult;
        }
    }

    private boolean isSelectQuery(String sql) {
        String cleaned = cleanSqlForDetection(sql);
        return cleaned.startsWith("select") ||
                cleaned.startsWith("with") ||
                cleaned.startsWith("values") ||
                cleaned.startsWith("show") ||
                cleaned.startsWith("describe") ||
                cleaned.startsWith("explain");
    }

    private boolean isWrappable(String sql) {
        String cleaned = cleanSqlForDetection(sql);
        return cleaned.startsWith("select") ||
                cleaned.startsWith("with") ||
                cleaned.startsWith("values");
    }

    private String cleanSqlForDetection(String sql) {
        return sql.replaceAll("(?s)/\\*.*?\\*/|--.*?(\\r?\\n|$)", " ").trim().toLowerCase();
    }

    private boolean hasPagination(String sql) {
        String lower = sql.toLowerCase();
        return lower.matches("(?s).*\\blimit\\s+\\d+.*");
    }
}