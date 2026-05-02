package com.dbsight.neo.modal;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
public class QweryResponse {

    @JsonProperty("action")
    private String action;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("sql_query")
    private String sqlQuery;

    @JsonProperty("result")
    private QueryResult result;

    @JsonProperty("chart_type")
    private String chartType;

}
