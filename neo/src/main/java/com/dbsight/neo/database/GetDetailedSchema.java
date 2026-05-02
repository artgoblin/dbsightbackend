package com.dbsight.neo.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dbsight.neo.dto.Column;
import com.dbsight.neo.dto.DatabaseSchema;
import com.dbsight.neo.dto.Relationship;
import com.dbsight.neo.dto.Table;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GetDetailedSchema {
    @Autowired
    private JdbcTemplate defaultJdbcTemplate;

    public DatabaseSchema getDetailedSchema() {
        return extractSchema(defaultJdbcTemplate);
    }

    public DatabaseSchema getDetailedSchema(JdbcTemplate jdbcTemplate) {
        return extractSchema(jdbcTemplate);
    }

    private DatabaseSchema extractSchema(JdbcTemplate jdbcTemplate) {
        DatabaseSchema databaseSchema = new DatabaseSchema();
        Map<String, List<Table>> schemaMap = new HashMap<>();

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            
            // For MySQL/MariaDB, catalog is the database name, and schema is often null.
            // For PostgreSQL, catalog is the database name, and schema is "public" or others.
            String dbName = catalog != null ? catalog : schema;
            databaseSchema.setDatabaseName(dbName);

            // Filter out system schemas
            String schemaPattern = schema;
            if ("public".equals(schema) || schema == null) {
                schemaPattern = null; // Get all schemas if public or null, then filter
            }

            try (ResultSet tables = metaData.getTables(catalog, null, "%", new String[] { "TABLE" })) {
                while (tables.next()) {
                    String tableSchema = tables.getString("TABLE_SCHEM");
                    String tableName = tables.getString("TABLE_NAME");

                    // Filter out system tables/schemas
                    if (isSystemSchema(tableSchema) || isInternalTable(tableName)) {
                        continue;
                    }

                    Table table = new Table();
                    table.setTableName(tableName);
                    
                    // Get Columns
                    List<Column> columns = new ArrayList<>();
                    try (ResultSet cols = metaData.getColumns(catalog, tableSchema, tableName, "%")) {
                        while (cols.next()) {
                            Column col = new Column();
                            col.setColumnName(cols.getString("COLUMN_NAME"));
                            col.setColumnType(cols.getString("TYPE_NAME"));
                            columns.add(col);
                        }
                    }
                    table.setColumns(columns);

                    // Get Primary Keys
                    List<String> pks = new ArrayList<>();
                    try (ResultSet pkResultSet = metaData.getPrimaryKeys(catalog, tableSchema, tableName)) {
                        while (pkResultSet.next()) {
                            pks.add(pkResultSet.getString("COLUMN_NAME"));
                        }
                    }
                    table.setPrimaryKeys(pks);

                    // Get Foreign Keys
                    List<Relationship> fks = new ArrayList<>();
                    try (ResultSet fkResultSet = metaData.getImportedKeys(catalog, tableSchema, tableName)) {
                        while (fkResultSet.next()) {
                            Relationship rel = new Relationship();
                            rel.setSourceTable(tableName);
                            rel.setSourceColumn(fkResultSet.getString("FKCOLUMN_NAME"));
                            rel.setTargetTable(fkResultSet.getString("PKTABLE_NAME"));
                            rel.setTargetColumn(fkResultSet.getString("PKCOLUMN_NAME"));
                            fks.add(rel);
                        }
                    }
                    table.setForeignKeys(fks);

                    // Unique columns (using IndexInfo)
                    List<String> uniqueCols = new ArrayList<>();
                    try (ResultSet indexInfo = metaData.getIndexInfo(catalog, tableSchema, tableName, true, false)) {
                        while (indexInfo.next()) {
                            String columnName = indexInfo.getString("COLUMN_NAME");
                            if (columnName != null && !pks.contains(columnName)) {
                                uniqueCols.add(columnName);
                            }
                        }
                    }
                    table.setUniqueColumns(uniqueCols);

                    String effectiveSchema = tableSchema != null ? tableSchema : "default";
                    schemaMap.computeIfAbsent(effectiveSchema, k -> new ArrayList<>()).add(table);
                }
            }
        } catch (SQLException e) {
            log.error("Error extracting database schema", e);
        }

        databaseSchema.setDatabaseSchema(schemaMap);
        return databaseSchema;
    }

    private boolean isSystemSchema(String schema) {
        if (schema == null) return false;
        String s = schema.toLowerCase();
        return s.equals("information_schema") || s.equals("pg_catalog") || s.equals("pg_toast") || s.startsWith("pg_") || s.equals("mysql") || s.equals("performance_schema") || s.equals("sys");
    }

    private boolean isInternalTable(String tableName) {
        if (tableName == null) return false;
        String t = tableName.toLowerCase();
        return t.equals("usercredentials") || t.equals("databaseconnectiondetails");
    }
}