package com.dbsight.neo.feature;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dbsight.neo.modal.QueryResult;
import com.dbsight.neo.modal.SqlExecutionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ManualSqlExecutionFeature {
    private final SqlQweryExecuteFeature sqlQweryExecuteFeature;
    private final StringRedisTemplate redisTemplate;

    public QueryResult executeSql(Long userId, SqlExecutionRequest sqlExecutionRequest,Integer offset,Integer limit)
            throws JsonMappingException, JsonProcessingException {
        String historyKey = "Sql::" + userId;
        String sql = sqlExecutionRequest.getSql();
        QueryResult queryResult = sqlQweryExecuteFeature.execute(sql, sqlExecutionRequest.getDatabaseName(),
                userId,offset,limit);

        redisTemplate.opsForList().remove(historyKey, 1, sql);
        redisTemplate.opsForList().leftPush(historyKey, sql);
        redisTemplate.opsForList().trim(historyKey, 0, 9);
        return queryResult;
    }

}