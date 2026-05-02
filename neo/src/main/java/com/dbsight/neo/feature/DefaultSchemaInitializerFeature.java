package com.dbsight.neo.feature;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.dbsight.neo.database.GetDetailedSchema;
import com.dbsight.neo.dto.DatabaseSchema;

@Component
@RequiredArgsConstructor
public class DefaultSchemaInitializerFeature {

    private final SchemaIndexingFeature schemaIndexingFeature;
    private final GetDetailedSchema getDetailedSchema;

    @PostConstruct
    public void init() {
        Long userId = 1L;
        DatabaseSchema schema = getDetailedSchema.getDetailedSchema();
        schemaIndexingFeature.initializeSchemaIndex(schema,userId);

        System.out.println("✅ Default DB schema indexed at startup");
    }
}