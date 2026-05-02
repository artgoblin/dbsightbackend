package com.dbsight.neo.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Relationship {
    private String sourceTable;
    private String sourceColumn;
    private String targetTable;
    private String targetColumn;
}