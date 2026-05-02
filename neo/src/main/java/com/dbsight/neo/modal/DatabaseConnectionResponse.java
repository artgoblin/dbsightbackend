package com.dbsight.neo.modal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

    
@Getter
@Setter
public class DatabaseConnectionResponse {

    @JsonProperty("connection_name")
    private String connectionName;
    @JsonProperty("db_type")
    private String dbType;
    @JsonProperty("host")
    private String host;
    @JsonProperty("port")
    private String port;
    @JsonProperty("username")
    private String username;
    @JsonProperty("database_name")
    private String databaseName;
    @JsonProperty("is_connected")
    private boolean connected;

}
