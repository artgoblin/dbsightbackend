package com.dbsight.neo.modal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Prompt {

    @JsonProperty("text")
    String promptText;

    @JsonProperty("database_name")
    String databaseName;
    
}
