package com.dbsight.neo.modal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseConnectionDetails {

    @JsonProperty("id")
    private Long id;
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
    @JsonProperty("password")
    private String password;
    @JsonProperty("database_name")
    private String databaseName;

}
