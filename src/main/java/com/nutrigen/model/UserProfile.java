package com.nutrigen.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private Integer age;
    private Double heightCm;
    private Double weightKg;
    private String gender;
    private String activityLevel;
    private String goal;

    private Double dailyCalories = 2000.0;
    private Double dailyProtein = 100.0;
    private Double dailyCarbs = 250.0;
    private Double dailyFat = 65.0;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public Integer getAge() { return age; }
    public Double getHeightCm() { return heightCm; }
    public Double getWeightKg() { return weightKg; }
    public String getGender() { return gender; }
    public String getActivityLevel() { return activityLevel; }
    public String getGoal() { return goal; }
    public Double getDailyCalories() { return dailyCalories; }
    public Double getDailyProtein() { return dailyProtein; }
    public Double getDailyCarbs() { return dailyCarbs; }
    public Double getDailyFat() { return dailyFat; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setAge(Integer age) { this.age = age; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public void setGender(String gender) { this.gender = gender; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public void setGoal(String goal) { this.goal = goal; }
    public void setDailyCalories(Double dailyCalories) { this.dailyCalories = dailyCalories; }
    public void setDailyProtein(Double dailyProtein) { this.dailyProtein = dailyProtein; }
    public void setDailyCarbs(Double dailyCarbs) { this.dailyCarbs = dailyCarbs; }
    public void setDailyFat(Double dailyFat) { this.dailyFat = dailyFat; }
}
