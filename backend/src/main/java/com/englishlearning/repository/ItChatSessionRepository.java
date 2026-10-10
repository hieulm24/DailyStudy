package com.englishlearning.repository;

import com.englishlearning.entity.ItChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItChatSessionRepository extends JpaRepository<ItChatSession, Long> {

    List<ItChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<ItChatSession> findByIdAndUserId(Long id, Long userId);
}
