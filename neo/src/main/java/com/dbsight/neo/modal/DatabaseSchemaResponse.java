package com.dbsight.neo.modal;

import com.dbsight.neo.dto.DatabaseSchema;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseSchemaResponse {
    @JsonProperty("database_name")
    private String databaseName;
    @JsonProperty("database_schema")
    private DatabaseSchema databaseSchema;
    @JsonProperty("error")
    private Error error;
}
