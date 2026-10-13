package com.nutrigen.controller;

import com.nutrigen.model.UserProfile;
import com.nutrigen.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserRepository users;

    public ProfileController(UserRepository users) {
        this.users = users;
    }

    // Get user profile
    @GetMapping("/{id}")
    public UserProfile get(@PathVariable Long id) {

        return users.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Update user profile
    @PutMapping("/{id}")
    public UserProfile update(
            @PathVariable Long id,
            @RequestBody UserProfile updatedProfile) {

        UserProfile user = users.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (updatedProfile.getName() != null && !updatedProfile.getName().isBlank()) user.setName(updatedProfile.getName());
        if (updatedProfile.getAge() != null) user.setAge(updatedProfile.getAge());
        if (updatedProfile.getHeightCm() != null) user.setHeightCm(updatedProfile.getHeightCm());
        if (updatedProfile.getWeightKg() != null) user.setWeightKg(updatedProfile.getWeightKg());
        if (updatedProfile.getGender() != null) user.setGender(updatedProfile.getGender());
        if (updatedProfile.getActivityLevel() != null) user.setActivityLevel(updatedProfile.getActivityLevel());
        if (updatedProfile.getGoal() != null) user.setGoal(updatedProfile.getGoal());

        if (updatedProfile.getDailyCalories() != null && updatedProfile.getDailyCalories() > 0) user.setDailyCalories(updatedProfile.getDailyCalories());
        if (updatedProfile.getDailyProtein() != null && updatedProfile.getDailyProtein() > 0) user.setDailyProtein(updatedProfile.getDailyProtein());
        if (updatedProfile.getDailyCarbs() != null && updatedProfile.getDailyCarbs() > 0) user.setDailyCarbs(updatedProfile.getDailyCarbs());
        if (updatedProfile.getDailyFat() != null && updatedProfile.getDailyFat() > 0) user.setDailyFat(updatedProfile.getDailyFat());

        return users.save(user);
    }
}