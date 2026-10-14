package com.inboxiq.controller;

import com.inboxiq.entity.User;
import com.inboxiq.repository.SenderRepository;
import com.inboxiq.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Validated
public class UserController {

    // DTO: only the fields the API should expose, so the entity cycle is never serialized
    public record UserDto(Long id, String email, LocalDateTime createdAt, int senderCount) {}

    @Autowired private UserService userService;
    @Autowired private SenderRepository senderRepository;

    @GetMapping("/user")
    public UserDto getUser(
            @RequestParam
            @NotBlank(message = "email is required")
            @Email(message = "email format is invalid")
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                     message = "email must look like name@domain.com")
            String email) {
        User user = userService.getOrCreateUser(email);
        int count = senderRepository.findByUserId(user.getId()).size();
        return new UserDto(user.getId(), user.getEmail(), user.getCreatedAt(), count);
    }
}