package com.englishlearning.repository;

import com.englishlearning.entity.EntityTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityTagRepository extends JpaRepository<EntityTag, Long> {
    List<EntityTag> findByEntityTypeAndEntityId(String entityType, Long entityId);
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
