package com.dbsight.neo.feature;

import javax.sql.DataSource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dbsight.neo.helper.UtilHelper;
import com.dbsight.neo.modal.DatabaseConnectionDetails;
import com.dbsight.neo.repository.NewDbConnectionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteConnectionFeature {

    private final StringRedisTemplate redisTemplate;
    private final NewDbConnectionRepository newDbConnectionRepository;
    private final DataSourceFactoryFeature dataSourceFactoryFeature;
    private final PasswordEncryptionFeature passwordEncryptionFeature;

    @Transactional
    public String deleteConnection(String databaseName, Long userId) {
        try {
            DatabaseConnectionDetails details = UtilHelper.getConnectionDetailsFromRedis(userId, databaseName,
                    redisTemplate);

            if (details == null) {
                return "Connection not found";
            }

            // Delete from main DB
             newDbConnectionRepository.deleteByUserIdAndDbName(userId, databaseName);

            // Delete from Redis
             redisTemplate.delete("dbConnections::" + userId + ":" + databaseName);

            // Setup pgvector datasource
            DatabaseConnectionDetails pgDetails = new DatabaseConnectionDetails();
            pgDetails.setHost("pgvector");
            pgDetails.setPort("5432");
            pgDetails.setDatabaseName("${DB_NAME}");
            pgDetails.setUsername("${DB_USERNAME}");
            pgDetails.setPassword(passwordEncryptionFeature.encrypt("${DB_PASSWORD}"));
            pgDetails.setDbType("postgresql");

            DataSource pgDs = dataSourceFactoryFeature.getOrCreateDataSource(pgDetails,userId);
            JdbcTemplate pgJdbcTemplate = new JdbcTemplate(pgDs);

            String table = "embeddings_" + userId + "_" + databaseName.toLowerCase();

            String sql = "DELETE FROM " + table +
                    " WHERE metadata->>'database' = " +"'"+ databaseName +"'"+ " AND metadata->>'userId' = " +"'"+ userId +"'";

            int rowsDeleted = pgJdbcTemplate.update(sql);

            log.info("Deleted {} vectors from pgvector for db: {}", rowsDeleted, databaseName);

            return "Connection deleted successfully";

        } catch (Exception e) {
            log.error("Error deleting connection", e);
            return "Error deleting connection: " + e.getMessage();
        }
    }
}