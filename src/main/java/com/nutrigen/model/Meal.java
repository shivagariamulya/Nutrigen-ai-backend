package com.nutrigen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private UserProfile user;

    private String imageName;
    private String foodName;

    @Column(name = "meal_name")
    private String mealName;

    private String portion;
    private Double calories;
    private Double protein;
    private Double carbohydrates;
    private Double fats;

    private LocalDate mealDate = LocalDate.now();
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserProfile getUser() { return user; }
    public void setUser(UserProfile user) { this.user = user; }

    @Transient
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getFoodName() { return foodName != null ? foodName : mealName; }
    public void setFoodName(String foodName) {
        this.foodName = foodName;
        this.mealName = foodName;
    }

    public String getMealName() { return mealName != null ? mealName : foodName; }
    public void setMealName(String mealName) {
        this.mealName = mealName;
        if (this.foodName == null) this.foodName = mealName;
    }

    public String getPortion() { return portion; }
    public void setPortion(String portion) { this.portion = portion; }

    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }

    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein; }

    public Double getCarbohydrates() { return carbohydrates; }
    public void setCarbohydrates(Double carbohydrates) { this.carbohydrates = carbohydrates; }

    public Double getFats() { return fats; }
    public void setFats(Double fats) { this.fats = fats; }

    public LocalDate getMealDate() { return mealDate; }
    public void setMealDate(LocalDate mealDate) { this.mealDate = mealDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
