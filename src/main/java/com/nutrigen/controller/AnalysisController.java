package com.nutrigen.controller;

import com.nutrigen.service.FoodAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final FoodAnalysisService foodAnalysisService;

    public AnalysisController(FoodAnalysisService foodAnalysisService) {
        this.foodAnalysisService = foodAnalysisService;
    }

    @PostMapping(
            value = "/upload-meal",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadMeal(
            @RequestParam("image") MultipartFile image,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "portion", required = false)
            String portion
    ) {
        try {
            FoodAnalysisService.Result result = foodAnalysisService.analyze(image, portion);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("=== FOOD ANALYSIS ERROR ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Food analysis failed."));
        }
    }

    @PostMapping(
            value = "/upload-meals",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadMeals(
            @RequestParam("images") MultipartFile[] images,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "portion", required = false)
            String portion
    ) {
        if (images == null || images.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please select at least one food image."));
        }
        try {
            List<FoodAnalysisService.Result> results = foodAnalysisService.analyzeBatch(List.of(images), portion);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.err.println("=== FOOD ANALYSIS BATCH ERROR ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Batch analysis failed."));
        }
    }
}
