package com.nutrigen.controller;

import com.nutrigen.model.Meal;
import com.nutrigen.model.UserProfile;
import com.nutrigen.repository.MealRepository;
import com.nutrigen.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
public class MealController {

 private final MealRepository meals;
 private final UserRepository users;

 public MealController(
         MealRepository meals,
         UserRepository users) {

  this.meals = meals;
  this.users = users;
 }

 // Save batch of analyzed meals
 @PostMapping("/batch")
 public List<Meal> saveBatch(@RequestBody List<Map<String, Object>> list) {
  if (list == null || list.isEmpty()) return List.of();
  List<Meal> saved = new java.util.ArrayList<>();
  for (Map<String, Object> body : list) {
   saved.add(save(body));
  }
  return saved;
 }

 // Save analyzed meal
 @PostMapping
 public Meal save(@RequestBody Map<String, Object> body) {

  if (body == null || body.get("userId") == null || body.get("userId").toString().isBlank()) {
   throw new IllegalArgumentException("User ID is required");
  }

  Long userId;
  try {
      userId = Long.valueOf(body.get("userId").toString().trim());
  } catch (Exception e) {
      throw new IllegalArgumentException("Invalid User ID format: " + body.get("userId"));
  }

  UserProfile user = users.findById(userId)
          .orElseThrow(() ->
                  new RuntimeException("User not found: " + userId));

  Meal meal = new Meal();

  meal.setUser(user);

  meal.setFoodName(
          body.get("foodName") != null
                  ? body.get("foodName").toString()
                  : "Meal"
  );

  meal.setPortion(
          body.get("portion") != null
                  ? body.get("portion").toString()
                  : "1 serving"
  );

  meal.setImageName(
          body.get("imageName") != null
                  ? body.get("imageName").toString()
                  : null
  );

  meal.setCalories(parseDouble(body.get("calories")));
  meal.setProtein(parseDouble(body.get("protein")));
  meal.setCarbohydrates(parseDouble(body.get("carbohydrates")));
  meal.setFats(parseDouble(body.get("fats")));

  return meals.save(meal);
 }

 private Double parseDouble(Object val) {
     if (val == null) return 0.0;
     if (val instanceof Number n) return n.doubleValue();
     String s = val.toString().replaceAll("[^0-9.]", "").trim();
     if (s.isEmpty()) return 0.0;
     try {
         return Double.parseDouble(s);
     } catch (Exception e) {
         return 0.0;
     }
 }

 // Get meal history
 @GetMapping("/user/{id}")
 public List<Meal> history(@PathVariable Long id) {

  return meals.findByUserIdOrderByCreatedAtDesc(id);
 }

 // Delete a meal
 @DeleteMapping("/{id}")
 public ResponseEntity<Map<String, Object>> deleteMeal(@PathVariable Long id) {
  if (!meals.existsById(id)) {
   return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Meal not found"));
  }
  meals.deleteById(id);
  return ResponseEntity.ok(Map.of("message", "Meal deleted successfully", "id", id));
 }

 // Get nutrition summary
 @GetMapping("/summary/{id}")
 public Map<String, Object> summary(
         @PathVariable Long id,
         @RequestParam(defaultValue = "7") int days) {

  LocalDate to = LocalDate.now();
  LocalDate from = to.minusDays(days - 1);

  List<Meal> mealList =
          meals.findByUserIdAndMealDateBetween(
                  id,
                  from,
                  to
          );

  double calories = mealList.stream()
          .mapToDouble(Meal::getCalories)
          .sum();

  double protein = mealList.stream()
          .mapToDouble(Meal::getProtein)
          .sum();

  double carbohydrates = mealList.stream()
          .mapToDouble(Meal::getCarbohydrates)
          .sum();

  double fats = mealList.stream()
          .mapToDouble(Meal::getFats)
          .sum();

  Map<String, Object> result = new HashMap<>();

  result.put("from", from);
  result.put("to", to);
  result.put("meals", mealList.size());
  result.put("calories", calories);
  result.put("protein", protein);
  result.put("carbohydrates", carbohydrates);
  result.put("fats", fats);

  return result;
 }

  // Weekly & Monthly Reports with Daily Breakdown & Calories Burned
  @GetMapping("/reports/{id}")
  public Map<String, Object> reports(
          @PathVariable Long id,
          @RequestParam(defaultValue = "7") int days) {

   UserProfile user = users.findById(id)
           .orElseThrow(() -> new RuntimeException("User not found"));

   LocalDate to = LocalDate.now();
   LocalDate from = to.minusDays(days - 1);

   List<Meal> mealList = meals.findByUserIdAndMealDateBetween(id, from, to);

   // Calculate BMR & TDEE (Daily Calories Burned)
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

   double dailyBurn = Math.round(bmr * actMultiplier);

   Map<LocalDate, List<Meal>> byDate = new HashMap<>();
   for (Meal m : mealList) {
       if (m.getMealDate() != null) {
           byDate.computeIfAbsent(m.getMealDate(), k -> new java.util.ArrayList<>()).add(m);
       }
   }

   List<Map<String, Object>> dailyBreakdown = new java.util.ArrayList<>();
   double totalCalories = 0;
   double totalProtein = 0;
   double totalCarbs = 0;
   double totalFats = 0;
   int daysWithMeals = 0;
   int onTrackDays = 0;

   double targetCal = user.getDailyCalories() != null && user.getDailyCalories() > 0 ? user.getDailyCalories() : 2000.0;

   for (int i = days - 1; i >= 0; i--) {
       LocalDate date = to.minusDays(i);
       List<Meal> dayMeals = byDate.getOrDefault(date, List.of());

       double dayCal = dayMeals.stream().mapToDouble(Meal::getCalories).sum();
       double dayProt = dayMeals.stream().mapToDouble(Meal::getProtein).sum();
       double dayCarb = dayMeals.stream().mapToDouble(Meal::getCarbohydrates).sum();
       double dayFat = dayMeals.stream().mapToDouble(Meal::getFats).sum();

       totalCalories += dayCal;
       totalProtein += dayProt;
       totalCarbs += dayCarb;
       totalFats += dayFat;

       if (!dayMeals.isEmpty()) {
           daysWithMeals++;
           if (Math.abs(dayCal - targetCal) <= 250) {
               onTrackDays++;
           }
       }

       double netCal = dayCal - dailyBurn;

       Map<String, Object> dayMap = new HashMap<>();
       dayMap.put("date", date.toString());
       dayMap.put("dayOfWeek", date.getDayOfWeek().toString().substring(0, 3));
       dayMap.put("calories", Math.round(dayCal));
       dayMap.put("protein", Math.round(dayProt));
       dayMap.put("carbohydrates", Math.round(dayCarb));
       dayMap.put("fats", Math.round(dayFat));
       dayMap.put("mealsCount", dayMeals.size());
       dayMap.put("caloriesBurned", Math.round(dailyBurn));
       dayMap.put("netCalories", Math.round(netCal));
       dayMap.put("targetCalories", Math.round(targetCal));

       dailyBreakdown.add(dayMap);
   }

   double totalBurned = dailyBurn * days;
   double netPeriodCalories = totalCalories - totalBurned;

   Map<String, Object> report = new HashMap<>();
   report.put("period", days <= 7 ? "WEEKLY" : "MONTHLY");
   report.put("days", days);
   report.put("from", from.toString());
   report.put("to", to.toString());
   report.put("dailyBreakdown", dailyBreakdown);

   report.put("totalCaloriesIntake", Math.round(totalCalories));
   report.put("avgDailyCaloriesIntake", Math.round(totalCalories / days));
   report.put("totalCaloriesBurned", Math.round(totalBurned));
   report.put("dailyCaloriesBurned", Math.round(dailyBurn));
   report.put("netCalories", Math.round(netPeriodCalories));

   report.put("totalProtein", Math.round(totalProtein));
   report.put("avgDailyProtein", Math.round(totalProtein / days));
   report.put("totalCarbs", Math.round(totalCarbs));
   report.put("avgDailyCarbs", Math.round(totalCarbs / days));
   report.put("totalFats", Math.round(totalFats));
   report.put("avgDailyFats", Math.round(totalFats / days));

   report.put("daysTracked", daysWithMeals);
   report.put("onTrackDays", onTrackDays);
   report.put("targetDailyCalories", Math.round(targetCal));

   return report;
  }
}