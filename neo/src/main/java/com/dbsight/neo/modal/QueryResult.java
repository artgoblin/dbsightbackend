package com.dbsight.neo.modal;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class QueryResult {
    private List<Map<String, Object>> result;
    private int rowCount;
    private boolean hasMore;
    private long executionTime;
    private Error error;
}
