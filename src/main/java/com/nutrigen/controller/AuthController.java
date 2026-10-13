package com.nutrigen.controller;

import com.nutrigen.model.UserProfile;
import com.nutrigen.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, String> body) {

        String name = body != null ? body.getOrDefault("name", "User") : "User";
        String email = body != null ? body.get("email") : null;
        String password = body != null ? body.get("password") : null;

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
        }

        email = email.trim().toLowerCase();

        if (users.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }

        UserProfile user = new UserProfile();
        user.setName(name.trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(password));

        UserProfile savedUser = users.save(user);

        return ResponseEntity.ok(safe(savedUser));
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body) {

        String email = body != null ? body.get("email") : null;
        String password = body != null ? body.get("password") : null;

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
        }

        email = email.trim().toLowerCase();

        UserProfile user = users.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }

        boolean passwordMatches = false;
        String storedPassword = user.getPassword();

        if (storedPassword != null) {
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                passwordMatches = encoder.matches(password, storedPassword);
            } else {
                // Support plain-text passwords from initial database seeds and auto-upgrade to BCrypt
                if (storedPassword.equals(password)) {
                    passwordMatches = true;
                    user.setPassword(encoder.encode(password));
                    users.save(user);
                }
            }
        }

        if (!passwordMatches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }

        return ResponseEntity.ok(safe(user));
    }

    // Return safe user information
    private Map<String, Object> safe(UserProfile user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("dailyCalories", user.getDailyCalories());
        response.put("dailyProtein", user.getDailyProtein());
        response.put("dailyCarbs", user.getDailyCarbs());
        response.put("dailyFat", user.getDailyFat());
        response.put("goal", user.getGoal());
        return response;
    }
}