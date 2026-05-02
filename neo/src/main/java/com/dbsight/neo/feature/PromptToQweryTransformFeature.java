package com.dbsight.neo.feature;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dbsight.neo.dto.QueryIntent;
import com.dbsight.neo.helper.UtilHelper;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.dbsight.neo.modal.Error;
import com.dbsight.neo.modal.QueryResult;
import com.dbsight.neo.modal.QweryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;

@Component
@RequiredArgsConstructor
public class PromptToQweryTransformFeature {

    private final AiSetupFeature aiSetupFeature;
    private final SqlQweryExecuteFeature sqlQweryExecuteFeature;
    private final EmbeddingStoreFactoryFeature embeddingStoreFactory;
    private final EmbeddingModel embeddingModel;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private static final int RELEVANT_TABLES_LIMIT = 5;
    private static final double RELEVANCE_THRESHOLD = 0.65;

    public QweryResponse generateSql(String userPrompt, String databaseName, Long userId)
            throws JsonMappingException, JsonProcessingException {

        DatabaseConnectionDetails connectionDetails = UtilHelper.getConnectionDetailsFromRedis(userId, databaseName, redisTemplate);
        String dialect = connectionDetails != null ? connectionDetails.getDbType() : "PostgreSQL";

        Embedding promptEmbedding = embeddingModel.embed(userPrompt).content();
        EmbeddingStore<TextSegment> embeddingStore = embeddingStoreFactory.getStore(databaseName, userId);
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(promptEmbedding)
                .maxResults(RELEVANT_TABLES_LIMIT)
                .minScore(RELEVANCE_THRESHOLD)
                .build();

        EmbeddingSearchResult<TextSegment> embeddingSearchResult = embeddingStore.search(request);
        List<EmbeddingMatch<TextSegment>> matches = embeddingSearchResult.matches();

        if (matches.isEmpty()) {
            throw new RuntimeException("No relevant database tables found for prompt: " + userPrompt);
        }

        String compactSchema = UtilHelper.buildContextFromMatches(matches);

        QueryIntent queryIntent = aiSetupFeature.getPromptIntent(dialect, userPrompt, compactSchema, userId);

        QueryResult result = null;
        if (queryIntent.getAction() != null && !queryIntent.getAction().equals("DDL") && !queryIntent.getAction().equals("TOKEN_EXHAUSTED")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("update")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("delete")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("insert")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("drop")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("alter")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("truncate")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("create")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("replace")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("call")
                && !queryIntent.getSqlQweryString().toLowerCase().startsWith("invalid_query")) {
            result = sqlQweryExecuteFeature.execute(queryIntent.getSqlQweryString(), databaseName, userId,0,10);
        }

        if(queryIntent.getAction()!=null && queryIntent.getAction().equals("TOKEN_EXHAUSTED")) {
           QweryResponse qweryResponse = new QweryResponse();
           QueryResult error = new QueryResult();
           error.setError(new Error("You have exceeded your daily token limit. Please try again later.","error"));
           qweryResponse.setResult(error);
           return qweryResponse;
        } else if(queryIntent.getAction() == null && !queryIntent.getSummary().equals("TOKEN_EXHAUSTED")) {
          QweryResponse qweryResponse = new QweryResponse();
          QueryResult error = new QueryResult();
          error.setError(new Error("You can have max 5 queries a day. Please try again tomorrow.","error"));
          qweryResponse.setResult(error);
          return qweryResponse;
        }

        QweryResponse qweryResponse = new QweryResponse();
        qweryResponse.setAction(queryIntent.getAction());
        qweryResponse.setSummary(queryIntent.getSummary());
        qweryResponse.setSqlQuery(queryIntent.getSqlQweryString());
        qweryResponse.setResult(result);
        qweryResponse.setChartType(queryIntent.getChartType());

        return qweryResponse;
    }
}