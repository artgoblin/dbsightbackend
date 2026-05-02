package com.dbsight.neo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseConnectionDetailsMapper {

    private String dbType;
    private String dbUrl;
    private String dbPort;
    private String dbUsername;
    private String dbPassword;
    private String dbName;

}
