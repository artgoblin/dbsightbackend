package com.dbsight.neo.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
public class DatabaseSchema { 
    private String databaseName;
    private Map<String, List<Table>> databaseSchema;  
}
