package com.dbsight.neo.feature;

import org.springframework.stereotype.Component;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

@Component
public class EmbeddingStoreFactoryFeature {

    public EmbeddingStore<TextSegment> getStore(String databaseName, Long userId) {
        return PgVectorEmbeddingStore.builder()
                .host("pgvector")
                .port(5432)
                .user("${DB_USERNAME}")
                .password("${DB_PASSWORD}")
                .database("vectordb")
                .table("embeddings_" + userId + "_" + databaseName)
                .dimension(768)
                .build();
    }
}
