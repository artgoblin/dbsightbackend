package com.dbsight.neo.feature;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dbsight.neo.entity.DatabaseConnectionDetailsEntity;
import com.dbsight.neo.modal.DatabaseConnectionResponse;
import com.dbsight.neo.repository.GetAllDatabaseDetailsRepository;

@Component
public class GetDatabaseDetailsFeature {

    @Autowired
    private GetAllDatabaseDetailsRepository getAllDatabaseDetailsRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<DatabaseConnectionResponse> getAllConnections(Long userId) {
        List<DatabaseConnectionDetailsEntity> list = getAllDatabaseDetailsRepository.findByUserId(userId);
        List<DatabaseConnectionResponse> response = new ArrayList<>();
        for (DatabaseConnectionDetailsEntity entity : list) {
            DatabaseConnectionResponse databaseConnectionResponse = new DatabaseConnectionResponse();
            databaseConnectionResponse.setConnectionName(entity.getConnectionName());
            databaseConnectionResponse.setDatabaseName(entity.getDbName());
            databaseConnectionResponse.setHost(entity.getDbUrl());
            databaseConnectionResponse.setUsername(entity.getDbUsername());
            databaseConnectionResponse.setDbType(entity.getDbType());
            databaseConnectionResponse.setPort(entity.getDbPort());
            String isDbPresent = redisTemplate.opsForValue()
                    .get("dbConnections::" + userId + ":" + entity.getDbName());
            databaseConnectionResponse.setConnected(isDbPresent != null);
            response.add(databaseConnectionResponse);
        }
        return response;
    }
}
