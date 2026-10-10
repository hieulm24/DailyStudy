package com.englishlearning.repository;

import com.englishlearning.entity.ItChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItChatMessageRepository extends JpaRepository<ItChatMessage, Long> {

    List<ItChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
