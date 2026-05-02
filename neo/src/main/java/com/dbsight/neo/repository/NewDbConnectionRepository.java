package com.dbsight.neo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dbsight.neo.entity.DatabaseConnectionDetailsEntity;

@Repository
public interface NewDbConnectionRepository extends JpaRepository<DatabaseConnectionDetailsEntity, Long> {
    
    DatabaseConnectionDetailsEntity findByDbNameAndUserId(String dbName, Long userId);
    void deleteByUserIdAndDbName(Long userId, String dbName);
}
