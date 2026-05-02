package com.dbsight.neo.modal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SavedQueryRequest {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("query")
    private String query;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("database_name")
    private String databaseName;
}
