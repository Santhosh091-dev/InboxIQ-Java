package com.inboxiq.controller;

import com.inboxiq.service.ScanService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Validated
public class ScanController {

    @Autowired private ScanService scanService;

    @GetMapping("/scan-demo")
    public List<Map<String, Object>> scanDemo(
            @RequestParam
            @NotBlank(message = "email is required")
            @Email(message = "email format is invalid")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                     message = "email must look like name@domain.com")
            String email) throws Exception {
        return scanService.demoScan(email);
    }
}