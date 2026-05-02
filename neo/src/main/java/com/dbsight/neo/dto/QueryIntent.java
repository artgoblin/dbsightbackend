package com.dbsight.neo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueryIntent {
    private String action;
    private String sqlQweryString;
    private String summary;
    private String chartType;
}