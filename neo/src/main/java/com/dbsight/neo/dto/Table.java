package com.dbsight.neo.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
public class Table {
    private String tableName;
    private List<Column> columns;
    private List<String> primaryKeys;
    private List<String> uniqueColumns;
    private List<Relationship> foreignKeys;
}
