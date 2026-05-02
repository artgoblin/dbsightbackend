package com.dbsight.neo.modal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveDatabaseConnectionDetails {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("db_name")
    private String dbName; 
    @JsonProperty("db_url")
    private String dbUrl;
    @JsonProperty("db_username")
    private String dbUsername;
    @JsonProperty("db_password")
    private String dbPassword;
    @JsonProperty("db_type")
    private String dbType;
    @JsonProperty("db_port")
    private String dbPort;
    @JsonProperty("error")
    private Error error;
    
}
