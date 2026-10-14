package com.inboxiq.repository;

import com.inboxiq.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {
}
