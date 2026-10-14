package com.inboxiq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_history")
@Data
public class ScanHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double storageUsedGb;
    private Double storageLimitGb;
    private int sendersFlagged;
    private LocalDateTime scannedAt = LocalDateTime.now();
}
