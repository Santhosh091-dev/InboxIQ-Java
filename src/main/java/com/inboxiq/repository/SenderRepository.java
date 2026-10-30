package com.inboxiq.repository;

import com.inboxiq.entity.Sender;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SenderRepository extends JpaRepository<Sender, Long> {
    List<Sender> findByUserId(Long userId);
    Optional<Sender> findFirstByUserIdAndSenderEmailOrderByIdAsc(Long userId, String senderEmail);
}