package com.dbsight.neo.modal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SqlExecutionRequest {

    @JsonProperty("database_name")
    private String databaseName;
    @JsonProperty("sql_query")
    private String sql;
}
