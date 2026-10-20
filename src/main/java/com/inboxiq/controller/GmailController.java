package com.inboxiq.controller;

import com.inboxiq.service.GmailDataService;
import com.inboxiq.service.GmailScanService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gmail")
@CrossOrigin(origins = "*")
@Validated
public class GmailController {

    @Autowired private GmailDataService gmailData;
    @Autowired private GmailScanService scanService;

    @GetMapping("/storage")
    public Map<String, Object> storage() throws Exception {
        return gmailData.storage();
    }

    @GetMapping("/scan")
    public Map<String, Object> scan(
            @RequestParam(defaultValue = "40") @Min(1) @Max(100) int maxMessages,
            @RequestParam(defaultValue = "8") @Min(1) @Max(15) int maxSenders) throws Exception {
        return scanService.scan(maxMessages, maxSenders);
    }
}