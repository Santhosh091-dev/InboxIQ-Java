package com.inboxiq.controller;

import com.inboxiq.entity.Sender;
import com.inboxiq.repository.SenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/senders")
@CrossOrigin(origins = "*")
public class UnsubscribeController {

    @Autowired private SenderRepository senderRepository;

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean dryRun) {

        Sender s = senderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sender not found: " + id));

        if (Boolean.TRUE.equals(s.getUnsubscribed())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already unsubscribed: " + s.getSenderEmail()));
        }

        String link = extractHttpLink(s.getUnsubscribeLink());
        if (link == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No http unsubscribe link for " + s.getSenderEmail()));
        }

        boolean success = true;
        if (!dryRun) {
            success = callLink(link);
        }

        if (success) {
            s.setUnsubscribed(true);
            s.setUnsubscribedAt(LocalDateTime.now());
            senderRepository.save(s);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", s.getId());
        out.put("sender", s.getSenderEmail());
        out.put("dryRun", dryRun);
        out.put("success", success);
        out.put("link", link);
        return ResponseEntity.ok(out);
    }

    private String extractHttpLink(String header) {
        if (header == null || header.isBlank()) return null;
        Matcher m = Pattern.compile("<(.*?)>").matcher(header);
        while (m.find()) {
            if (m.group(1).startsWith("http")) return m.group(1);
        }
        return null;
    }

    private boolean callLink(String link) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(link)).timeout(Duration.ofSeconds(8)).GET().build();
            HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());
            return res.statusCode() >= 200 && res.statusCode() < 400;
        } catch (Exception e) {
            return false;
        }
    }
}