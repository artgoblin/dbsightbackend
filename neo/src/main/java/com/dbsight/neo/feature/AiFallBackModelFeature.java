package com.dbsight.neo.feature;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dbsight.neo.dto.QueryIntent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AiFallBackModelFeature {

    private final QueryIntentExtractor primaryExtractor;
    private final QueryIntentExtractor fallbackExtractor;
    private final QueryIntentExtractor fallbackExtractor2;
    private final ExecutorService fallbackExecutor;

    public AiFallBackModelFeature(
            @Value("${app.grok.api.key}") String grokApiKey,
            @Value("${app.gemini.api.key}") String geminiApiKey,
            @Value("${app.gemini.model.name}") String geminiModelName,
            @Value("${app.gemini.model.name2}") String geminiModelName2,
            @Value("${app.grok.model.name}") String grokModelName) {
        this.fallbackExecutor = new ThreadPoolExecutor(
                5, // core pool size
                20, // max pool size
                60L, TimeUnit.SECONDS, // keep alive time
                new LinkedBlockingQueue<>(100), // bounded queue
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("fallback-executor-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );

        ChatLanguageModel primaryModel = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(grokApiKey)
                .modelName(grokModelName)
                .temperature(0.0)
                .build();

        ChatLanguageModel fallbackModel = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiModelName)
                .temperature(0.0)
                .build();

        ChatLanguageModel fallbackModel2 = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiModelName2)
                .temperature(0.0)
                .build();

        this.primaryExtractor = dev.langchain4j.service.AiServices.builder(QueryIntentExtractor.class)
                .chatLanguageModel(primaryModel)
                .build();

        this.fallbackExtractor = dev.langchain4j.service.AiServices.builder(QueryIntentExtractor.class)
                .chatLanguageModel(fallbackModel)
                .build();

        this.fallbackExtractor2 = dev.langchain4j.service.AiServices.builder(QueryIntentExtractor.class)
                .chatLanguageModel(fallbackModel2)
                .build();
    }

    @CircuitBreaker(name = "ai-service", fallbackMethod = "executeFallbackChain")
    @Retry(name = "ai-service")
    @TimeLimiter(name = "ai-service")
    public CompletableFuture<QueryIntent> extractIntentWithFallBackAsync(String dialect, String schema, String userQuery) {
        return CompletableFuture.supplyAsync(() -> primaryExtractor.extractIntent(dialect, schema, userQuery));
    }

    public CompletableFuture<QueryIntent> executeFallbackChain(String dialect, String schema, String userQuery, Exception ex) {
        return CompletableFuture.supplyAsync(() -> {
            log.warn("Primary failed: {}. Trying fallback chain...", ex.getMessage());

            try {
                return fallbackExtractor.extractIntent(dialect, schema, userQuery);
            } catch (Exception e1) {
                try {
                    return fallbackExtractor2.extractIntent(dialect, schema, userQuery);
                } catch (Exception e2) {
                    log.error("All models failed", e2);
                    return getDefaultQueryIntent();
                }
            }
        }, fallbackExecutor);
    }

    private QueryIntent getDefaultQueryIntent() {
        return new QueryIntent("TOKEN_EXHAUSTED", "null", "null", "null");
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down fallback executor...");
        fallbackExecutor.shutdown();
        try {
            if (!fallbackExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Forcing shutdown of fallback executor...");
                fallbackExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted during executor shutdown", e);
            fallbackExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
