package com.inboxiq.controller;

import com.inboxiq.repository.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired private AnalyticsRepository analyticsRepository;

    @GetMapping("/summary/{userId}")
    public List<Map<String, Object>> summary(@PathVariable Long userId) {
        return analyticsRepository.decisionSummary(userId);
    }
}