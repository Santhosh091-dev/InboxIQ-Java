package com.inboxiq.service;

import com.inboxiq.entity.ScanHistory;
import com.inboxiq.entity.Sender;
import com.inboxiq.entity.User;
import com.inboxiq.repository.ScanHistoryRepository;
import com.inboxiq.repository.SenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GmailScanService {

    @Autowired private GmailDataService gmailData;
    @Autowired private GeminiClassifierService classifier;
    @Autowired private SenderRepository senderRepository;
    @Autowired private ScanHistoryRepository scanHistoryRepository;
    @Autowired private UserService userService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private static String cut(String s, int n) {
        return s == null ? "" : (s.length() > n ? s.substring(0, n) : s);
    }

    // No @Transactional here: each save() commits on its own, so no lock is held during the slow AI calls
    public Map<String, Object> scan(int maxMessages, int maxSenders) throws Exception {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("A scan is already running. Wait for it to finish.");
        }
        try {
            String email = gmailData.myEmail();
            User user = userService.getOrCreateUser(email);
            Map<String, Object> storage = gmailData.storage();

            List<GmailDataService.SenderInfo> found = gmailData.fetchSubscriptionSenders(maxMessages)
                    .stream().limit(maxSenders).toList();

            List<Map<String, Object>> results = new ArrayList<>();
            int flagged = 0;

            for (GmailDataService.SenderInfo s : found) {
                Map<String, Object> c = classifier.classifySender(s.display(), s.count(), s.subjects());

                Sender sender = senderRepository
                        .findFirstByUserIdAndSenderEmailOrderByIdAsc(user.getId(), s.address())
                        .orElseGet(Sender::new);
                sender.setUser(user);
                sender.setSenderEmail(s.address());
                sender.setEmailCount(s.count());
                sender.setUnsubscribeLink(cut(s.unsubscribeLink(), 1000));
                sender.setAiDecision((String) c.get("decision"));
                sender.setAiConfidence((Double) c.get("confidence"));
                sender.setAiReason(cut((String) c.get("reason"), 500));
                sender.setLastScannedAt(LocalDateTime.now());
                sender = senderRepository.save(sender);

                if ("unsubscribe".equalsIgnoreCase(sender.getAiDecision())) flagged++;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", sender.getId());
                row.put("sender", s.address());
                row.put("emailCount", s.count());
                row.put("decision", sender.getAiDecision());
                row.put("confidence", sender.getAiConfidence());
                row.put("reason", sender.getAiReason());
                row.put("hasUnsubscribeLink", !s.unsubscribeLink().isEmpty());
                results.add(row);

                Thread.sleep(4000);
            }

            ScanHistory history = new ScanHistory();
            history.setUser(user);
            history.setStorageUsedGb((Double) storage.get("usedGb"));
            history.setStorageLimitGb((Double) storage.get("limitGb"));
            history.setSendersFlagged(flagged);
            scanHistoryRepository.save(history);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("account", email);
            out.put("userId", user.getId());
            out.put("storage", storage);
            out.put("sendersScanned", results.size());
            out.put("sendersFlagged", flagged);
            out.put("senders", results);
            return out;
        } finally {
            running.set(false);
        }
    }
}