package com.inboxiq.service;

import com.inboxiq.entity.ScanHistory;
import com.inboxiq.entity.Sender;
import com.inboxiq.entity.User;
import com.inboxiq.repository.ScanHistoryRepository;
import com.inboxiq.repository.SenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScanService {

    @Autowired private GeminiClassifierService classifier;
    @Autowired private SenderRepository senderRepository;
    @Autowired private ScanHistoryRepository scanHistoryRepository;
    @Autowired private UserService userService;

    private record Sample(String sender, int count, String link, List<String> subjects) {}

    private static final List<Sample> SAMPLES = List.of(
        new Sample("deals@dailydeals.com", 12, "<https://dailydeals.com/unsub>",
            List.of("50% off today only", "Flash sale ends tonight")),
        new Sample("hr@mycollege.edu", 3, "",
            List.of("Exam timetable released", "Fee payment reminder")),
        new Sample("newsletter@shopzone.com", 25, "<https://shopzone.com/unsub>",
            List.of("New arrivals just for you", "Your cart misses you"))
    );

    public List<Map<String, Object>> demoScan(String email) throws InterruptedException {
        User user = userService.getOrCreateUser(email);
        List<Map<String, Object>> results = new ArrayList<>();
        int flagged = 0;

        for (Sample s : SAMPLES) {
            Map<String, Object> c = classifier.classifySender(s.sender(), s.count(), s.subjects());

            // Upsert: reuse the existing row for this user + sender, otherwise create one
            Sender sender = senderRepository
                    .findFirstByUserIdAndSenderEmailOrderByIdAsc(user.getId(), s.sender())
                    .orElseGet(Sender::new);

            sender.setUser(user);
            sender.setSenderEmail(s.sender());
            sender.setEmailCount(s.count());
            sender.setUnsubscribeLink(s.link());
            sender.setAiDecision((String) c.get("decision"));
            sender.setAiConfidence((Double) c.get("confidence"));
            sender.setAiReason((String) c.get("reason"));
            sender.setLastScannedAt(LocalDateTime.now());
            senderRepository.save(sender);

            if ("unsubscribe".equalsIgnoreCase(sender.getAiDecision())) flagged++;

            results.add(Map.of(
                "sender", s.sender(),
                "emailCount", s.count(),
                "decision", sender.getAiDecision(),
                "confidence", sender.getAiConfidence(),
                "reason", sender.getAiReason()
            ));
            Thread.sleep(2000);
        }

        ScanHistory history = new ScanHistory();
        history.setUser(user);
        history.setSendersFlagged(flagged);
        scanHistoryRepository.save(history);

        return results;
    }
}
