package com.nutrigen.repository;

import com.nutrigen.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {

    @Query("SELECT m FROM Meal m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    List<Meal> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT m FROM Meal m WHERE m.user.id = :userId AND m.mealDate BETWEEN :from AND :to")
    List<Meal> findByUserIdAndMealDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
