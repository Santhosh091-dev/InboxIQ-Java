package com.inboxiq.controller;

import com.inboxiq.service.GeminiClassifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Standalone test endpoint for Gemini classification - no Gmail dependency.
 * Proves the AI integration works in isolation before wiring into the full scan flow.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TestClassifyController {

    @Autowired
    private GeminiClassifierService classifierService;

    @GetMapping("/test-classify")
    public Map<String, Object> testClassify() {
        // Hardcoded sample data to prove the Gemini call works end to end
        String sender = "newsletter@dailydeals.com";
        int count = 12;
        List<String> subjects = List.of(
            "50% off everything today only!",
            "Flash sale ends tonight",
            "You won't believe these deals"
        );
        return classifierService.classifySender(sender, count, subjects);
    }
}
