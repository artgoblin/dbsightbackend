package com.dbsight.neo.service;

import java.util.List;
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

public interface QweryService {

    public QweryResponse query(String prompt, String databaseName, Long userId)
            throws JsonMappingException, JsonProcessingException;

    public SaveDatabaseConnectionDetails newDbConnection(DatabaseConnectionDetails databaseConnectionDetails,
            Long userId);

    public List<DatabaseConnectionResponse> getAllConnections(Long userId);

    public String connectionStatus(String databaseName, Long userId);

    public DatabaseSchemaResponse getSchemaDetails(String databaseName, Long userId);

    public String reconnect(String databaseName, Long userId) throws JsonMappingException, JsonProcessingException;

    public QueryResult executeSql(SqlExecutionRequest sqlExecutionRequest, Long userId,Integer offset,Integer limit)
            throws JsonMappingException, JsonProcessingException;

    public List<String> getAllQueryCache(Long userId);

    public String deleteConnection(String databaseName, Long userId);

    public String saveQuery(SavedQueryRequest request, Long userId);

    public List<SavedQueryRequest> getAllQueries(Long userId);

    public String updateQuery(Long id, SavedQueryRequest request, Long userId);

    public String deleteQuery(Long id, Long userId);

    public List<SavedQueryRequest> getSearchedQueries(String search, Long userId);

}
