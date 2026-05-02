package com.dbsight.neo.feature;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dbsight.neo.entity.DatabaseConnectionDetailsEntity;
import com.dbsight.neo.helper.UtilHelper;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.dbsight.neo.repository.NewDbConnectionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReconnectDatabaseFeature {

    private final NewDbConnectionFeature newDbConnectionFeature;
    private final NewDbConnectionRepository newDbConnectionRepository;
    private final StringRedisTemplate redisTemplate;
    private final DataSourceFactoryFeature dataSourceFactoryFeature;

    public String reconnect(String databaseName, Long userId) throws JsonMappingException, JsonProcessingException {
        DatabaseConnectionDetails databaseConnectionDetails = UtilHelper.getConnectionDetailsFromRedis(userId,
                databaseName,
                redisTemplate);
        if (databaseConnectionDetails != null && databaseConnectionDetails.getDatabaseName() != null
                && databaseConnectionDetails.getHost() != null
                && databaseConnectionDetails.getPort() != null && databaseConnectionDetails.getUsername() != null
                && databaseConnectionDetails.getPassword() != null && databaseConnectionDetails.getDbType() != null) {
            new JdbcTemplate(dataSourceFactoryFeature.getOrCreateDataSource(databaseConnectionDetails,userId));
        } else {
            DatabaseConnectionDetailsEntity databaseConnectionDetailsEntity = newDbConnectionRepository
                    .findByDbNameAndUserId(databaseName, userId);
            databaseConnectionDetails = new DatabaseConnectionDetails();
            databaseConnectionDetails.setId(databaseConnectionDetailsEntity.getId());
            databaseConnectionDetails.setDatabaseName(databaseConnectionDetailsEntity.getDbName());
            databaseConnectionDetails.setHost(databaseConnectionDetailsEntity.getDbUrl());
            databaseConnectionDetails.setUsername(databaseConnectionDetailsEntity.getDbUsername());
            databaseConnectionDetails.setPassword(databaseConnectionDetailsEntity.getDbPassword());
            databaseConnectionDetails.setDbType(databaseConnectionDetailsEntity.getDbType());
            databaseConnectionDetails.setPort(databaseConnectionDetailsEntity.getDbPort());
            newDbConnectionFeature.newDbConnection(databaseConnectionDetails,userId, new JdbcTemplate(dataSourceFactoryFeature.getOrCreateDataSource(databaseConnectionDetails,userId)));
        }
        return "Reconnected";
    }
}
