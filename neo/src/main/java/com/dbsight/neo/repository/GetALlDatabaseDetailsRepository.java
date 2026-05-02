package com.dbsight.neo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dbsight.neo.entity.DatabaseConnectionDetailsEntity;

@Repository
public interface GetAllDatabaseDetailsRepository extends JpaRepository<DatabaseConnectionDetailsEntity, Long> {
    List<DatabaseConnectionDetailsEntity> findByUserId(Long userId);
}
