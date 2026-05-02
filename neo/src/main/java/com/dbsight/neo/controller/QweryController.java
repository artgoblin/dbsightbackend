package com.dbsight.neo.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dbsight.neo.helper.CustomUserDetailsHelper;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.dbsight.neo.modal.DatabaseConnectionResponse;
import com.dbsight.neo.modal.DatabaseSchemaResponse;
import com.dbsight.neo.modal.Error;
import com.dbsight.neo.modal.Prompt;
import com.dbsight.neo.modal.QueryResult;
import com.dbsight.neo.modal.QweryResponse;
import com.dbsight.neo.modal.SaveDatabaseConnectionDetails;
import com.dbsight.neo.modal.SavedQueryRequest;
import com.dbsight.neo.modal.SqlExecutionRequest;
import com.dbsight.neo.service.QweryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;;

@RestController
@RequestMapping("/ask")
@RequiredArgsConstructor
public class QweryController {

    private final QweryService qweryService;

    @PostMapping("/query")
    public QweryResponse query(@RequestBody Prompt prompt,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        try {
            return qweryService.query(prompt.getPromptText(), prompt.getDatabaseName(), userDetails.getUserId());
        } catch (JsonProcessingException e) {
            QweryResponse qweryResponse = new QweryResponse();
            QueryResult error = new QueryResult();
            error.setError(new Error(e.getMessage(), "error"));
            qweryResponse.setResult(error);
            return qweryResponse;
        }
    }

    @PostMapping("/newdbconnection")
    @ResponseStatus(HttpStatus.CREATED)
    public SaveDatabaseConnectionDetails newDbConnection(
            @RequestBody DatabaseConnectionDetails databaseConnectionDetails,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.newDbConnection(databaseConnectionDetails, userDetails.getUserId());
    }

    @GetMapping("/getallconnections")
    public List<DatabaseConnectionResponse> getAllConnections(
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.getAllConnections(userDetails.getUserId());
    }

    @GetMapping("/connectionstatus")
    public String connectionStatus(@RequestParam String databaseName,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.connectionStatus(databaseName, userDetails.getUserId());
    }

    @GetMapping("/reconnect/{databaseName}")
    public String reconnect(@PathVariable String databaseName,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        try {
            return qweryService.reconnect(databaseName, userDetails.getUserId());
        } catch (JsonProcessingException e) {
            return "Unable to Connect" + e.getMessage();
        }
    }

    @GetMapping("/getschemadetails/{databaseName}")
    public DatabaseSchemaResponse getSchemaDetails(@PathVariable String databaseName,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.getSchemaDetails(databaseName, userDetails.getUserId());
    }

    @PostMapping("/executesql")
    public QueryResult executeSql(@RequestBody SqlExecutionRequest request,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit)
            throws JsonMappingException, JsonProcessingException {
        return qweryService.executeSql(request, userDetails.getUserId(), offset, limit);
    }

    @GetMapping("/getallquerycache")
    public List<String> getAllQueryCache(@AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.getAllQueryCache(userDetails.getUserId());
    }

    @DeleteMapping("/deleteconnection/{databaseName}")
    public String deleteConnection(@PathVariable String databaseName,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.deleteConnection(databaseName, userDetails.getUserId());
    }

    @PostMapping("/savedquery")
    public ResponseEntity<Map<String, String>> saveQuery(@RequestBody SavedQueryRequest request,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        String response = qweryService.saveQuery(request, userDetails.getUserId());
        if (response.equals("Query Saved")) {
            return ResponseEntity.ok(Map.of(
                    "message", "Query Saved"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Query Not Saved"));
        }
    }

    @GetMapping("/getallsavedqueries")
    public List<SavedQueryRequest> getAllQueries(@AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.getAllQueries(userDetails.getUserId());
    }

    @GetMapping("/getsearchedqueries")
    public List<SavedQueryRequest> getSearchedQueries(@RequestParam String search, @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.getSearchedQueries(search, userDetails.getUserId());
    }

    @PutMapping("/savedquery/{id}")
    public ResponseEntity<Map<String, String>> updateQuery(@PathVariable Long id,
            @RequestBody SavedQueryRequest request,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        String response = qweryService.updateQuery(id, request, userDetails.getUserId());
        if (response.equals("Query Updated")) {
            return ResponseEntity.ok(Map.of(
                    "message", "Query Updated"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Query Not Updated"));
        }
    }

    @DeleteMapping("/savedquery/{id}")
    public String deleteQuery(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetailsHelper userDetails) {
        return qweryService.deleteQuery(id, userDetails.getUserId());
    }

}
