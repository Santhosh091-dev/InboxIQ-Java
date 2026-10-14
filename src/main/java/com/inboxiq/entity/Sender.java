package com.inboxiq.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "senders")
@Data
public class Sender {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String senderEmail;
    private int emailCount;

    @Column(length = 1000)
    private String unsubscribeLink;

    private String aiDecision;
    private Double aiConfidence;

    @Column(length = 500)
    private String aiReason;

    private String userOverride;

    private Boolean unsubscribed = false;
    private LocalDateTime unsubscribedAt;

    private LocalDateTime lastScannedAt = LocalDateTime.now();
}