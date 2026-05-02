package com.dbsight.neo.feature;

import org.springframework.stereotype.Component;

import com.dbsight.neo.dto.QueryIntent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiSetupFeature {

  private final AiFallBackModelFeature aiFallBackModelFeature;
  private final RateLimitFeature rateLimitFeature;

  public QueryIntent getPromptIntent(String dialect, String userQuery, String schema, Long userId) {
    try {
      rateLimitFeature.checkLimit(userId);
      return aiFallBackModelFeature.extractIntentWithFallBackAsync(dialect, schema, userQuery).join();
    } catch (Exception e) {
      String message = getRootMessage(e);

      if (message != null && message.contains("Daily AI limit reached")) {
        return new QueryIntent(null, "TOKEN_EXHAUSTED", message, null);
      }
      return new QueryIntent(null, "Error extracting query intent: " + e.getMessage(), null, null);
    }
  }
  
  private String getRootMessage(Throwable throwable) {
    Throwable rootCause = throwable;

    while (rootCause.getCause() != null) {
        rootCause = rootCause.getCause();
    }

    return rootCause.getMessage();
}
}
