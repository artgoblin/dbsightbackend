package com.dbsight.neo.feature;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.dbsight.neo.dto.DatabaseSchema;
import com.dbsight.neo.dto.Table;
import com.dbsight.neo.helper.UtilHelper;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaIndexingFeature {

    private final EmbeddingStoreFactoryFeature storeFactory;
    private final EmbeddingModel embeddingModel;

    public void initializeSchemaIndex(DatabaseSchema schema,Long userId) {
        
        EmbeddingStore<TextSegment> store =
                storeFactory.getStore(schema.getDatabaseName(),userId);
        List<TextSegment> segments = new ArrayList<>();

        schema.getDatabaseSchema().forEach((schemaName, tables) -> {
            for (Table table : tables) {
                String content = UtilHelper.buildSchemaContent(schema.getDatabaseName(), schemaName, table);

                Metadata metadata = Metadata.metadata("tableName", table.getTableName())
                        .add("schemaName", schemaName)
                        .add("database", schema.getDatabaseName())
                        .add("userId", userId)
                        .add("primaryKeys", table.getPrimaryKeys())
                        .add("uniqueColumns", table.getUniqueColumns())
                        .add("foreignKeys", table.getForeignKeys())
                        .add("tableId", schemaName + "." + table.getTableName());

                segments.add(TextSegment.from(content, metadata));
            }
        });
        if (!segments.isEmpty()) {
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            store.addAll(embeddings, segments);
            System.out.println("Indexed " + segments.size() + " tables into PG Vector Store.");
        }
    }
}
