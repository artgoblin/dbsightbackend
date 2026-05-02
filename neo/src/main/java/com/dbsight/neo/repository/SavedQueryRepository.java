package com.dbsight.neo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dbsight.neo.entity.SavedQueryEntity;

@Repository
public interface SavedQueryRepository extends JpaRepository<SavedQueryEntity, Long> {
    List<SavedQueryEntity> findByUserId(Long userId);

    Optional<SavedQueryEntity> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    @Query(value = "SELECT * FROM usercredentials.savedquery WHERE user_id = :userId AND search_vector @@ websearch_to_tsquery('english', :search) ORDER BY ts_rank(search_vector, websearch_to_tsquery('english', :search)) DESC", nativeQuery = true)
    List<SavedQueryEntity> findByUserIdAndSeachTerms(String search, Long userId);
}
