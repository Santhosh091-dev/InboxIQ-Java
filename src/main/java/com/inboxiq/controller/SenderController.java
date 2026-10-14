package com.inboxiq.controller;

import com.inboxiq.entity.Sender;
import com.inboxiq.repository.SenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/senders")
@CrossOrigin(origins = "*")
public class SenderController {

    @Autowired private SenderRepository senderRepository;

    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> list(@PathVariable Long userId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Sender s : senderRepository.findByUserId(userId)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("sender", s.getSenderEmail());
            m.put("aiDecision", s.getAiDecision());
            m.put("userOverride", s.getUserOverride());
            m.put("unsubscribed", Boolean.TRUE.equals(s.getUnsubscribed()));
            out.add(m);
        }
        return out;
    }

    @PutMapping("/{id}/override")
    public ResponseEntity<Map<String, Object>> override(@PathVariable Long id, @RequestParam String decision) {
        if (!decision.equals("keep") && !decision.equals("unsubscribe")) {
            return ResponseEntity.badRequest().body(Map.of("error", "decision must be keep or unsubscribe"));
        }
        Sender s = senderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sender not found: " + id));
        s.setUserOverride(decision);
        senderRepository.save(s);
        return ResponseEntity.ok(Map.of("id", id, "userOverride", decision));
    }
}