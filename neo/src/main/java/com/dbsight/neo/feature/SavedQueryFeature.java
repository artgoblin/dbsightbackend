package com.dbsight.neo.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dbsight.neo.entity.SavedQueryEntity;
import com.dbsight.neo.modal.SavedQueryRequest;
import com.dbsight.neo.repository.SavedQueryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SavedQueryFeature {
    
    private final SavedQueryRepository savedQueryRepository;

    public String saveQuery(SavedQueryRequest request, Long userId) {
        SavedQueryEntity savedQueryEntity = new SavedQueryEntity();
        savedQueryEntity.setQuery(request.getQuery());
        savedQueryEntity.setTitle(request.getTitle());
        savedQueryEntity.setDescription(request.getDescription());
        savedQueryEntity.setDatabaseName(request.getDatabaseName());
        savedQueryEntity.setUserId(userId);
        savedQueryRepository.save(savedQueryEntity);
        return "Query Saved";
    }

    public List<SavedQueryRequest> getAllQueries(Long userId) {
        List<SavedQueryEntity> savedQueries = savedQueryRepository.findByUserId(userId);
        List<SavedQueryRequest> savedQueryRequests = new ArrayList<>();
        for (SavedQueryEntity savedQuery : savedQueries) {
            SavedQueryRequest savedQueryRequest = new SavedQueryRequest();
            savedQueryRequest.setId(savedQuery.getId());
            savedQueryRequest.setQuery(savedQuery.getQuery());
            savedQueryRequest.setTitle(savedQuery.getTitle());
            savedQueryRequest.setDescription(savedQuery.getDescription());
            savedQueryRequest.setDatabaseName(savedQuery.getDatabaseName());
            savedQueryRequests.add(savedQueryRequest);
        }
        return savedQueryRequests;
    }

    public String updateQuery(Long id, SavedQueryRequest request, Long userId) {
        Optional<SavedQueryEntity> optionalEntity = savedQueryRepository.findByIdAndUserId(id, userId);
        if (optionalEntity.isEmpty()) {
            return "Query not found or access denied";
        }
        SavedQueryEntity entity = optionalEntity.get();
        if (request.getQuery() != null) entity.setQuery(request.getQuery());
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getDatabaseName() != null) entity.setDatabaseName(request.getDatabaseName());
        savedQueryRepository.save(entity);
        return "Query Updated";
    }

    @Transactional
    public String deleteQuery(Long id, Long userId) {
        Optional<SavedQueryEntity> optionalEntity = savedQueryRepository.findByIdAndUserId(id, userId);
        if (optionalEntity.isEmpty()) {
            return "Query not found or access denied";
        }
        savedQueryRepository.deleteByIdAndUserId(id, userId);
        return "Query Deleted";
    }

    public List<SavedQueryRequest> getSearchedQueries(String search, Long userId) {
        List<SavedQueryEntity> savedQueries = savedQueryRepository.findByUserIdAndSeachTerms(search, userId);
        List<SavedQueryRequest> savedQueryRequests = new ArrayList<>();
        for (SavedQueryEntity savedQuery : savedQueries) {
            SavedQueryRequest savedQueryRequest = new SavedQueryRequest();
            savedQueryRequest.setId(savedQuery.getId());
            savedQueryRequest.setQuery(savedQuery.getQuery());
            savedQueryRequest.setTitle(savedQuery.getTitle());
            savedQueryRequest.setDescription(savedQuery.getDescription());
            savedQueryRequest.setDatabaseName(savedQuery.getDatabaseName());
            savedQueryRequests.add(savedQueryRequest);
        }
        return savedQueryRequests;
    }
}
