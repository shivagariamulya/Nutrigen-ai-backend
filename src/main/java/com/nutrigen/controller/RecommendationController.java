package com.nutrigen.controller;

import com.nutrigen.model.Meal;
import com.nutrigen.model.UserProfile;
import com.nutrigen.repository.MealRepository;
import com.nutrigen.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final UserRepository users;
    private final MealRepository meals;

    public RecommendationController(UserRepository users, MealRepository meals) {
        this.users = users;
        this.meals = meals;
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        UserProfile user = users.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String goal = user.getGoal();
        if (goal == null || goal.isBlank()) {
            goal = "maintain";
        } else {
            goal = goal.toLowerCase();
        }

        List<String> suggestions;
        if (goal.contains("loss")) {
            suggestions = Arrays.asList(
                    "Oats + eggs",
                    "Grilled chicken/paneer salad",
                    "Dal + vegetables",
                    "Greek yogurt + fruit"
            );
        } else if (goal.contains("gain") || goal.contains("muscle")) {
            suggestions = Arrays.asList(
                    "Oats + milk + banana + nuts",
                    "Chicken/paneer rice bowl",
                    "Dal + roti + vegetables",
                    "Curd + nuts + fruit"
            );
        } else {
            suggestions = Arrays.asList(
                    "Vegetable poha + eggs",
                    "Dal + rice + vegetables",
                    "Chicken/paneer + roti + salad",
                    "Fruit + yogurt"
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("goal", goal);
        response.put("dailyCalories", user.getDailyCalories());
        response.put("dailyProtein", user.getDailyProtein());
        response.put("suggestions", suggestions);
        return response;
    }

    // Dynamic AI Diet Plan prepared according to remaining intake today
    @GetMapping("/daily/{id}")
    public Map<String, Object> getDailyDietPlan(@PathVariable Long id) {
        UserProfile user = users.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        List<Meal> todayMeals = meals.findByUserIdAndMealDateBetween(id, today, today);

        double consumedCal = todayMeals.stream().mapToDouble(Meal::getCalories).sum();
        double consumedProt = todayMeals.stream().mapToDouble(Meal::getProtein).sum();
        double consumedCarbs = todayMeals.stream().mapToDouble(Meal::getCarbohydrates).sum();
        double consumedFat = todayMeals.stream().mapToDouble(Meal::getFats).sum();

        double targetCal = user.getDailyCalories() != null && user.getDailyCalories() > 0 ? user.getDailyCalories() : 2000.0;
        double targetProt = user.getDailyProtein() != null && user.getDailyProtein() > 0 ? user.getDailyProtein() : 100.0;
        double targetCarbs = user.getDailyCarbs() != null && user.getDailyCarbs() > 0 ? user.getDailyCarbs() : 250.0;
        double targetFat = user.getDailyFat() != null && user.getDailyFat() > 0 ? user.getDailyFat() : 65.0;

        double leftCal = Math.max(0, targetCal - consumedCal);
        double leftProt = Math.max(0, targetProt - consumedProt);
        double leftCarbs = Math.max(0, targetCarbs - consumedCarbs);
        double leftFat = Math.max(0, targetFat - consumedFat);

        // BMR and Calories Burned calculation
        double weight = user.getWeightKg() != null && user.getWeightKg() > 0 ? user.getWeightKg() : 70.0;
        double height = user.getHeightCm() != null && user.getHeightCm() > 0 ? user.getHeightCm() : 172.0;
        int age = user.getAge() != null && user.getAge() > 0 ? user.getAge() : 25;
        boolean isMale = user.getGender() == null || user.getGender().equalsIgnoreCase("male");
        double bmr = (10 * weight) + (6.25 * height) - (5 * age) + (isMale ? 5 : -161);
        if (bmr < 1200) bmr = 1600;

        String act = user.getActivityLevel() != null ? user.getActivityLevel().toUpperCase() : "MODERATE";
        double actMultiplier = 1.55;
        if (act.contains("SEDENT")) actMultiplier = 1.2;
        else if (act.contains("LIGHT")) actMultiplier = 1.375;
        else if (act.contains("MOD")) actMultiplier = 1.55;
        else if (act.contains("HIGH") || act.contains("VERY")) actMultiplier = 1.725;
        else if (act.contains("ATHLETE")) actMultiplier = 1.9;

        double burnedToday = Math.round(bmr * actMultiplier);

        // Prepare tailored diet plan based on remaining budget and goal
        List<Map<String, Object>> suggestedMeals = new ArrayList<>();
        String statusMessage;

        if (leftCal > 750) {
            statusMessage = "You have " + Math.round(leftCal) + " kcal and " + Math.round(leftProt) + "g protein left to reach your goal today. Here is a balanced plan:";
            suggestedMeals.add(Map.of(
                    "type", "Recommended Main Meal (Dinner)",
                    "title", leftProt > 35 ? "Grilled Salmon Fillet & Quinoa Bowl" : "Spiced Paneer & Brown Rice Bowl",
                    "calories", 520,
                    "protein", leftProt > 35 ? 42 : 28,
                    "carbs", 54,
                    "fat", 16,
                    "description", "Nutrient-dense dinner rich in complex carbs, clean protein, and healthy omega fats."
            ));
            suggestedMeals.add(Map.of(
                    "type", "Evening Recovery Snack",
                    "title", "Greek Yogurt with Berries & Honey",
                    "calories", 220,
                    "protein", 20,
                    "carbs", 24,
                    "fat", 5,
                    "description", "Slow-digesting casein protein snack to repair muscle overnight."
            ));
        } else if (leftCal > 350) {
            statusMessage = "You are near your goal! You have " + Math.round(leftCal) + " kcal left. A focused protein-rich meal will complete your target:";
            suggestedMeals.add(Map.of(
                    "type", "Targeted Dinner",
                    "title", "Lemon Herb Chicken Breast & Mixed Greens",
                    "calories", 380,
                    "protein", 36,
                    "carbs", 18,
                    "fat", 14,
                    "description", "High protein, low carb plate to reach your macro goal without excess calories."
            ));
        } else if (leftCal > 100) {
            statusMessage = "Almost there! Only " + Math.round(leftCal) + " kcal remaining today. A light snack will hit the bullseye:";
            suggestedMeals.add(Map.of(
                    "type", "Light Snack",
                    "title", "2 Hard-Boiled Eggs or Whey Protein Shake",
                    "calories", 180,
                    "protein", 22,
                    "carbs", 4,
                    "fat", 6,
                    "description", "Quick zero-carb protein to finish your protein intake cleanly."
            ));
        } else {
            statusMessage = "Target reached! You have fulfilled today's calorie budget. Focus on hydration and recovery.";
            suggestedMeals.add(Map.of(
                    "type", "Hydration & Digestion",
                    "title", "Chamomile Green Tea with Mint & Lemon",
                    "calories", 10,
                    "protein", 0,
                    "carbs", 2,
                    "fat", 0,
                    "description", "Calming antioxidant infusion to promote metabolism and restorative sleep."
            ));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("targetCalories", Math.round(targetCal));
        result.put("targetProtein", Math.round(targetProt));
        result.put("targetCarbs", Math.round(targetCarbs));
        result.put("targetFat", Math.round(targetFat));

        result.put("consumedCalories", Math.round(consumedCal));
        result.put("consumedProtein", Math.round(consumedProt));
        result.put("consumedCarbs", Math.round(consumedCarbs));
        result.put("consumedFat", Math.round(consumedFat));

        result.put("leftCalories", Math.round(leftCal));
        result.put("leftProtein", Math.round(leftProt));
        result.put("leftCarbs", Math.round(leftCarbs));
        result.put("leftFat", Math.round(leftFat));

        result.put("caloriesBurnedToday", burnedToday);
        result.put("netCaloriesToday", Math.round(consumedCal - burnedToday));

        result.put("statusMessage", statusMessage);
        result.put("suggestedMeals", suggestedMeals);
        result.put("goal", user.getGoal());

        return result;
    }
}
