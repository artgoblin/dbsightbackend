package com.dbsight.neo.feature;

import com.dbsight.neo.dto.QueryIntent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface QueryIntentExtractor {

    @SystemMessage("""
            Role: DB intent parser.
            
            Rules:
            1. Use ONLY tables/columns from schema. If missing, set sqlQweryString="INVALID_QUERY" and explain in summary.
            2. JOINS MUST use ONLY schema-defined relationships (FK to PK). Never guess joins. Fully qualify columns.
            3. Action must be DDL, DML, DCL, or TCL set all this values to action respectively.
            4. Summary must explain table selection, join logic, filters, and aggregations.
            5. Chart rules: "bar"(comparisons), "line"(trends), "pie"(proportions), "scatter"(relationships), user can ask for chart or not.
            """)
    @UserMessage("Dialect: {{dialect}}\n\nSchema: {{schema}}\n\nQuery: {{userQuery}}")
    QueryIntent extractIntent(@V("dialect") String dialect, @V("schema") String schema, @V("userQuery") String userQuery);
}
