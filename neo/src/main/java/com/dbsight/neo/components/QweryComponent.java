package com.dbsight.neo.components;

import com.dbsight.neo.database.GetDetailedSchema;
import com.dbsight.neo.feature.ConnectionStatusFeature;
import com.dbsight.neo.feature.DataSourceFactoryFeature;
import com.dbsight.neo.feature.DeleteConnectionFeature;
import com.dbsight.neo.feature.GetDatabaseDetailsFeature;
import com.dbsight.neo.feature.ManualSqlExecutionFeature;
import com.dbsight.neo.feature.NewDbConnectionFeature;
import com.dbsight.neo.feature.PasswordEncryptionFeature;
import com.dbsight.neo.feature.PromptToQweryTransformFeature;
import com.dbsight.neo.feature.ReconnectDatabaseFeature;
import com.dbsight.neo.feature.SavedQueryFeature;
import com.dbsight.neo.helper.UtilHelper;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.dbsight.neo.modal.DatabaseConnectionResponse;
import com.dbsight.neo.modal.DatabaseSchemaResponse;
import com.dbsight.neo.modal.Error;
import com.dbsight.neo.modal.QueryResult;
import com.dbsight.neo.modal.QweryResponse;
import com.dbsight.neo.modal.SaveDatabaseConnectionDetails;
import com.dbsight.neo.modal.SavedQueryRequest;
import com.dbsight.neo.modal.SqlExecutionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QweryComponent {

    private final PromptToQweryTransformFeature sqlGeneratorFeature;
    private final NewDbConnectionFeature newDbConnectionFeature;
    private final GetDatabaseDetailsFeature getDatabaseDetailsFeature;
    private final ConnectionStatusFeature connectionStatusFeature;
    private final GetDetailedSchema getDetailedSchema;
    private final StringRedisTemplate redisTemplate;
    private final ReconnectDatabaseFeature reconnectDatabaseFeature;
    private final ManualSqlExecutionFeature manualSqlExecutionFeature;
    private final DataSourceFactoryFeature dataSourceFactoryFeature;
    private final PasswordEncryptionFeature passwordEncryptionFeature;
    private final DeleteConnectionFeature deleteConnectionFeature;
    private final SavedQueryFeature savedQueryFeature;

    public QweryResponse query(String prompt, String databaseName, Long userId)
            throws JsonMappingException, JsonProcessingException {
        return sqlGeneratorFeature.generateSql(prompt, databaseName, userId);
    }

    public SaveDatabaseConnectionDetails newDbConnection(DatabaseConnectionDetails databaseConnectionDetails,
            Long userId) {
        try {
            databaseConnectionDetails
                    .setPassword(passwordEncryptionFeature.encrypt(databaseConnectionDetails.getPassword()));
            DataSource dataSource = dataSourceFactoryFeature.getOrCreateDataSource(databaseConnectionDetails, userId);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            newDbConnectionFeature.newDbConnection(databaseConnectionDetails, userId, jdbcTemplate);
            SaveDatabaseConnectionDetails saveDatabaseConnectionDetails = new SaveDatabaseConnectionDetails();
            saveDatabaseConnectionDetails.setId(databaseConnectionDetails.getId());
            saveDatabaseConnectionDetails.setDbName(databaseConnectionDetails.getDatabaseName());
            saveDatabaseConnectionDetails.setDbType(databaseConnectionDetails.getDbType());
            saveDatabaseConnectionDetails.setDbPort(databaseConnectionDetails.getPort());
            saveDatabaseConnectionDetails.setError(new Error("Success", null));

            return saveDatabaseConnectionDetails;
        } catch (Exception e) {
            SaveDatabaseConnectionDetails saveDatabaseConnectionDetails = new SaveDatabaseConnectionDetails();
            saveDatabaseConnectionDetails.setError(new Error(e.getCause().getMessage(), "error"));
            return saveDatabaseConnectionDetails;
        }
    }

    public List<DatabaseConnectionResponse> getAllConnections(Long userId) {
        return getDatabaseDetailsFeature.getAllConnections(userId);
    }

    public String connectionStatus(String databaseName, Long userId) {
        return connectionStatusFeature.connectionStatus(databaseName, userId);
    }

    public DatabaseSchemaResponse getSchemaDetails(String databaseName, Long userId) {
        try {
            DatabaseConnectionDetails databaseConnectionDetails = UtilHelper.getConnectionDetailsFromRedis(
                    userId, databaseName,
                    redisTemplate);
            DataSource dataSource = dataSourceFactoryFeature.getOrCreateDataSource(databaseConnectionDetails, userId);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            DatabaseSchemaResponse databaseSchemaResponse = new DatabaseSchemaResponse();
            databaseSchemaResponse.setDatabaseName(databaseName);
            databaseSchemaResponse.setDatabaseSchema(getDetailedSchema.getDetailedSchema(jdbcTemplate));
            databaseSchemaResponse.setError(new Error("Success", null));
            return databaseSchemaResponse;
        } catch (Exception e) {
            DatabaseSchemaResponse databaseSchemaResponse = new DatabaseSchemaResponse();
            databaseSchemaResponse.setError(new Error("Database Disconnected, Please Reconnect", "error"));
            return databaseSchemaResponse;
        }
    }

    public String reconnect(String databaseName, Long userId) throws JsonMappingException, JsonProcessingException {
        return reconnectDatabaseFeature.reconnect(databaseName, userId);
    }

    public QueryResult executeSql(SqlExecutionRequest sqlExecutionRequest, Long userId, Integer offset, Integer limit)
            throws JsonMappingException, JsonProcessingException {
        DatabaseConnectionDetails databaseConnectionDetails = UtilHelper.getConnectionDetailsFromRedis(
                userId,
                sqlExecutionRequest.getDatabaseName(),
                redisTemplate);
        QueryResult queryResult = new QueryResult();
        if (databaseConnectionDetails == null) {
            queryResult.setError(new Error("Database Disconnected, Please Reconnect", "error"));
            return queryResult;
        }
        return manualSqlExecutionFeature.executeSql(userId, sqlExecutionRequest, offset, limit);
    }

    public List<String> getAllQueryCache(Long userId) {
        String historyKey = "Sql::" + userId;
        return redisTemplate.opsForList().range(historyKey, 0, 9);
    }

    public String deleteConnection(String databaseName, Long userId) {
        return deleteConnectionFeature.deleteConnection(databaseName, userId);
    }

    public String saveQuery(SavedQueryRequest request, Long userId) {
        return savedQueryFeature.saveQuery(request, userId);
    }

    public List<SavedQueryRequest> getAllQueries(Long userId) {
        return savedQueryFeature.getAllQueries(userId);
    }

    public String updateQuery(Long id, SavedQueryRequest request, Long userId) {
        return savedQueryFeature.updateQuery(id, request, userId);
    }

    public String deleteQuery(Long id, Long userId) {
        return savedQueryFeature.deleteQuery(id, userId);
    }

    public List<SavedQueryRequest> getSearchedQueries(String search, Long userId) {
        return savedQueryFeature.getSearchedQueries(search, userId);
    }
}
