package com.dbsight.neo.feature;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.dbsight.neo.modal.DatabaseConnectionDetails;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSourceFactoryFeature {

    private final PasswordEncryptionFeature passwordEncryptionFeature;
    private final ConcurrentHashMap<String, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();

    // Key: userId:databaseName
    private String generateKey(Long userId, String databaseName) {
        return userId + ":" + databaseName;
    }

    public DataSource getOrCreateDataSource(DatabaseConnectionDetails details, Long userId) {
        String key = generateKey(userId, details.getDatabaseName());

        return dataSourceCache.computeIfAbsent(key, k -> {
            try {
                return createAndConfigureDataSource(details);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create DataSource for " + key, e);
            }
        });
    }

    public HikariDataSource createAndConfigureDataSource(DatabaseConnectionDetails details) {

        String url = "jdbc:" + details.getDbType().toLowerCase() + "://" +
                details.getHost() + ":" +
                details.getPort() + "/" +
                details.getDatabaseName();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(details.getUsername());
        config.setPassword(
                passwordEncryptionFeature.decrypt(details.getPassword()));
        config.setMaximumPoolSize(3); // Never exceed DB max_connections
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        if ("Adventureworks".equalsIgnoreCase(details.getDatabaseName())) {
               config.setReadOnly(true);
        }
      

        // Validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setPoolName("pool-" + details.getDatabaseName() + "-" + details.getHost());

        return new HikariDataSource(config);
    }

    public void closeDataSource(Long userId, String databaseName) {
        String key = generateKey(userId, databaseName);
        HikariDataSource ds = dataSourceCache.remove(key);
        if (ds != null && !ds.isClosed()) {
            ds.close(); // Gracefully shutdown pool
        }
    }

    public void closeAll() {
        dataSourceCache.values().forEach(ds -> {
            if (!ds.isClosed())
                ds.close();
        });
        dataSourceCache.clear();
    }
}
