package com.dbsight.neo.helper;

import java.util.List;


import org.springframework.data.redis.core.StringRedisTemplate;

import com.dbsight.neo.dto.DatabaseConnectionDetailsMapper;
import com.dbsight.neo.dto.Table;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

public class UtilHelper {
    public static DatabaseConnectionDetails getConnectionDetailsFromRedis(
            Long userId, String databaseName,
            StringRedisTemplate redisTemplate) {

        String key = "dbConnections::" + userId + ":" + databaseName;
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DatabaseConnectionDetailsMapper cached = objectMapper.readValue(json,
                    DatabaseConnectionDetailsMapper.class);

            if (cached == null) {
                return null;
            }

            DatabaseConnectionDetails db = new DatabaseConnectionDetails();
            db.setDatabaseName(cached.getDbName());
            db.setHost(cached.getDbUrl());
            db.setUsername(cached.getDbUsername());
            db.setPassword(cached.getDbPassword());
            db.setDbType(cached.getDbType());
            db.setPort(cached.getDbPort());

            return db;

        } catch (Exception e) {
            // optional: log this, otherwise debugging later = pain
            // log.error("Failed to parse Redis JSON for key {}", key, e);
            return null;
        }
    }



    public static String buildContextFromMatches(List<EmbeddingMatch<TextSegment>> matches) {
        StringBuilder sb = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : matches) {
            sb.append(match.embedded().text());
            sb.append("\n---\n");
        }
        return sb.toString();
    }

    
    public static String buildSchemaContent(String database, String schemaName, Table table) {
        StringBuilder sb = new StringBuilder();
        sb.append("Database: ").append(database).append("\n");
        sb.append("Schema: ").append(schemaName).append("\n");
        sb.append("Table: ").append(table.getTableName()).append("\n");
        sb.append("Primary Keys: ").append(table.getPrimaryKeys()).append("\n");
        sb.append("Unique Columns: ").append(table.getUniqueColumns()).append("\n");
        sb.append("Foreign Keys: ").append(table.getForeignKeys()).append("\n");
        sb.append("Columns:\n");

        table.getColumns().forEach(col -> {
            sb.append("  - ")
                    .append(col.getColumnName())
                    .append(" (")
                    .append(col.getColumnType())
                    .append(")")
                    .append("\n");
        });

        return sb.toString();
    }

}
