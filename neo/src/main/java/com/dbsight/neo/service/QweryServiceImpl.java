package com.dbsight.neo.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.dbsight.neo.components.QweryComponent;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.dbsight.neo.modal.DatabaseConnectionResponse;
import com.dbsight.neo.modal.DatabaseSchemaResponse;
import com.dbsight.neo.modal.QueryResult;
import com.dbsight.neo.modal.QweryResponse;
import com.dbsight.neo.modal.SaveDatabaseConnectionDetails;
import com.dbsight.neo.modal.SavedQueryRequest;
import com.dbsight.neo.modal.SqlExecutionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
@RequiredArgsConstructor
public class QweryServiceImpl implements QweryService {

    private final QweryComponent qweryComponent;

    @Override
    public QweryResponse query(String prompt, String databaseName, Long userId) throws JsonMappingException, JsonProcessingException {
        return qweryComponent.query(prompt, databaseName,userId);
    }

    @Override
    public SaveDatabaseConnectionDetails newDbConnection(DatabaseConnectionDetails databaseConnectionDetails, Long userId) {
        return qweryComponent.newDbConnection(databaseConnectionDetails,userId);

    }

    @Override
    public List<DatabaseConnectionResponse> getAllConnections(Long userId) {
        return qweryComponent.getAllConnections(userId);
    }

    @Override
    public String connectionStatus(String databaseName, Long userId) {
        return qweryComponent.connectionStatus(databaseName,userId);
    }

    @Override
    public DatabaseSchemaResponse getSchemaDetails(String databaseName, Long userId) {
        return qweryComponent.getSchemaDetails(databaseName,userId);
    }

    @Override
    public String reconnect(String databaseName, Long userId) throws JsonMappingException, JsonProcessingException {
        return qweryComponent.reconnect(databaseName,userId);
    }

    @Override
    public QueryResult executeSql(SqlExecutionRequest sqlExecutionRequest, Long userId,Integer offset,Integer limit) throws JsonMappingException, JsonProcessingException {
        return qweryComponent.executeSql(sqlExecutionRequest,userId,offset,limit);
    }

    @Override
    public List<String> getAllQueryCache(Long userId) {
        return qweryComponent.getAllQueryCache(userId);
    }

    @Override
    public String deleteConnection(String databaseName, Long userId) {
        return qweryComponent.deleteConnection(databaseName,userId);
    }

    @Override
    public String saveQuery(SavedQueryRequest request, Long userId) {
        return qweryComponent.saveQuery(request,userId);
    }

    @Override
    public List<SavedQueryRequest> getAllQueries(Long userId) {
        return qweryComponent.getAllQueries(userId);
    }

    @Override
    public String updateQuery(Long id, SavedQueryRequest request, Long userId) {
        return qweryComponent.updateQuery(id, request, userId);
    }

    @Override
    public String deleteQuery(Long id, Long userId) {
        return qweryComponent.deleteQuery(id, userId);
    }

    @Override
    public List<SavedQueryRequest> getSearchedQueries(String search, Long userId) {
        return qweryComponent.getSearchedQueries(search, userId);
    }

}
