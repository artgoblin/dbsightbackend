package com.dbsight.neo.feature;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dbsight.neo.database.GetDetailedSchema;
import com.dbsight.neo.dto.DatabaseSchema;
import com.dbsight.neo.entity.DatabaseConnectionDetailsEntity;
import com.dbsight.neo.entity.User;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.dbsight.neo.repository.NewDbConnectionRepository;

import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NewDbConnectionFeature {

    private final NewDbConnectionRepository newDbConnectionRepository;
    private final EmbeddingStoreFactoryFeature embeddingStoreFactory;
    private final EmbeddingModel embeddingModel;
    private final GetDetailedSchema getDetailedSchemas;
    
    @Transactional
    @Cacheable(value = "dbConnections", key = "#userId + ':' + #databaseConnectionDetails.databaseName")
    public DatabaseConnectionDetailsEntity newDbConnection(DatabaseConnectionDetails databaseConnectionDetails,
            Long userId, JdbcTemplate jdbcTemplate) {
        DatabaseConnectionDetailsEntity isDbExist = newDbConnectionRepository
                .findByDbNameAndUserId(databaseConnectionDetails.getDatabaseName(), userId);
        if (isDbExist != null) {
            return isDbExist;
        }

        DatabaseSchema schema = getDetailedSchemas.getDetailedSchema(jdbcTemplate);
        SchemaIndexingFeature schemaIndexingFeature = new SchemaIndexingFeature(embeddingStoreFactory, embeddingModel);
        schemaIndexingFeature.initializeSchemaIndex(schema, userId);
        DatabaseConnectionDetailsEntity entity = new DatabaseConnectionDetailsEntity();

        entity.setDbName(databaseConnectionDetails.getDatabaseName());
        entity.setConnectionName(databaseConnectionDetails.getConnectionName());
        User user = new User();
        user.setId(userId);
        entity.setUser(user);
        entity.setDbUrl(databaseConnectionDetails.getHost());
        entity.setDbUsername(databaseConnectionDetails.getUsername());
        entity.setDbPassword(databaseConnectionDetails.getPassword());
        entity.setDbType(databaseConnectionDetails.getDbType());
        entity.setDbPort(databaseConnectionDetails.getPort());

        return newDbConnectionRepository.save(entity);
    }

}